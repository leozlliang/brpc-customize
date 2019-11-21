package com.yy.lite.brpc.namming.s2s.base;

import com.yy.ent.clients.daemon.DaemonClient;
import com.yy.ent.clients.daemon.DaemonConfig;
import com.yy.ent.clients.s2s.S2SClient;
import com.yy.ent.clients.s2s.util.HostInfoUtils;

import java.lang.reflect.Field;
import java.util.Map;

public class S2sDaemonClient extends  DaemonClient {

    private DaemonConfig s2sConfig;
    private S2SClient  client;

    public S2sDaemonClient(DaemonConfig config) throws Exception {
        super(config);
        this.s2sConfig = config;
    }

    private S2SClient getS2SClient(){
        if(client==null) {
            try {
                Field field = DaemonClient.class.getDeclaredField("s2SClient");
                boolean flag = field.isAccessible();
                field.setAccessible(true);
                client = (S2SClient) field.get(this);
                field.setAccessible(flag);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        return  client;
    }

    public int registerToDaemon(ProtocolType protocolType,Integer port,Integer groupId,Map<Integer, Integer> protocolMap,Map<String,Object> extMap ){
        if (getS2SClient() != null) {
            S2SEntDataExt data = new S2SEntDataExt();
            if(port == null){
                port = s2sConfig.getRegSvPort();
            }
            if(groupId == null){
                groupId = s2sConfig.getGroupId();
            }
            data.writeTcpPort(port).writeIp(HostInfoUtils.getIpList("true".equalsIgnoreCase(s2sConfig.getEnableIntranet())));
//            if(s2sConfig.getSetid() != null) data.put(XsdKeyConstants.SET_ID, s2sConfig.getSetid());
            if(protocolMap != null){
                data.writeProtocol(protocolMap);
            }
            if(extMap!=null){
                data.getParamMap().putAll(extMap);
            }
            if (groupId != 0) {
                // 如果groupId为0，可能是配置文件没传groupId，或者数据有问题，因此当groupId不为0时才进行处理
                getS2SClient().setGroupId(groupId);
            }
            data.setProtocol(protocolType.getCode());
            return getS2SClient().setMine(data);
        }
        return 0;
    }
}
