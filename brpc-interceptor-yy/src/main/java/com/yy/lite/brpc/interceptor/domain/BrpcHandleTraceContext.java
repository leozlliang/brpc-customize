package com.yy.lite.brpc.interceptor.domain;

import com.yy.aomi.sdk.tracecontext.HandleTraceContext;

public class BrpcHandleTraceContext extends HandleTraceContext {
    public static final String BRPC_THREADPOOL_MODEL_KEY = "brpc_work_threadpool";

    public BrpcHandleTraceContext() {
        this(BRPC_THREADPOOL_MODEL_KEY);
    }

    public BrpcHandleTraceContext(String handleId) {
        super(handleId);
    }
}