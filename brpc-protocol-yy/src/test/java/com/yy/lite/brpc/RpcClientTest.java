package com.yy.lite.brpc;

import com.baidu.brpc.client.BrpcProxy;
import com.baidu.brpc.client.RpcClient;
import com.baidu.brpc.client.RpcClientOptions;
import com.baidu.brpc.client.loadbalance.LoadBalanceStrategy;
import com.baidu.brpc.exceptions.RpcException;
import com.baidu.brpc.interceptor.Interceptor;
import com.baidu.brpc.protocol.Options;
import com.yy.anka.io.codec.protocol.bean.MChannel;
import com.yy.lite.brpc.protocol.CustomProtocolTypeEnum;
import com.yy.lite.brpc.service.EchoService;
import com.yy.lite.brpc.service.VideoTaskShow;

import java.util.ArrayList;
import java.util.List;

/**
 * @author donghonghua
 * @date 2019/7/25
 */
@SuppressWarnings("unchecked")
public class RpcClientTest {

    public static void main(String[] args) {
        RpcClientOptions clientOption = new RpcClientOptions();
        clientOption.setProtocolType(CustomProtocolTypeEnum.YYP.type);
        clientOption.setWriteTimeoutMillis(10000);
        clientOption.setReadTimeoutMillis(10000);
        clientOption.setMaxTotalConnections(10000);
        clientOption.setMinIdleConnections(10);
        clientOption.setLoadBalanceType(LoadBalanceStrategy.LOAD_BALANCE_FAIR);
        clientOption.setCompressType(Options.CompressType.COMPRESS_TYPE_NONE);

        String serviceUrl = "list://127.0.0.1:8002";
//        String serviceUrl = "list://14.215.104.72:38313";
//        String serviceUrl = "zookeeper://127.0.0.1:2181";
        if (args.length == 1) {
            serviceUrl = args[0];
        }

        List<Interceptor> interceptors = new ArrayList<Interceptor>();

        // sync call
        RpcClient rpcClient = new RpcClient(serviceUrl, clientOption, interceptors);
//        RpcClient rpcClient = new RpcClient(serviceUrl, clientOption, interceptors);
        EchoService echoService = BrpcProxy.getProxy(rpcClient, EchoService.class);
        try {
            MChannel<VideoTaskShow.VideoTaskShowReq> mChannel = new MChannel<>();
            mChannel.uid = 1644232461L;
            mChannel.topSid = 1345167196L;
            mChannel.subSid = 1345167196L;
            mChannel.setContentType("protobuf");
            VideoTaskShow.VideoTaskShowReq.Builder builder =  VideoTaskShow.VideoTaskShowReq.newBuilder();
            mChannel.payload = builder.build();
            VideoTaskShow.VideoTaskShowResp response = echoService.queryShortVideoRedBagStatus(mChannel);
            System.out.println(response);
        } catch (RpcException ex) {
        }
        rpcClient.stop();

//        // async call
//        rpcClient = new RpcClient(serviceUrl, clientOption, interceptors);
////        rpcClient = new RpcClient(serviceUrl, clientOption, interceptors);
//        RpcCallback callback = new RpcCallback<Echo.EchoResponse>() {
//            @Override
//            public void success(Echo.EchoResponse response) {
//                System.out.printf("async call EchoService.echo success, response=%s\n",
//                        response.getMessage());
//            }
//
//            @Override
//            public void fail(Throwable e) {
//                System.out.printf("async call EchoService.echo failed, %s\n", e.getMessage());
//            }
//        };
//        EchoServiceAsync asyncEchoService = BrpcProxy.getProxy(rpcClient, EchoServiceAsync.class);
//        try {
//            Future<Echo.EchoResponse> future = asyncEchoService.echo(request, callback);
//            try {
//                Echo.EchoResponse response = future.get(1000, TimeUnit.MILLISECONDS);
//                System.out.println("response from future:" + response.getMessage());
//            } catch (Exception ex1) {
//                ex1.printStackTrace();
//            }
//        } catch (RpcException ex) {
//            System.out.println("rpc send failed, ex=" + ex.getMessage());
//        }
        rpcClient.stop();
    }

}
