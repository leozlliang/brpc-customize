package com.yy.lite.brpc.protocol.codec;

import com.baidu.brpc.RpcMethodInfo;
import com.google.common.collect.Maps;
import com.yy.anka.io.rpc.parse.*;
import org.apache.commons.lang3.tuple.Pair;

import java.util.Map;

/**
 * @author donghonghua
 * @date 2019/7/26
 */
public class BrpcMetaService {

    private static volatile BrpcMetaService instance;

    public static BrpcMetaService getInstance() {
        if (instance == null) {
            synchronized (BrpcMetaService.class) {
                if (instance == null) {
                    instance = new BrpcMetaService();
                }
            }
        }
        return instance;
    }

    private BrpcMetaService() {
    }

    private Map<Class, BrpcMetaConverter> brpcMetaConverterMap = Maps.newHashMapWithExpectedSize(4);
    {
        brpcMetaConverterMap.put(ServiceRPCInfo.class, new ServiceRpcInfoBrpcMetaConverter());
        brpcMetaConverterMap.put(SrvRPCInfo.class, new SrvRpcInfoBrpcMetaConverter());
        brpcMetaConverterMap.put(UriRPCInfo.class, new UriRpcInfoBrpcMetaConverter());
        brpcMetaConverterMap.put(YypRPCInfo.class, new YypRpcInfoBrpcMetaConverter());
    }

    @SuppressWarnings("unchecked")
    public RpcMethodInfo findRpcMethodInfo(RPCInfo rpcInfo, RPCInfoType infoType) {

        BrpcMetaConverter converter = brpcMetaConverterMap.get(rpcInfo.getClass());
        if (converter == null) {
            return null;
        }
        switch (infoType) {
            case REQ:
                return converter.getRpcMethodInfoByReqRPCInfo(rpcInfo);
            case RESP:
                return converter.getRpcMethodInfoByRespRPCInfo(rpcInfo);
            default:
                return null;
        }
    }

    public Pair<RPCInfo, RPCInfo> findRPCPair(String serviceName, String methodName) {
        return YypProtocolManager.getInstance().findRPCPair(serviceName, methodName);
    }

    public enum RPCInfoType {
        /**
         * 请求
         */
        REQ,
        /**
         * 返回
         */
        RESP
    }
}
