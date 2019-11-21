package com.yy.lite.brpc.protocol.codec;

import com.baidu.brpc.RpcMethodInfo;
import com.yy.anka.io.rpc.parse.ServiceRPCInfo;

/**
 * @author donghonghua
 * @date 2019/7/26
 */
public class ServiceRpcInfoBrpcMetaConverter extends AbstractBrpcMetaConverter<ServiceRPCInfo> {

    @Override
    public RpcMethodInfo getRpcMethodInfoByReqRPCInfo(ServiceRPCInfo req) {
        return doGetRpcMethodInfo(req);
    }

    @Override
    public RpcMethodInfo getRpcMethodInfoByRespRPCInfo(ServiceRPCInfo resp) {
        return doGetRpcMethodInfo(resp);
    }

    private RpcMethodInfo doGetRpcMethodInfo(ServiceRPCInfo info) {
        String serviceName = info.getServiceName();
        String methodName = info.getFunctionName();
        return this.getService(serviceName, methodName);
    }
}
