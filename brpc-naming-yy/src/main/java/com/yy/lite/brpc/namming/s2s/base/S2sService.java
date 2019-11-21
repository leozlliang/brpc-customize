package com.yy.lite.brpc.namming.s2s.base;

import com.yy.ent.clients.daemon.*;
import com.yy.ent.clients.daemon.listener.ServerArrivalListener;
import com.yy.ent.clients.daemon.listener.ServerRemoveListener;
import com.yy.ent.clients.s2s.SubFilter;
import com.yy.ent.clients.s2s.exception.S2SException;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;

public class S2sService {

    private Logger m_logger = LoggerFactory.getLogger(S2sService.class);
    private  int DEFAULT_WAIT_TIME_MILLSEC = 3000;
    private  DaemonClient subClient;
    private  Map<String, DaemonClient> serverRegClientEntry = new ConcurrentHashMap<>();
    private  Map<DaemonConfig, AtomicInteger> checkRpCounts = new ConcurrentHashMap<>();

    private S2sService() {
    }

    public  DaemonClient getClient() {
        return subClient;
    }

    public  DaemonClient register(String path, Integer port, Integer groupId, Map<Integer, Integer> protocol) throws Exception {
        DaemonConfig cfg = buildConfig(path);
        DaemonClient client = register(cfg, port, groupId, protocol);
        if (client.isReady(cfg.getReadyTime())) {
            List<DaemonServerInfo> serverInfoList = client.getServiceByName(cfg.getAccessAccount());
            if (CollectionUtils.isNotEmpty(serverInfoList)) {
                m_logger.info(cfg.getAccessAccount() + " first server:" + ((DaemonServerInfo)serverInfoList.get(0)).toString());
            }

            m_logger.info("ok and serverinfo size is:  " + (null != serverInfoList ? serverInfoList.size() : 0));
        } else {
            m_logger.warn("register to daemon failed...");
        }

        m_logger.warn("end register to daemon...");
        return client;
    }

    public  DaemonClient register(String path) throws Exception {
        return register((String)path, (Integer)null, (Integer)null, (Map)null);
    }

    public  DaemonConfig buildConfig(String path) {
        String p = StringUtils.isBlank(path) ? this.getClass().getClassLoader().getResource("daemon.properties").getPath() : path;
        Properties properties = ConfigUtil.readProperties(p);
        String accessName = properties.getProperty("accessAccount");
        String accessKey = properties.getProperty("accessKey");
        int serverPort = ConfigUtil.getIntValue(properties, "regSvPort");
        int groupId = ConfigUtil.getIntValue(properties, "groupId");
        String serverArrivalListener = properties.getProperty("serverArrivalListener");
        String serverRemoveListener = properties.getProperty("serverRemoveListener");
        String metaServer = properties.getProperty("metaServer");
        String enableIntranet = properties.getProperty("enableIntranet");
        int time = ConfigUtil.getIntValue(properties, "readytime");
        int readytime = DEFAULT_WAIT_TIME_MILLSEC;
        if (time > 0) {
            readytime = time;
        }

        DaemonConfig config = new DaemonConfig();
        config.setAccessAccount(accessName);
        config.setAccessKey(accessKey);
        config.setRegSvPort(serverPort);
        config.setGroupId(groupId);
        config.setServerArrivalListener(serverArrivalListener);
        config.setServerRemoveListener(serverRemoveListener);
        config.setReadyTime(readytime);
        config.setMetaServer(metaServer);
        config.setEnableIntranet(enableIntranet);
        return config;
    }

    public  synchronized DaemonClient register(DaemonConfig config) throws Exception {
        return register((DaemonConfig)config, (Integer)null, (Integer)null, (Map)null);
    }

    public  synchronized DaemonClient register(final DaemonConfig config, final Integer port, final Integer groupId, final Map<Integer, Integer> protocol) throws Exception {
        DaemonClient client = (DaemonClient)serverRegClientEntry.get(config.getAccessAccount());
        if (client == null) {
            client = new DaemonClient(config);
            serverRegClientEntry.put(config.getAccessAccount(), client);
        }

        (new Thread(new Runnable() {
            public void run() {
                long useTime = 0L;
                DaemonClient client = (DaemonClient)serverRegClientEntry.get(config.getAccessAccount());

                try {
                    while(useTime < 2000L) {
                        if (checkDependencyServers(config)) {
                            m_logger.info("begin to registerToDaemon for config=" + config);
                            client.registerToDaemon(port, groupId, protocol);
                            break;
                        }

                        Thread.sleep(500L);
                        useTime += 500L;
                    }

                    if (getSubscribeNames().isEmpty()) {
                        m_logger.warn("start register to daemon...");
                        client.registerToDaemon(port, groupId, protocol);
                    }
                } catch (Exception var5) {
                    m_logger.error("", var5);
                }

            }
        })).start();
        return client;
    }

    public  synchronized DaemonClient init(String cfgPath) throws Exception {
        if (m_logger.isDebugEnabled()) {
            m_logger.debug("cfgPath is: " + cfgPath);
        }

        m_logger.info("daemon cfgPath init :" + cfgPath);
        DaemonConfig config = buildConfig(cfgPath);
        return init(config);
    }

    public  static  synchronized S2sDaemonClient init(DaemonConfig config) throws Exception {
        return new S2sDaemonClient(config);
    }

    private  void initServerArrivalListener(String listenerStr) {
        if (!StringUtils.isBlank(listenerStr)) {
            String[] saListeners = listenerStr.split(",");
            String[] var2 = saListeners;
            int var3 = saListeners.length;

            for(int var4 = 0; var4 < var3; ++var4) {
                String listener = var2[var4];

                try {
                    Class<?> aClass = Class.forName(listener);
                    ServerArrivalListener instance = (ServerArrivalListener)aClass.newInstance();
                    subClient.addServerArrivalListener(instance);
                    if (m_logger.isDebugEnabled()) {
                        m_logger.debug("add server arrival listener: " + listener);
                    }
                } catch (Exception var8) {
                    var8.printStackTrace();
                    m_logger.error("add server arrival listener failed: " + listener);
                }
            }

        }
    }

    private  void initServerRemoveListener(String listenerStr) {
        if (!StringUtils.isBlank(listenerStr)) {
            String[] saListeners = listenerStr.split(",");
            String[] var2 = saListeners;
            int var3 = saListeners.length;

            for(int var4 = 0; var4 < var3; ++var4) {
                String listener = var2[var4];

                try {
                    Class<?> aClass = Class.forName(listener);
                    ServerRemoveListener instance = (ServerRemoveListener)aClass.newInstance();
                    subClient.addServerRemoveListener(instance);
                    if (m_logger.isDebugEnabled()) {
                        m_logger.debug("add server remove listener: " + listener);
                    }
                } catch (Exception var8) {
                    var8.printStackTrace();
                    m_logger.error("add server remove listener failed: " + listener);
                }
            }

        }
    }

    public  void subscribeService(String serviceName, DataAdapter dataAdapter) {
        if (subClient != null) {
            List<SubFilter> filters = new ArrayList();
            SubFilter f = new SubFilter();
            f.setInterestedName(serviceName);
            filters.add(f);
            subClient.subscribe(filters);
            subClient.addDataAdapter(serviceName, dataAdapter);
        }
    }

    public  Map<String, List<DaemonServerInfo>> getServices(Set<String> snames) {
        Map<String, List<DaemonServerInfo>> serverMap = new HashMap();
        if (subClient != null && snames != null && !snames.isEmpty()) {
            Iterator var2 = snames.iterator();

            while(var2.hasNext()) {
                String sname = (String)var2.next();
                List<DaemonServerInfo> list = subClient.getServiceByNameFromAllGroup(sname);
                if (list != null && !list.isEmpty()) {
                    serverMap.put(sname, list);
                }
            }

            return serverMap;
        } else {
            return serverMap;
        }
    }

    private  boolean checkDependencyServers(DaemonConfig config) throws Exception {
        Set<String> serverNames = getSubscribeNames();
        if (serverNames != null && serverNames.size() != 0) {
            AtomicInteger aint = (AtomicInteger)checkRpCounts.get(config);
            if (aint == null) {
                aint = new AtomicInteger(0);
                checkRpCounts.put(config, aint);
            }

            StringBuffer sb = null;
            m_logger.info("check dependency start ...size = " + serverNames.size());

            while(true) {
                serverNames = getSubscribeNames();
                sb = new StringBuffer();
                Iterator var5 = serverNames.iterator();

                while(var5.hasNext()) {
                    String s = (String)var5.next();
                    if (!getClient().isSubscribePulled(s)) {
                        sb.append(",").append(s);
                    }
                }

                if (sb.length() <= 0) {
                    m_logger.info("dependency servers=" + getSubscribeNames() + " has subscrib from s2s and  can query it's serverList");
                    return true;
                }

                int retryTime = aint.incrementAndGet();
                if (retryTime > 10) {
                    String result = "dependency servers:[" + sb.toString().replaceFirst(",", "") + "] not found in s2s serverList,must start these servers first";
                    throw new S2SException(result);
                }

                m_logger.info("dependency server=" + sb.toString().replaceFirst(",", "") + " can not query it's serverList! retry in 1s current try time = " + retryTime + " s");
                Thread.sleep(1000L);
            }
        } else {
            return false;
        }
    }

    public  Set<String> getSubscribeNames() {
        if (subClient == null) {
            return new HashSet();
        } else {
            CopyOnWriteArrayList<ServerArrivalListener> list = subClient.getServerArrivalListeners();
            Set<String> nameset = new HashSet();
            Iterator var2 = list.iterator();

            while(var2.hasNext()) {
                ServerArrivalListener listener = (ServerArrivalListener)var2.next();
                nameset.add(listener.listenSv());
            }

            return nameset;
        }
    }
}
