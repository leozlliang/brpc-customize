package com.yy.lite.brpc.domain;

import lombok.Builder;

@Builder
public class BaseResult {
    public final static BaseResult METHOD_NOT_EXIST = BaseResult.builder().code(-1).msg("method not exist").build();
    public final static BaseResult INTERNAL_ERROR = BaseResult.builder().code(-2).msg("internal error").build();
    public final static BaseResult  INVALID_TOKEN = BaseResult.builder().code(-3).msg("invalid token").build();
    private int code;
    private String msg;

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }
}