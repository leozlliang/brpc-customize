package com.yy.lite.brpc.protocol;

import com.baidu.brpc.protocol.Protocol;
import com.baidu.brpc.protocol.ProtocolFactory;
import com.yy.lite.brpc.constant.ProtocolType;

public class HttpClientJsonProtocolFactory implements ProtocolFactory {

    @Override
    public Integer getProtocolType() {
        return ProtocolType.PROTOCOL_HTTP_JSON_VALUE;
    }

    public Integer getPriority() {
        return ProtocolFactory.DEFAULT_PRIORITY + 1;
    }

    @Override
    public Protocol createProtocol(String encoding) {
        return new HttpRpcClientProtocol(getProtocolType(), encoding);
    }
}
