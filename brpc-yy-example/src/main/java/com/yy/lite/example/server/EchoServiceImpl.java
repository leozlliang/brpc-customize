package com.yy.lite.example.server;

import com.baidu.brpc.spring.annotation.RpcExporter;
import com.baidu.brpc.spring.annotation.RpcProxy;
import com.yy.anka.io.codec.protocol.bean.MChannel;
import com.yy.lite.brpc.example.api.EchoRequest;
import com.yy.lite.brpc.example.api.EchoResponse;
import com.yy.lite.brpc.example.api.EchoService;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author donghonghua
 * @date 2019/7/30
 */
@RpcExporter
public class EchoServiceImpl implements EchoService {

    @RpcProxy
    @Override
    public EchoResponse echo(EchoRequest request) {
        System.out.println(request.getMessage());
        EchoResponse response = new EchoResponse();
        response.setMessage("echo");
        return response;
    }

    @Override
    public EchoResponse echo2(MChannel<EchoRequest> request) {
        System.out.println(request.payload.getMessage());
        EchoResponse response = new EchoResponse();
        response.setMessage("echo2");
        return response;
    }
}
