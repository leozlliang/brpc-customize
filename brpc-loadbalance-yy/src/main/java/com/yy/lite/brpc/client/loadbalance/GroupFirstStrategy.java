/*
 * Copyright (c) 2019 Baidu, Inc. All Rights Reserved.
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
package com.yy.lite.brpc.client.loadbalance;

import com.alibaba.fastjson.JSON;
import com.baidu.brpc.client.RpcClient;
import com.baidu.brpc.client.channel.BrpcChannel;
import com.baidu.brpc.client.loadbalance.LoadBalanceStrategy;
import com.baidu.brpc.protocol.Request;
import com.google.common.collect.Lists;
import com.yy.common.hostinfo.bean.IpInfo;
import com.yy.common.hostinfo.bean.ServerInfo;
import com.yy.common.hostinfo.bean.ServiceInstanceTag;
import com.yy.common.hostinfo.utils.ServerUtil;
import com.yy.lite.brpc.client.loadbalance.bean.RandomObjectPool;
import com.yy.lite.brpc.client.loadbalance.bean.WeightProperties;
import com.yy.lite.brpc.client.loadbalance.utils.GroupFirstFilterUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

/**
 * 同机房优先：
 * 1. 筛选出ISP相同及roomId/groupId/areaId/regionId相同的列表
 * 2. 对列表进行轮询
 */
public class GroupFirstStrategy implements LoadBalanceStrategy {

    private Logger logger = LoggerFactory.getLogger(GroupFirstStrategy.class);
    private AtomicLong counter = new AtomicLong(0);

    private ServiceInstanceTag consumerTag = new ServiceInstanceTag();

    private Set<Integer> ispSet = new HashSet<>();

    private RpcClient rpcClient;

    @Override
    public void init(RpcClient rpcClient) {
        this.rpcClient = rpcClient;
        ServerInfo serverInfo = ServerUtil.getServerInfoByHostInfo();
        consumerTag = getServiceInstanceTag(serverInfo);
        TreeSet<IpInfo> ipList = serverInfo.getIpInfo();
        for (IpInfo ipInfo : ipList) {
            ispSet.add(ipInfo.getIsp().getIspid());
        }
    }

    @Override
    public BrpcChannel selectInstance(
            Request request,
            List<BrpcChannel> instances,
            Set<BrpcChannel> selectedInstances) {
        List<String> ips = Lists.transform(instances,inst-> inst.getServiceInstance().getIp());
//        logger.info("{}_{}_all_brpcChannels:{}",request.getServiceName(),request.getMethodName(),StringUtils.join(ips,";"));
        long instanceNum = instances.size();
        if (instanceNum == 0) {
            return null;
        }
        if (instanceNum == 1) {
            return instances.get(0);
        }
        List<WeightProperties> serviceWeightPropList = instances.stream().map(brpcChannel -> {
            WeightProperties properties = new WeightProperties();
            properties.setBrpcChannel(brpcChannel);
            properties.setRpcClient(rpcClient);
            ServiceInstanceTag tagInfo = new ServiceInstanceTag();
            String tagStr = brpcChannel.getServiceInstance().getTag();
            if (StringUtils.isNotBlank(tagStr)) {
                tagInfo = JSON.parseObject(tagStr, ServiceInstanceTag.class);
            }
            properties.setTagInfo(tagInfo);
            return properties;
        }).collect(Collectors.toList());

        serviceWeightPropList = GroupFirstFilterUtils.filter(serviceWeightPropList, consumerTag, ispSet);

        BrpcChannel result =  new RandomObjectPool<>(serviceWeightPropList).random().get().getBrpcChannel();
//        logger.info("{}_{}_selected_brpcChannel:{}",request.getServiceName(),request.getMethodName(),result.getServiceInstance().getIp());
        return result;
    }

    private int getByRoundRobin(int size) {
        return (int) counter.getAndIncrement() % size;
    }

    private static ServiceInstanceTag getServiceInstanceTag(ServerInfo serverInfo) {
        ServiceInstanceTag tagInfo = new ServiceInstanceTag();
        tagInfo.setGroupId(serverInfo.getGroupid());
        tagInfo.setRegionId(serverInfo.getRegionId());
        tagInfo.setAreaId(serverInfo.getAreaId());
        tagInfo.setRoomId(serverInfo.getRoomId());
        return tagInfo;
    }

    @Override
    public void destroy() {
    }

}
