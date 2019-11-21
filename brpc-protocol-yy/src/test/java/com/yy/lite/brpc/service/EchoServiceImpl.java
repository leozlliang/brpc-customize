package com.yy.lite.brpc.service;

import com.yy.anka.io.codec.protocol.bean.MChannel;

import java.util.Random;

/**
 * @author donghonghua
 * @date 2019/7/23
 */
public class EchoServiceImpl implements EchoService {

    @Override
    public AResponse echo(ARequest request) {

        System.out.println(request.getName());
        AResponse response = new AResponse();
        response.setResult("success");
        return response;
    }

    @Override
    public VideoTaskShow.VideoTaskShowResp queryShortVideoRedBagStatus(MChannel<VideoTaskShow.VideoTaskShowReq> req) {
        VideoTaskShow.VideoTaskShowResp.Builder rspBuilder = VideoTaskShow.VideoTaskShowResp.newBuilder();
        rspBuilder.setShow(1);
        try {
            Thread.sleep(new Random().nextInt(300));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        rspBuilder.setSkipUrl(String.valueOf(req.uid));
        System.out.println("hello");
        return rspBuilder.build();
    }
}
