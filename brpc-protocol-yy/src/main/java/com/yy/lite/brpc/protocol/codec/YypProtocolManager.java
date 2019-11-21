package com.yy.lite.brpc.protocol.codec;

import com.baidu.brpc.RpcMethodInfo;
import com.baidu.brpc.server.ServiceManager;
import com.google.common.collect.Maps;
import com.yy.anka.io.rpc.parse.*;
import org.apache.commons.lang3.tuple.Pair;

import java.util.Map;

/**
 * @author donghonghua
 * @date 2019/7/26
 */
public class YypProtocolManager {

    private Map<String, RpcMethodInfo> rpcMethodInfoMap = Maps.newConcurrentMap();

    private Map<String, RpcMethodInfo> rpcMethodInfoNameMap = Maps.newConcurrentMap();

    private Map<String, Pair<RPCInfo, RPCInfo>> rpcInfoPairsMap = Maps.newConcurrentMap();

    private static volatile YypProtocolManager instance;

    public static YypProtocolManager getInstance() {
        if (instance == null) {
            synchronized(YypProtocolManager.class) {
                if (instance == null) {
                    instance = new YypProtocolManager();
                }
            }
        }

        return instance;
    }

    private YypProtocolManager() {
    }

    public RpcMethodInfo findRpcMethodInfo(String serviceName, String methodName) {
        if (serviceName == null && methodName == null) {
            return null;
        }
        ServiceManager serviceManager = ServiceManager.getInstance();
        if (serviceName != null && methodName != null) {
            return serviceManager.getService(serviceName, methodName);
        }
        if (serviceName != null) {
            RpcMethodInfo result = rpcMethodInfoMap.get(serviceName);
            if (result != null || rpcMethodInfoMap.containsKey(serviceName)) {
                return result;
            }
            result = matchRpcMethodInfo(serviceName);
            rpcMethodInfoMap.put(serviceName, result);
            return result;
        } else {
            RpcMethodInfo result = rpcMethodInfoNameMap.get(methodName);
            if (result != null || rpcMethodInfoNameMap.containsKey(methodName)) {
                return result;
            }
            result = matchRpcMethodInfoByMethodName(methodName);
            rpcMethodInfoNameMap.put(methodName, result);
            return result;
        }
    }

    private RpcMethodInfo matchRpcMethodInfo(String serviceName) {
        ServiceManager serviceManager = ServiceManager.getInstance();

        RpcMethodInfo result = null;
        for (Map.Entry<String, RpcMethodInfo> entry : serviceManager.getServiceMap().entrySet()) {
            if (entry.getKey().startsWith(serviceName.toLowerCase())) {
                result = entry.getValue();
                break;
            }
        }
        return result;
    }

    private RpcMethodInfo matchRpcMethodInfoByMethodName(String methodName) {
        ServiceManager serviceManager = ServiceManager.getInstance();

        RpcMethodInfo result = null;
        for (Map.Entry<String, RpcMethodInfo> entry : serviceManager.getServiceMap().entrySet()) {
            if (entry.getValue().getMethodName().equals(methodName)) {
                result = entry.getValue();
                break;
            }
        }

        return result;
    }

    public Pair<RPCInfo, RPCInfo> findRPCPair(String serviceName, String methodName) {
        String key = serviceName + "_" + methodName;

        Pair<RPCInfo, RPCInfo> result = rpcInfoPairsMap.get(key);
        if (result != null || rpcInfoPairsMap.containsKey(key)) {
            return result;
        }
        result = parseRPCInfo(serviceName, methodName);
        rpcInfoPairsMap.put(serviceName + "_" + methodName, result);
        return result;
    }

    private Pair<RPCInfo, RPCInfo> parseRPCInfo(String serviceName, String methodName) {
        String[] splits = serviceName.split(RpcConstants.BIG_LEVEL_SEPERATOR);
        int splitLen = splits.length;

        RPCInfo reqInfo = null;
        RPCInfo respInfo = null;
        if (splitLen == 1) {
            UriRPCInfo uriRPCInfo = new UriRPCInfo();
            uriRPCInfo.setServiceName(serviceName);
            uriRPCInfo.setFunctionName(methodName);
            reqInfo = uriRPCInfo;
            respInfo = uriRPCInfo;
        } else if (splitLen == 2) {
            if ("".equals(splits[0])) {
                YypRPCInfo reqYypRPCInfo = new YypRPCInfo();
                reqYypRPCInfo.setUri(Integer.valueOf(splits[1]));
                reqInfo = reqYypRPCInfo;
                YypRPCInfo respYypRPCInfo = new YypRPCInfo();
                respYypRPCInfo.setUri(Integer.valueOf(methodName));
                respInfo = respYypRPCInfo;
            } else {
                SrvRPCInfo reqSrvRPCInfo = new SrvRPCInfo();
                reqSrvRPCInfo.setUri(Integer.valueOf(splits[0]));
                String[] maxMin = splits[1].split(RpcConstants.SMALL_LEVEL_SEPERATOR);
                reqSrvRPCInfo.setMax(Integer.valueOf(maxMin[0]));
                reqSrvRPCInfo.setMin(Integer.valueOf(maxMin[1]));
                reqInfo = reqSrvRPCInfo;

                SrvRPCInfo respSrvRPCInfo = new SrvRPCInfo();
                String[] maxMin2 = methodName.split(RpcConstants.SMALL_LEVEL_SEPERATOR);
                respSrvRPCInfo.setMax(Integer.valueOf(maxMin2[0]));
                respSrvRPCInfo.setMin(Integer.valueOf(maxMin2[1]));
                respInfo = respSrvRPCInfo;
            }
        }

        Pair<RPCInfo, RPCInfo> pair = Pair.of(reqInfo, respInfo);
        return pair;
    }

    public void registerMethodInfoByMethodName(String methodName, RpcMethodInfo rpcMethodInfo) {
        rpcMethodInfoNameMap.putIfAbsent(methodName, rpcMethodInfo);
    }
}
