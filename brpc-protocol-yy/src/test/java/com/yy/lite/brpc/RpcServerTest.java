package com.yy.lite.brpc;

import com.baidu.brpc.protocol.ProtocolManager;
import com.baidu.brpc.server.RpcServer;
import com.baidu.brpc.server.RpcServerOptions;
import com.yy.lite.brpc.protocol.CustomProtocolTypeEnum;
import com.yy.lite.brpc.service.EchoServiceImpl;

/**
 * @author donghonghua
 * @date 2019/7/23
 */
public class RpcServerTest {

    public static void main(String[] args) throws InterruptedException {
        int port = 8002;
        if (args.length == 1) {
            port = Integer.valueOf(args[0]);
        }

        RpcServerOptions options = new RpcServerOptions();
        options.setReceiveBufferSize(64 * 1024 * 1024);
        options.setSendBufferSize(64 * 1024 * 1024);
//        options.setProtocolType(CustomProtocolTypeEnum.YYP.type);
        final RpcServer rpcServer = new RpcServer(port, options);
        rpcServer.registerService(new EchoServiceImpl());
        rpcServer.start();
        // make server keep running
        synchronized (RpcServerTest.class) {
            try {
                RpcServerTest.class.wait();
            } catch (Throwable e) {
            }
        }
    }
}
