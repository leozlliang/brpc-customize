package com.yy.lite.brpc.client;

import com.baidu.brpc.RpcMethodInfo;
import com.baidu.brpc.client.MethodUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by huwenwei on 2017/4/25.
 */
@SuppressWarnings("unchecked")
public class ClientServiceManager {
    private static final Logger LOG = LoggerFactory.getLogger(ClientServiceManager.class);
    private static volatile ClientServiceManager instance;

    private Map<String, RpcMethodInfo> serviceMap;

    public static ClientServiceManager getInstance() {
        if (instance == null) {
            synchronized (ClientServiceManager.class) {
                if (instance == null) {
                    instance = new ClientServiceManager();
                }
            }
        }
        return instance;
    }

    private ClientServiceManager() {
        this.serviceMap = new HashMap<String, RpcMethodInfo>();
    }



    public void registerService(Class infc, Object service) {
        Class clazz = infc;
        Method[] methods = clazz.getDeclaredMethods();
        ClientServiceManager serviceManager = ClientServiceManager.getInstance();
        for (Method method : methods) {
            RpcMethodInfo serviceInfo = MethodUtils.getRpcMethodInfo(infc,method.getName());
            serviceInfo.setTarget(service);
            serviceInfo.setMethod(method);
            serviceManager.registerService(serviceInfo);
            LOG.info("register client service, serviceName={}, methodName={}",
                    serviceInfo.getServiceName(), serviceInfo.getMethodName());
        }
    }


    protected void registerService(RpcMethodInfo methodInfo) {
        String key = buildServiceKey(methodInfo.getServiceName(), methodInfo.getMethodName());
        serviceMap.put(key, methodInfo);
    }

    public RpcMethodInfo getService(String serviceName, String methodName) {
        String key = buildServiceKey(serviceName, methodName);
        return serviceMap.get(key);
    }

    public RpcMethodInfo getService(String serviceMethodName) {
        return serviceMap.get(serviceMethodName);
    }

    public Map<String, RpcMethodInfo> getServiceMap() {
        return serviceMap;
    }


    private String buildServiceKey(String serviceName, String methodName) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(serviceName.toLowerCase()).append(".").append(methodName);
        return stringBuilder.toString();
    }
}
