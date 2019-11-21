package com.yy.lite.example.schedule;

import com.baidu.brpc.spring.annotation.RpcProxy;
import com.yy.anka.io.codec.protocol.bean.MChannel;
import com.yy.lite.brpc.example.api.EchoRequest;
import com.yy.lite.brpc.example.api.EchoResponse;
import com.yy.lite.brpc.example.api.EchoService;
import com.yy.lite.userinfo.api.UserInfoService;
import com.yy.lite.userinfo.bean.proto.UserInfo;
import org.assertj.core.util.Lists;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * @author donghonghua
 * @date 2019/7/30
 */
@Component
public class TestJob {

    @RpcProxy(name = "brpc_example")
    private EchoService echoService;

    @RpcProxy(name = "zk_userinfo")
    private UserInfoService userInfoService;

    @Scheduled(fixedRate = 5000)
    public void execute() {
        UserInfo.BatchGetUserAllInfoReq.Builder builder = UserInfo.BatchGetUserAllInfoReq.newBuilder();
        builder.setLogId(1)
                .addUids(50019899L);

        UserInfo.BatchGetUserAllInfoResp resp = userInfoService.batchGetUserAllInfo(builder.build());
        System.out.println("resp = " + resp);

        EchoRequest request = new EchoRequest();
        request.setMessage("echo");
        EchoResponse response = echoService.echo(request);
        System.out.println(response.getMessage());

        EchoRequest request2 = new EchoRequest();
        request.setMessage("echo2");
        MChannel<EchoRequest> mChannel = new MChannel<>();
        mChannel.setPayload(request2);
        EchoResponse response2 = echoService.echo2(mChannel);
        System.out.println(response2.getMessage());
    }
}
