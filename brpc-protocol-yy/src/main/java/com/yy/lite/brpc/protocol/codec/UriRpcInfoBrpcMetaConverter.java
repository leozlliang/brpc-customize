package com.yy.lite.brpc.protocol.codec;

import com.baidu.brpc.RpcMethodInfo;
import com.yy.anka.io.rpc.parse.UriRPCInfo;

/**
 * @author donghonghua
 * @date 2019/7/26
 */
public class UriRpcInfoBrpcMetaConverter extends AbstractBrpcMetaConverter<UriRPCInfo> {
    @Override
    public RpcMethodInfo getRpcMethodInfoByReqRPCInfo(UriRPCInfo req) {
        return doGetRpcMethodInfo(req);
    }

    @Override
    public RpcMethodInfo getRpcMethodInfoByRespRPCInfo(UriRPCInfo resp) {
        return doGetRpcMethodInfo(resp);
    }

    private RpcMethodInfo doGetRpcMethodInfo(UriRPCInfo info) {
        String serviceName = info.getServiceName();
        String methodName = info.getFunctionName();
        return getService(serviceName, methodName);
    }
}
