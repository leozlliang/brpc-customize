package com.yy.lite.brpc.protocol.codec;

import com.baidu.brpc.RpcMethodInfo;
import com.yy.anka.io.rpc.parse.SrvRPCInfo;

/**
 * @author donghonghua
 * @date 2019/7/26
 */
public class SrvRpcInfoBrpcMetaConverter extends AbstractBrpcMetaConverter<SrvRPCInfo> {
    @Override
    public RpcMethodInfo getRpcMethodInfoByReqRPCInfo(SrvRPCInfo req) {
        String serviceName = req.getUri() + "__" + req.getMax() + "_" + req.getMin();
        return getService(serviceName, null);
    }

    @Override
    public RpcMethodInfo getRpcMethodInfoByRespRPCInfo(SrvRPCInfo resp) {
        String methodName = resp.getMax() + "_" + resp.getMin();
        return getService(null, methodName);
    }

}
