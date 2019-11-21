package com.yy.lite.brpc.interceptor;

import com.baidu.brpc.interceptor.AbstractInterceptor;
import com.baidu.brpc.interceptor.InterceptorChain;
import com.baidu.brpc.protocol.Request;
import com.baidu.brpc.protocol.Response;
import com.yy.aomi.sdk.core.Trace;
import com.yy.aomi.sdk.core.TraceContext;
import com.yy.aomi.sdk.core.Tracer;
import com.yy.aomi.sdk.core.TracerHolder;
import com.yy.lite.brpc.interceptor.domain.BrpcTraceInvokeContext;
import io.netty.channel.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;

public abstract class AbstractBrpcAomiInterceptor extends AbstractInterceptor {
    protected final Logger logger = LoggerFactory.getLogger(this.getClass());
    protected Tracer tracer = TracerHolder.getTracer();
    protected Boolean isConsumerSide;


    public void aroundProcess(Request request, Response response, InterceptorChain chain) throws Exception {
        BrpcTraceInvokeContext traceCtx = this.initContext(request);
        Trace trace = this.startTrace(traceCtx);
        try {
            request.setTraceId(trace.getTraceId());
            super.aroundProcess(request, response, chain);
            this.afterInvoke(trace, traceCtx);
        } catch (Exception var10) {
            if(trace != null) {
                this.onError(trace, var10);
            }
            throw var10;
        } finally {
            this.endTrace(trace);
        }

    }

    protected BrpcTraceInvokeContext initContext(Request request) {
        String remoteKey = "provider";
        BrpcTraceInvokeContext invokeContext = new BrpcTraceInvokeContext();
        Channel channel = request.getChannel();
        if(channel!=null){
            remoteKey = "consumer";
            String remoteHost =  channel.remoteAddress().toString();
            invokeContext.setIp(remoteHost);
            InetSocketAddress localAddress = (InetSocketAddress)channel.localAddress();
            InetSocketAddress remoteAddress = (InetSocketAddress)channel.remoteAddress();
            invokeContext.setIp(remoteHost);
            invokeContext.setIp(remoteAddress.getHostString());
            invokeContext.setPort(remoteAddress.getPort());
            invokeContext.setLocalPort(localAddress.getPort());
            int localPort = remoteAddress.getPort();
            invokeContext.setLocalPort(localPort);
            String remoteValue = remoteAddress.getHostName();
            invokeContext.setRemoteValue(remoteValue);
        }
        invokeContext.setRemoteKey(remoteKey);
        String application = request.getClientName();
        String service = request.getServiceName();
        String method = request.getMethodName();
        invokeContext.setApplication(application);
        invokeContext.setService(service);
        invokeContext.setMethod(method);

        return invokeContext;
    }

    protected void afterInvoke(Trace trace, BrpcTraceInvokeContext traceCtx) {
    }

    protected void endTrace(Trace trace) {
        this.tracer.end(trace, new TraceContext[0]);
    }

    protected abstract Trace startTrace(BrpcTraceInvokeContext var1);

    protected abstract void onError(Trace var1, Throwable var2);

}