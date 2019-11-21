package com.yy.lite.brpc.spring;

import com.baidu.brpc.client.BrpcProxy;
import com.baidu.brpc.client.RpcClient;
import com.baidu.brpc.client.RpcClientOptions;
import com.google.common.base.Predicate;
import com.yy.lite.brpc.client.ClientServiceManager;
import com.yy.lite.brpc.namming.s2s.annotation.S2SNamming;
import com.yy.lite.brpc.utils.*;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.ResourceUtils;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Set;

public class ClientServiceLoader {
    private static final Logger LOG = LoggerFactory.getLogger(ClientServiceLoader.class);

    public static void loadAll(String pkgPath, String s2sUrl){
        /*
        String jarDownloadPath =  System.getProperty("user.dir") + File.separator+ "tempJarPath";
        URL[] urls = null;
        try {
            HttpUtil.downLoadFromUrl("http://nexus.yy.com/music/content/repositories/yyent/com/yy/quesgo/quesgo-userservice-api/0.0.1-SNAPSHOT/quesgo-userservice-api-0.0.1-20190911.065700-6.jar",jarDownloadPath);
            HttpUtil.downLoadFromUrl("http://nexus.yy.com/music/content/repositories/yyent/com/yy/quesgo/quesgo-question-api/0.0.1-SNAPSHOT/quesgo-question-api-0.0.1-20190914.032407-27.jar",jarDownloadPath);
            urls = JarPathClassLoader.getURLsByJarRoot(jarDownloadPath);
        } catch (IOException e) {

        }
        JarPathClassLoader pkgUtil = new JarPathClassLoader(urls,pkgPath);
        Set<Class<?>> classSet =  pkgUtil.getClzFromPkg(new Predicate<Class>() {
        */
        Set<Class<?>> classSet =  PkgUtil.getClzFromPkg(pkgPath,new Predicate<Class>() {
            @Override
            public boolean apply( Class aClass) {
                return  aClass.isInterface() && aClass.getAnnotation(S2SNamming.class)!=null;
            }
        });
        //Thread.currentThread().setContextClassLoader(pkgUtil);

        for(Class clazz : classSet){
            RpcClientOptions options = new RpcClientOptions();
            S2SNamming clientParams = (S2SNamming)clazz.getAnnotation(S2SNamming.class);
            if(clientParams!=null && clientParams.soReadTimeout()>0){
                LOG.info("{} S2SNamming name:{},soReadTimeout:{}",clazz,clientParams.name(),clientParams.soReadTimeout());
                options.setReadTimeoutMillis(clientParams.soReadTimeout());
            }
            RpcClient bpcClient = new RpcClient(s2sUrl,options);
            Object proxy =  BrpcProxy.getProxy(bpcClient, clazz);
            ClientServiceManager.getInstance().registerService(clazz,proxy);
        }

    }


    public static void main(String[] args) {
        loadAll("com.yy","aaaa");
    }
}