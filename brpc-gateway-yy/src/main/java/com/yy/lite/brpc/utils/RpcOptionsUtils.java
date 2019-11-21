package com.yy.lite.brpc.utils;

import com.baidu.brpc.client.RpcClientOptions;
import com.baidu.brpc.server.RpcServerOptions;

public class RpcOptionsUtils {
    public static RpcClientOptions getRpcClientOptions() {
        RpcClientOptions options = new RpcClientOptions();
        options.setReadTimeoutMillis(10000);
        options.setHealthyCheckIntervalMillis(20000);
//        options.setIoThreadNum(8);
//        options.setWorkThreadNum(50);
//        options.setMinIdleConnections(300);
        return options;
    }

    public static RpcServerOptions getRpcServerOptions() {
        RpcServerOptions options = new RpcServerOptions();
        options.setIoThreadNum(1);
        options.setWorkThreadNum(1);
        return options;
    }
}
