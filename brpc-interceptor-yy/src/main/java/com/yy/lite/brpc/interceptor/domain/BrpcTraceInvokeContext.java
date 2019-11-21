package com.yy.lite.brpc.interceptor.domain;

import lombok.Data;

@Data
public class BrpcTraceInvokeContext {

    private String application;
    private String protocol = "yyp";
    private String uri;
    private String service;
    private String method;
    private int localPort;
    private String remoteKey;
    private String remoteValue;
    private String input;
    private String output;
    private String ip;
    private int port;

}