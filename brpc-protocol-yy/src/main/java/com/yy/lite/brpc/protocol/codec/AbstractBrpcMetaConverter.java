package com.yy.lite.brpc.protocol.codec;

import com.baidu.brpc.RpcMethodInfo;
import com.yy.anka.io.rpc.parse.RPCInfo;


/**
 * @author donghonghua
 * @date 2019/7/26
 */
public abstract class AbstractBrpcMetaConverter<T extends RPCInfo> implements BrpcMetaConverter<T> {

    RpcMethodInfo getService(String serviceName, String methodName) {
        YypProtocolManager protocolManager = YypProtocolManager.getInstance();
        return protocolManager.findRpcMethodInfo(serviceName, methodName);
    }

}
