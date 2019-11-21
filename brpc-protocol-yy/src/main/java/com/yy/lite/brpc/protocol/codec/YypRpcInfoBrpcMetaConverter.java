package com.yy.lite.brpc.protocol.codec;

import com.baidu.brpc.RpcMethodInfo;
import com.yy.anka.io.rpc.parse.YypRPCInfo;

/**
 * @author donghonghua
 * @date 2019/7/26
 */
public class YypRpcInfoBrpcMetaConverter extends AbstractBrpcMetaConverter<YypRPCInfo> {
    @Override
    public RpcMethodInfo getRpcMethodInfoByReqRPCInfo(YypRPCInfo req) {
        String serviceName = "__" + req.getUri();
        return getService(serviceName, null);
    }

    @Override
    public RpcMethodInfo getRpcMethodInfoByRespRPCInfo(YypRPCInfo resp) {
        return getService(null, String.valueOf(resp.getUri()));
    }

}
