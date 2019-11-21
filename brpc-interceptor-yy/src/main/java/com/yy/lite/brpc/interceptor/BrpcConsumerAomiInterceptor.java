package com.yy.lite.brpc.interceptor;

import com.yy.aomi.sdk.core.Trace;
import com.yy.aomi.sdk.core.TraceContext;
import com.yy.aomi.sdk.event.EventConverterHolder;
import com.yy.aomi.sdk.tracecontext.HandleEventTraceContext;
import com.yy.aomi.sdk.tracecontext.RpcMsgTraceContext;
import com.yy.lite.brpc.interceptor.AbstractBrpcAomiInterceptor;
import com.yy.lite.brpc.interceptor.domain.BrpcHandleTraceContext;
import com.yy.lite.brpc.interceptor.domain.BrpcTraceInvokeContext;

public class BrpcConsumerAomiInterceptor extends AbstractBrpcAomiInterceptor {
    public BrpcConsumerAomiInterceptor() {
        this.isConsumerSide = Boolean.TRUE;
    }

    protected Trace startTrace(BrpcTraceInvokeContext invokeContext) {
        Trace trace = null;

        try {
            RpcMsgTraceContext e = new RpcMsgTraceContext(invokeContext.getUri(), invokeContext.getProtocol());
            BrpcHandleTraceContext handleTraceContext = new BrpcHandleTraceContext();
            trace = this.tracer.start(e.getKey(), new TraceContext[]{e, handleTraceContext});
        } catch (Throwable var5) {
            this.logger.warn("consumerTraceFilter startTrace error", var5);
        }

        return trace;
    }

    protected void onError(Trace trace, Throwable ex) {
        if(trace != null) {
            String eventKey = EventConverterHolder.toEvent(ex);
            trace.addContext(new HandleEventTraceContext(eventKey));
        }

    }
}