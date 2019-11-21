package com.yy.lite.brpc.utils;

import com.alibaba.fastjson.JSON;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.Message;
import com.google.protobuf.TextFormat.ParseException;
import com.google.protobuf.util.JsonFormat;

public class ProtoUtils {
    /**
     * json数据转换为pb对象
     */
    @SuppressWarnings("unchecked")
    public static <T> T json2pb(String json, Message.Builder builder) throws ParseException, InvalidProtocolBufferException {
        if (builder == null) {
            return null;
        }
        JsonFormat.parser().ignoringUnknownFields().merge(json, builder);
        return (T) builder.build();
    }

    /**
     * json数据转换为pb对象
     */
    public static <T> T json2pb(Object entity, Message.Builder builder) throws ParseException, InvalidProtocolBufferException {
        if (builder == null || entity == null) {
            return null;
        }
        return json2pb(JSON.toJSONString(entity), builder);
    }


    /**
     * 转换为JSON字符串，并带有默认值
     * @param message
     * @return
     * @throws InvalidProtocolBufferException
     */
    public static String toDefaultJson(Message message) throws InvalidProtocolBufferException{
        return JsonFormat.printer().includingDefaultValueFields().print(message);
    }
}