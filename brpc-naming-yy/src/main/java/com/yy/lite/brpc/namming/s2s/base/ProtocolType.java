package com.yy.lite.brpc.namming.s2s.base;


import org.apache.commons.lang3.StringUtils;

/**
 * 用于框架自身，其中val用于s2s注册兼容anka的protocol map信息，存在变动性
 * @author 罗文江
 *
 */
public enum ProtocolType {

    YYP("yyp", 0), THRIFT("thrift", 1), YSERVICE("yservice", 2), YRPC("yrpc", 3), 
    NTHRIFT("nthrift",1), 
    ANKATHRIFT("ankathrift", 1), 
    ENTSERV("entserv", 8), MOBSERV("mobserv", 9), 
    DUBBO("dubbo", 10);

    ProtocolType(String code, Integer val) {
        this.code = code;
        this.val = val;
    }

    private String code;
    private Integer val;

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public Integer getVal() {
        return val;
    }

    public void setVal(Integer val) {
        this.val = val;
    }

    public static ProtocolType create(String protocol) {
        if (StringUtils.isEmpty(protocol))  throw new RuntimeException(String.format("ProtocolType#create(String) failed, protocol-%s is empty", protocol));
        switch (protocol.toLowerCase()) {
            case "yyp":
                return YYP;
            case "yservice":
                return YSERVICE;
            case "thrift":
                return THRIFT;
            case "dubbo":
                return DUBBO;
            case "yrpc":
                return YRPC;
            case "nthrift":
                return NTHRIFT;
            case "entserv":
            	return ENTSERV;
            case "mobserv":
            	return MOBSERV;
            case "ankathrift":
            	return ANKATHRIFT;
            	default:
            		throw new RuntimeException(String.format("ProtocolType#create(String) failed, protocol-%s is unsuported", protocol));
        }
    }
    /**
     * 用于展示给业务知悉，不变
     * @author zhangfeng@yycom
     *
     */
    public static enum RpcProtocol {
    	YYP,YSERVICE,THRIFT,ANKATHRIFT,ENTSERV,MOBSERV,YRPC,YRPCSERVICE;
    	public static RpcProtocol toRPCProtocol(String protocol) {
    		if (StringUtils.isEmpty(protocol))  throw new RuntimeException(String.format("RPCProtocol#toRPCProtocol(String) failed, protocol-%s is empty", protocol));
            switch (protocol.toLowerCase()) {
                case "yyp":
                    return YYP;
                case "yservice":
                    return YSERVICE;
                case "thrift":
                    return THRIFT;
                case "ankathrift":
                	return ANKATHRIFT;
                case "entserv":
                	return ENTSERV;
                case "mobserv":
                	return MOBSERV;
                case "yrpc":
                    return YRPC;
                case "yrpcservice":
                    return YRPCSERVICE;
                	default:
                		throw new RuntimeException(String.format("RPCProtocol#toRPCProtocol(String) failed, protocol-%s is unsuported", protocol));
            }
    	}
    }
}
