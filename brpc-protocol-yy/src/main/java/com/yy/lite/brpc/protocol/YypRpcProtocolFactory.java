package com.yy.lite.brpc.protocol;

import com.baidu.brpc.protocol.Protocol;
import com.baidu.brpc.protocol.ProtocolFactory;

/**
 * @author donghonghua
 *
 */
public class YypRpcProtocolFactory implements ProtocolFactory {

    @Override
    public Integer getProtocolType() {
        return CustomProtocolTypeEnum.YYP.type;
    }

    @Override
    public Integer getPriority() {
        return ProtocolFactory.DEFAULT_PRIORITY - 2;
    }

    @Override
    public Protocol createProtocol(String encoding) {
        return new YypRpcProtocol();
    }

}
