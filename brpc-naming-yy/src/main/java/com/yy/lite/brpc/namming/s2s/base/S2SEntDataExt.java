package com.yy.lite.brpc.namming.s2s.base;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.yy.ent.clients.s2s.codec.S2SEncoder;
import com.yy.ent.clients.s2s.codec.S2SEntData;

import java.util.HashMap;
import java.util.Map;

public class S2SEntDataExt extends S2SEntData {
	
	private static final long serialVersionUID = 5515165437932878827L;
	
	private Map<String,Object> extMap = new HashMap<>();

    public void setProtocol(String protocol){
        extMap.put("codec",protocol);
    }

    public byte[] toByteArray() throws JsonProcessingException {
        S2SEncoder en = new S2SEncoder();
        en.writeIpList(getIpList());
        if (getTcp_port() != 0)
            en.writeTcpPort(getTcp_port());
        if (getUdp_port() != 0)
            en.writeUdpPort(getUdp_port());
        if (getProtocol() != null) {
            en.writeProtocol(getProtocol());
        }
        Map<String, Object> paramMap =  getParamMap();
        for (String key : paramMap.keySet()) {
            en.insert(key, paramMap.get(key));
        }
        for(Map.Entry<String,Object> entry : extMap.entrySet()){
            en.insert(entry.getKey(),entry.getValue());
        }
        return en.endToByteArray();
    }

}
