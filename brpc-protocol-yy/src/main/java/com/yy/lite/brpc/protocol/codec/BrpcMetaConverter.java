package com.yy.lite.brpc.protocol.codec;

import com.baidu.brpc.RpcMethodInfo;
import com.yy.anka.io.rpc.parse.RPCInfo;

/**
 * @author donghonghua
 * @date 2019/7/26
 */
public interface BrpcMetaConverter<T extends RPCInfo> {

    RpcMethodInfo getRpcMethodInfoByReqRPCInfo(T req);

    RpcMethodInfo getRpcMethodInfoByRespRPCInfo(T resp);

}
