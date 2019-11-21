/*
 * Copyright (c) 2018 Baidu, Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.yy.lite.brpc.namming.s2s;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baidu.brpc.client.instance.ServiceInstance;
import com.baidu.brpc.naming.*;
import com.google.common.collect.Sets;
import com.yy.common.hostinfo.bean.ServerInfo;
import com.yy.common.hostinfo.bean.ServiceInstanceTag;
import com.yy.common.hostinfo.utils.ServerUtil;
import com.yy.ent.client.s2s.constants.S2SEnvBuilder;
import com.yy.ent.clients.daemon.DaemonConfig;
import com.yy.ent.clients.s2s.S2SCallback;
import com.yy.ent.clients.s2s.S2SClient;
import com.yy.ent.clients.s2s.S2SMeta;
import com.yy.ent.clients.s2s.SubFilter;
import com.yy.ent.clients.s2s.codec.S2SEntData;
import com.yy.ent.clients.s2s.util.IpUtils;
import com.yy.lite.brpc.namming.s2s.annotation.S2SNamming;
import com.yy.lite.brpc.namming.s2s.base.ProtocolType;
import com.yy.lite.brpc.namming.s2s.base.S2sDaemonClient;
import com.yy.lite.brpc.namming.s2s.base.S2sService;
import com.yy.lite.brpc.namming.s2s.base.S2sSignalHandler;
import io.netty.util.Timer;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.annotation.Annotation;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class S2sNamingService implements NamingService {
    private final static Logger logger = LoggerFactory.getLogger(S2sNamingService.class);
    private static final String SPLIT = ":";
    private static String DEFAULT_REGISTER;
    private Map<String, Map<String, Map<String, String>>> servCacheMap = new ConcurrentHashMap<>();
    private final ConcurrentMap<ServiceInstance, Map<String, List<ServiceInstance>>> notified = new ConcurrentHashMap<ServiceInstance, Map<String, List<ServiceInstance>>>();
    protected BrpcURL url;
    private int retryInterval;
    private Timer timer;

    private S2sDaemonClient daemonClient;
    private String s2sName = "";
    private String accessKey = "";
    private String protocol = "yyp";
    private boolean toRegister = true;
    private boolean hasRegister = false;

    private Set<String> subscribedSet = new HashSet<>();

    private volatile Set<ServiceInstance> serviceInstancesCache = new HashSet<>();


    public S2sNamingService(BrpcURL url) {
        this.retryInterval = url.getIntParameter(Constants.INTERVAL, Constants.DEFAULT_INTERVAL);
        this.s2sName = url.getStringParameter("accessAccount", "");
        this.accessKey = url.getStringParameter("accessKey", "");
        this.protocol = url.getStringParameter("protocol", ProtocolType.YYP.getCode());
        this.toRegister = url.getStringParameter("toRegister","true").equals("true");

        logger.info("init:" + JSONObject.toJSONString(url));

        if (StringUtils.isBlank(s2sName)) {
            throw new IllegalStateException("s2s accessName  is not blank");
        }
        if (StringUtils.isBlank(accessKey)) {
            throw new IllegalStateException("s2s accessKey is not blank");
        }

        DaemonConfig config = new DaemonConfig();
        config.setAccessAccount(s2sName);
        config.setAccessKey(accessKey);
        config.setReadyTime(1000);
        config.setEnableIntranet("true");
        try {
            daemonClient = S2sService.init(config);
            //最后一个s2s注册中心作为客户端注册中心， 在多s2s注册中心时有用
            DEFAULT_REGISTER = s2sName;
        } catch (Exception e) {
            logger.error("init s2s client error", e);
            throw new IllegalStateException(e);
        }
    }

    @Override
    public List<ServiceInstance> lookup(SubscribeInfo subscribeInfo) {
        return new ArrayList<ServiceInstance>();
    }

    @Override
    public void subscribe(SubscribeInfo subscribeInfo, final NotifyListener listener) {
        logger.info("subscribe:" + JSONObject.toJSONString(subscribeInfo));


        //只需要一个s2s注册中心，进行订阅服务
        if (!s2sName.equals(DEFAULT_REGISTER)) {
            logger.info(s2sName + " is not default register,do nothing.");
            return;
        }

        String servName = subscribeInfo.getServiceId();
        S2SNamming s2SNamming = getAnnationByClass(subscribeInfo.getInterfaceName(), S2SNamming.class);
        if(s2SNamming!=null && StringUtils.isNotBlank(s2SNamming.name())){
            servName = s2SNamming.name();
        }
        final String serverNameFinal = servName;
        String servProtocol = protocol;

        ProtocolType protocolType = ProtocolType.create(protocol);
        if (protocolType == null) {
            throw new RuntimeException("protocol is unsupported. serverName:" + servName + ",protocol:" + servProtocol);
        }

        if (subscribedSet.contains(servName)) {
            logger.info(servName + " s2s name has bean already subscribed.");
            return;
        }

        //客户端
        S2SEnvBuilder builder = new S2SEnvBuilder();
        builder.setMetaServer(daemonClient.getConfig().getMetaServer());

        S2SClient client = new S2SClient(new S2SCallback() {
            @Override
            public void onAdd(S2SMeta meta) {//do nothing
            }

            @Override
            public void onBind() {//never happen
            }

            @Override
            public void onRefresh(S2SMeta[] metas) {
                // 刷新
                List<ServiceInstance> currentServiceList = getURLListFromS2s(metas, "listener", protocolType);
                Set<ServiceInstance> currentServiceSet = Sets.newHashSet(currentServiceList);
                Set<ServiceInstance> addList = Sets.difference(currentServiceSet,serviceInstancesCache);
                Set<ServiceInstance> deleteList = Sets.difference(serviceInstancesCache,currentServiceSet);
                logger.info("{}:s2slist:currentServiceList:{}",serverNameFinal,currentServiceList);
                logger.info("{}:s2slist:serviceInstancesCache:{}",serverNameFinal,serviceInstancesCache);
                logger.info("{}:s2slist:addList:{}",serverNameFinal,addList);
                logger.info("{}:s2slist:deleteList:{}",serverNameFinal,deleteList);
                listener.notify(addList, deleteList);
                serviceInstancesCache = currentServiceSet;
                logger.info("{}:s2slist:after_serviceInstancesCache:{}",serverNameFinal,serviceInstancesCache);
            }

            @Override
            public void onRemove(S2SMeta meta) {
                //do nothing
            }
        }, builder);

        client.initialize(daemonClient.getConfig().getAccessAccount(), daemonClient.getConfig().getAccessKey());


        List<SubFilter> subFilters = new ArrayList<>();
        SubFilter subFilter = new SubFilter();
        subFilter.setInterestedName(servName);
        subFilters.add(subFilter);
        client.subscribe(subFilters);


        synchronized (this) {
            subscribedSet.add(servName);
        }

        List<S2SMeta> list = client.getServicesByName(servName);
        if (list == null || list.isEmpty()) {
            try {
                for (int i = 0; i < 10; i++) {
                    Thread.sleep(1000);
                    list = client.getServicesByName(servName);
                    if (list != null && !list.isEmpty()) {
                        break;
                    }
                }
            } catch (InterruptedException e) {
                logger.error("client.getServicesByName error", e);
            }
        }
        if (CollectionUtils.isNotEmpty(list)) {
            S2SMeta[] result = new S2SMeta[list.size()];
            list.toArray(result);
            // 刷新
            List<ServiceInstance> addList = getURLListFromS2s(result, "direct", protocolType);
            List<ServiceInstance> deleteList = new ArrayList<>();
            listener.notify(addList, deleteList);
//                serviceInstancesCache = addList ;
        } else {
            logger.info("get service from s2s is empty");
        }
    }

    @Override
    public void unsubscribe(SubscribeInfo subscribeInfo) {
        logger.info("unsubscribe:" + JSONObject.toJSONString(subscribeInfo));
    }

    @Override
    public void register(RegisterInfo registerInfo) {
        if(!this.toRegister){
            logger.info("s2sName={} not to register ",this.s2sName);
            return ;
        }
        logger.info("register:" + JSONObject.toJSONString(url));

        ProtocolType protocolType = ProtocolType.create(protocol);
        if (protocolType == null) {
            throw new RuntimeException("protocol is unsupported. protocol:" + protocol);
        }
        if (daemonClient == null) {
            throw new RuntimeException("s2s client is null.s2sName:" + s2sName);
        }

        Map<Integer, Integer> protocolMap = new HashMap<>();
        int port = registerInfo.getPort();
        protocolMap.put(protocolType.getVal(), port);

        if (!hasRegister) {
            ServerInfo serverInfo = ServerUtil.getServerInfoByHostInfo();
            Map<String, Object> extMap = new HashMap<>(8);
            extMap.put("areaId", serverInfo.getAreaId());
            extMap.put("regionId", serverInfo.getRegionId());
            extMap.put("roomId", serverInfo.getRoomId());
            daemonClient.registerToDaemon(protocolType, port, daemonClient.getConfig().getGroupId(), protocolMap, extMap);

            //注册新号，用于优雅停机
            new S2sSignalHandler(daemonClient).registerSignal();
            hasRegister = true;
            logger.info(String.format("interface[%s],protocal[%s],port[%s],s2sName[%s] do s2s register.", accessKey, protocol, port, s2sName));
        } else {
            logger.info(String.format("interface[%s],protocal[%s],port[%s],s2sName[%s] had been register.", accessKey, protocol, port, s2sName));
        }
    }

    @Override
    public void unregister(RegisterInfo registerInfo) {
        logger.info("unregister:" + JSONObject.toJSONString(url));
    }

    private List<ServiceInstance> getURLListFromS2s(S2SMeta[] metas, String tag, ProtocolType protocolType) {
        List<ServiceInstance> result = new ArrayList<ServiceInstance>();
        for (S2SMeta meta : metas) {
            logger.info("====================fresh start " + tag + "========================");
            String servName = meta.getName();
            if (servName != null && !"".equals(servName.trim())) {
                S2SEntData s2sEntData = S2SEntData.readFromByteArray(meta.getData());

                Integer port = null;
                if (s2sEntData.getProtocol() != null && !s2sEntData.getProtocol().isEmpty()) {
                    //兼容anka等一个s2sName 多种协议
                    port = s2sEntData.getProtocol().get(protocolType.getVal());
                }
                //新框架使用tcp_port
                port = port == null ? s2sEntData.getTcp_port() : port;
                if (port == null || port == 0) {
                    logger.info(servName + " not found available port.");
                }
                if (s2sEntData.getIpList() == null || s2sEntData.getIpList().isEmpty()) {
                    continue;
                }
                Object setid = null;
                Map<String, Map<String, String>> ipMap = servCacheMap.computeIfAbsent(servName, k -> new ConcurrentHashMap<>());
                for (Integer ipType : s2sEntData.getIpList().keySet()) {
                    Long ip = s2sEntData.getIpList().get(ipType);

                    String ipStr = IpUtils.longToIp(ip);
                    if ("127.0.0.1".equals(ipStr)) {
                        continue;
                    }
                    if (0 == meta.getMetaStatus()) {
                        //新加入服务器
                        Map<String, String> infoMap = new HashMap<>();
                        ServiceInstanceTag tagInfo = new ServiceInstanceTag();
                        tagInfo.setIspId(ipType);
                        tagInfo.setGroupId(meta.getGroupId());
                        int roomId = 0;
                        int areaId = 0;
                        int regionId = 0;
                        if (s2sEntData.getParamMap() != null && s2sEntData.getParamMap().containsKey("roomId")) {
                            roomId = (int) s2sEntData.getParamMap().get("roomId");
                        }
                        if (s2sEntData.getParamMap() != null && s2sEntData.getParamMap().containsKey("areaId")) {
                            areaId = (int) s2sEntData.getParamMap().get("areaId");
                        }
                        if (s2sEntData.getParamMap() != null && s2sEntData.getParamMap().containsKey("regionId")) {
                            regionId = (int) s2sEntData.getParamMap().get("regionId");
                        }
                        tagInfo.setRoomId(roomId);
                        tagInfo.setAreaId(areaId);
                        tagInfo.setRegionId(regionId);
                        infoMap.put("tagInfo", JSON.toJSONString(tagInfo));
                        ipMap.put(ipStr + SPLIT + port, infoMap);
                        logger.info(String.format("subscribe service[add], name %s, port %s, ip %s, group %s, setid %s, time %s", servName, port, ipStr, meta.getGroupId(), setid, meta.getTimestamp()));
                    } else if (1 == meta.getMetaStatus()) {
                        //要移除的服务器
                        ipMap.remove(ipStr + SPLIT + port);
                        logger.info(String.format("subscribe service[remove], name %s, port %s, ip %s, group %s, setid %s, time %s", servName, port, ipStr, meta.getGroupId(), setid, meta.getTimestamp()));
                    } else {
                        logger.info("unknow status[" + servName + "],status:" + meta.getMetaStatus() + "========================");
                    }
                }
                for (String key : ipMap.keySet()) {
                    Map<String, String> value = ipMap.get(key);
                    if (value == null) {
                        continue;
                    }
                    String[] ipInfo = key.split(SPLIT);
                    ServiceInstance serviceInstance = new ServiceInstance();
                    serviceInstance.setIp(ipInfo[0]);
                    serviceInstance.setPort(NumberUtils.toInt(ipInfo[1]));
                    serviceInstance.setTag(value.get("tagInfo"));
                    logger.info(servName + ",serviceInstance:" + serviceInstance);

                    result.add(serviceInstance);
                }
            }
        }
        logger.info("====================fresh  end " + tag + "========================");
        return result;
    }


    private <A extends Annotation> A getAnnationByClass(String clazz, Class<A> annation){
        if(StringUtils.isBlank(clazz) || annation==null){
            return null;
        }
        try{

            return Thread.currentThread().getContextClassLoader().loadClass(clazz).getAnnotation(annation);
        }catch (Exception e){
            return null;
        }
    }


}
