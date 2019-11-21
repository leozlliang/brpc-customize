package com.yy.lite.brpc.interceptor;
import com.baidu.brpc.interceptor.AbstractInterceptor;
import com.baidu.brpc.interceptor.InterceptorChain;
import com.baidu.brpc.protocol.Request;
import com.baidu.brpc.protocol.Response;
import com.yy.sv.base.util.MetricsClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BrpcMetricsInterceptor  extends AbstractInterceptor {
    private static final Logger LOGGER = LoggerFactory.getLogger(BrpcMetricsInterceptor.class);
    private static final int CODE_OK = 0;
    private static final int CODE_ERROR = 1;

    public static final String LOG_PROCESS_FINISH = "Brpc client method {} invoke finish, cost time:{},req:{},resp:{}";
    public static final String LOG_PROCESS_ERROR= "Brpc client method {} invoke error, cost time:{},args:{}";


    @Override
    public void aroundProcess(Request request, Response response, InterceptorChain chain) throws Exception {
        String method = request.getServiceName()+"_"+request.getMethodName();
        long startTime = System.currentTimeMillis();
        int metricsCode = CODE_OK;

        super.aroundProcess(request, response, chain);

        long costTime = System.currentTimeMillis() - startTime;
        MetricsClient.reportDataInner(method, metricsCode, costTime);
        if(response.getException()!=null){
            metricsCode = CODE_ERROR;
        }
        if( metricsCode == CODE_OK){
            LOGGER.info(LOG_PROCESS_FINISH, method, costTime,request.getArgs(),"");
        }else{
            LOGGER.error(LOG_PROCESS_ERROR, method, costTime,request.getArgs());
        }
    }

}