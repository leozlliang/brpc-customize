package com.yy.lite.brpc.protocol;

import com.baidu.brpc.ChannelInfo;
import com.baidu.brpc.RpcMethodInfo;
import com.baidu.brpc.buffer.DynamicCompositeByteBuf;
import com.baidu.brpc.client.RpcFuture;
import com.baidu.brpc.exceptions.BadSchemaException;
import com.baidu.brpc.exceptions.NotEnoughDataException;
import com.baidu.brpc.exceptions.RpcException;
import com.baidu.brpc.exceptions.TooBigDataException;
import com.baidu.brpc.protocol.AbstractProtocol;
import com.baidu.brpc.protocol.Request;
import com.baidu.brpc.protocol.Response;
import com.baidu.brpc.protocol.RpcResponse;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.Maps;
import com.yy.anka.io.codec.AnkaPacket;
import com.yy.anka.io.codec.PacketUtils;
import com.yy.anka.io.rpc.parse.RPCInfo;
import com.yy.ent.commons.protopack.base.Packet;
import com.yy.lite.brpc.protocol.codec.BrpcMetaService;
import com.yy.lite.brpc.protocol.codec.YYPDecoder;
import com.yy.lite.brpc.protocol.codec.YypProtocolManager;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @author donghonghua
 * @date 2019/7/23
 */
public class YypRpcProtocol extends AbstractProtocol {

    private final Logger logger = LoggerFactory.getLogger(YypRpcProtocol.class);

    private final YYPDecoder decoder = new YYPDecoder();

    private static final String REQ_PACKET_KEY = "reqPacket";

    private Cache<String, Object> cache = CacheBuilder.newBuilder()
            .expireAfterWrite(1, TimeUnit.MINUTES)
            .build();

    @Override
    public ByteBuf encodeRequest(Request request) throws Exception {

        RPCInfo reqInfo = BrpcMetaService.getInstance().findRPCPair(request.getServiceName(), request.getMethodName()).getLeft();
        if (reqInfo == null) {
            throw new Exception("error client protocol from method:" + request.getTargetMethod().getName());
        }
        YypProtocolManager.getInstance().registerMethodInfoByMethodName(request.getMethodName(), request.getRpcMethodInfo());
        Packet p = PacketUtils.parsePacket(null, request.getArgs()[0], reqInfo);
        return encodePacket(p);
    }

    @Override
    public Object decode(ChannelHandlerContext ctx, DynamicCompositeByteBuf in,
                         boolean isDecodingRequest)
            throws BadSchemaException, TooBigDataException, NotEnoughDataException {

        ByteBuf fixHeaderBuf = in.retainedSlice(in.readableBytes());

        try {
            ByteBuf frame = (ByteBuf) decoder.decode(ctx, fixHeaderBuf);
            if (frame == null) {
                throw notEnoughDataException;
            }
            byte[] array = new byte[frame.readableBytes() - 4];
            frame.getBytes(4, array, 0, array.length);
            Object packet = PacketUtils.decode(array);
            if (packet == null) {
                throw new BadSchemaException("bad schema!");
            }
            in.skipBytes(frame.readableBytes());
            return packet;
        } catch (NotEnoughDataException|BadSchemaException e ) {
            throw e;
        } catch (Exception e) {
            throw new BadSchemaException("bad schema!");
        } finally {
            fixHeaderBuf.release();
        }
    }

    @Override
    public Response decodeResponse(Object in, ChannelHandlerContext ctx) throws Exception {
        Response response = new RpcResponse();

        try {

            ChannelInfo channelInfo = ChannelInfo.getClientChannelInfo(ctx.channel());
            long relationId = channelInfo.getCorrelationId();
            RpcFuture future = channelInfo.getRpcFuture(relationId);
            if (future == null) {
                logger.warn("failed to find future");
                response.setException(new RpcException(RpcException.NETWORK_EXCEPTION, "can not find future"));
                return response;
            }
            AnkaPacket packet = (AnkaPacket) in;
            RPCInfo rpcInfo = packet.generatorRPCInfo();
            RpcMethodInfo methodInfo = BrpcMetaService.getInstance().findRpcMethodInfo(rpcInfo, BrpcMetaService.RPCInfoType.RESP);
            if (methodInfo == null) {
                logger.warn("failed to find methodInfo");
                response.setException(new RpcException(RpcException.NETWORK_EXCEPTION, "can not find methodInfo"));
                return response;
            }
            if (!methodInfo.getMethodName().equals(future.getRpcMethodInfo().getMethodName())
                    || !methodInfo.getServiceName().equals(future.getRpcMethodInfo().getServiceName())) {
                logger.warn("methodInfo is not fixed");
                response.setException(new RpcException(RpcException.NETWORK_EXCEPTION, "can not find fixed methodInfo"));
                return response;
            }
            channelInfo.removeRpcFuture(relationId);
            Object result = packet.convertToObj(methodInfo.getOutputClass());
            response.setRpcMethodInfo(methodInfo);
            response.setResult(result);
            response.setRpcFuture(future);
        } catch (Exception e) {
            logger.error("failed to decode response", e);
            response.setException(new RpcException(RpcException.SERVICE_EXCEPTION, "failed to decode response"));
        }
        return response;
    }

    @Override
    public Request decodeRequest(Object packet) throws Exception {

        Request request = this.getRequest();
        AnkaPacket ankaPacket = (AnkaPacket) packet;

        RPCInfo rpcInfo = ankaPacket.generatorRPCInfo();
        if (rpcInfo == null) {
            request.setException(new RpcException(RpcException.SERVICE_EXCEPTION, "Fail to find rpcInfo"));
            return request;
        }

        try {
            // service info
            RpcMethodInfo rpcMethodInfo = BrpcMetaService.getInstance().findRpcMethodInfo(rpcInfo, BrpcMetaService.RPCInfoType.REQ);

            if (rpcMethodInfo == null) {
                String errorMsg = "Fail to find rpcMethodInfo";
                request.setException(new RpcException(RpcException.SERVICE_EXCEPTION, errorMsg));
                return request;
            }

            request.setServiceName(rpcMethodInfo.getServiceName());
            request.setMethodName(rpcMethodInfo.getMethodName());
            request.setRpcMethodInfo(rpcMethodInfo);
            request.setTargetMethod(rpcMethodInfo.getMethod());
            request.setTarget(rpcMethodInfo.getTarget());

            String key = Thread.currentThread().getName() + "_" + System.currentTimeMillis();
            Map<String, Object> extendsMaps = Maps.newHashMapWithExpectedSize(2);
            cache.put(key, ankaPacket);
            extendsMaps.put(REQ_PACKET_KEY, key);
            request.setKvAttachment(extendsMaps);
            Object obj = ankaPacket.convertToObj(rpcMethodInfo.getInputClasses()[0]);
            request.setArgs(new Object[]{obj});
            return request;
        } catch (Exception ex) {
            String errorMsg = String.format("decode failed, msg=%s", ex.getMessage());
            throw new RpcException(RpcException.SERIALIZATION_EXCEPTION, errorMsg, ex);
        }
    }

    @Override
    public ByteBuf encodeResponse(Request request, Response response) throws Exception {
        Pair<RPCInfo, RPCInfo> pair = BrpcMetaService.getInstance().findRPCPair(request.getServiceName(), request.getMethodName());
        RPCInfo resInfo = pair.getRight();
        logger.info("encode response for res rpc info {}", resInfo);
        String key = (String) request.getKvAttachment().get(REQ_PACKET_KEY);
        Packet reqPacket = (Packet) cache.get(key, () -> {
            logger.error("req error");
            return null;
        });
        cache.invalidate(key);
        Packet p = PacketUtils.parsePacket(reqPacket, response.getResult(), resInfo);
        return encodePacket(p);
    }

    private ByteBuf encodePacket(Packet packet) throws Exception {
        ByteBuffer data = PacketUtils.encode(packet, 10 * 1024 * 1024);
        int size = data.limit() - data.position() + 4;
        logger.debug("packet Pack size :{}  Resp :{}", size, packet);
        ByteBuf buf = Unpooled.buffer(size);
        buf.writeIntLE(size);
        buf.writeBytes(data);
        return buf;
    }

    @Override
    public boolean returnChannelBeforeResponse() {
        return false;
    }

    @Override
    public boolean isCoexistence() {
        return true;
    }
}
