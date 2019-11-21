package com.yy.lite.brpc.servlet;

import com.baidu.brpc.client.RpcClient;
import com.baidu.brpc.exceptions.RpcException;
import com.baidu.brpc.protocol.Protocol;
import com.baidu.brpc.protocol.ProtocolManager;
import com.baidu.brpc.protocol.Request;
import com.baidu.brpc.protocol.Response;
import com.baidu.brpc.protocol.http.HttpRpcServlet;
import com.baidu.brpc.spi.ExtensionLoaderManager;
import com.yy.lite.brpc.domain.BaseResult;
import com.yy.lite.brpc.protocol.HttpRpcClientProtocol;
import com.yy.lite.brpc.spring.ClientServiceLoader;
import com.yy.lite.brpc.utils.RpcOptionsUtils;
import io.netty.handler.codec.http.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;

public class HttpRpcClientServlet extends HttpRpcServlet {
    private static final Logger LOG = LoggerFactory.getLogger(com.yy.lite.brpc.servlet.HttpRpcClientServlet.class);
    private ApplicationContext appCtx ;

    public ApplicationContext getAppCtx() {
        return appCtx;
    }

    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        appCtx = WebApplicationContextUtils.getRequiredWebApplicationContext(this.getServletContext());
    }


    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        long startTime = System.nanoTime();
        String requestUri = req.getRequestURI();
        if (requestUri == null) {
            LOG.warn("invalid request");
            resp.setStatus(404);
            return;
        }

        String encoding = req.getCharacterEncoding();
        String contentType = req.getContentType().split(";")[0];
        if (contentType == null) {
            contentType = "application/baidu.json-rpc";
        } else {
            contentType = contentType.toLowerCase();
        }

        byte[] bytes = this.readStream(req.getInputStream(), req.getContentLength());

        FullHttpRequest httpRequest = new DefaultFullHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.POST, requestUri);
        httpRequest.headers().add(HttpHeaderNames.CONTENT_TYPE, contentType);
        httpRequest.content().writeBytes(bytes);
        int protocolType = HttpRpcClientProtocol.parseProtocolType(contentType);
        ExtensionLoaderManager.getInstance().loadAllExtensions(encoding);
        Protocol protocol = ProtocolManager.getInstance().getProtocol(protocolType);
        Request request = null;
        Response response = new com.baidu.brpc.protocol.HttpResponse();
        String errorMsg = null;
        try {
            request = protocol.decodeRequest(httpRequest);
            Object result = null;
            if(request!=null&& request.getTargetMethod()!=null){
                result = request.getTargetMethod().invoke(request.getTarget(), request.getArgs());
            }else{
                result = BaseResult.METHOD_NOT_EXIST;
            }
            response.setResult(result);
            response.setRpcMethodInfo(request.getRpcMethodInfo());
            response.setLogId(request.getLogId());
            protocol.encodeResponse(request, response);
        } catch (Exception ex) {
            errorMsg = String.format("invoke method failed, msg=%s", ex.getMessage());
            LOG.error(errorMsg);
            response.setException(new RpcException(RpcException.SERVICE_EXCEPTION, errorMsg));
            BaseResult result = BaseResult.builder().code(BaseResult.INTERNAL_ERROR.getCode())
                    .msg(errorMsg).build();
            response.setResult(result);
        }

        resp.setContentType(contentType);
        resp.setCharacterEncoding(encoding);
        byte[] content = ((HttpRpcClientProtocol) protocol).encodeResponseBody(protocolType, request, response);
        resp.setContentLength(content.length);
        resp.getOutputStream().write(content);

        if (request != null) {
            long endTime = System.nanoTime();
            LOG.debug("uri={} logId={} service={} method={} elapseNs={}",
                    requestUri,
                    request.getLogId(),
                    request.getServiceName(),
                    request.getMethodName(),
                    endTime - startTime);
        }

    }

    private byte[] readStream(InputStream input, int length) throws IOException {
        byte[] bytes = new byte[length];

        int bytesRead;
        for (int offset = 0; offset < bytes.length; offset += bytesRead) {
            bytesRead = input.read(bytes, offset, bytes.length - offset);
            if (bytesRead == -1) {
                break;
            }
        }

        return bytes;
    }


}
