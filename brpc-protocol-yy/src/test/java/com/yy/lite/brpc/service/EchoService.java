package com.yy.lite.brpc.service;

import com.baidu.brpc.protocol.BrpcMeta;
import com.yy.anka.io.codec.protocol.bean.MChannel;

/**
 * @author donghonghua
 * @date 2019/7/23
 */
public interface EchoService {
    /**
     * brpc/sofa：
     * serviceName默认是包名 + 类名，methodName是proto文件Service内对应方法名，
     * hulu/public_pbrpc：
     * serviceName默认是类名，不需要加包名，methodName是proto文件Service内对应方法index，默认从0开始。
     */
    @BrpcMeta(serviceName = "__" + (9 << 8 | 278), methodName = "" + (10 << 8 | 278))
    AResponse echo(ARequest request);

    @BrpcMeta(serviceName = "464__7134_1041", methodName = "7134_1042")
    VideoTaskShow.VideoTaskShowResp queryShortVideoRedBagStatus(MChannel<VideoTaskShow.VideoTaskShowReq> req);
}
