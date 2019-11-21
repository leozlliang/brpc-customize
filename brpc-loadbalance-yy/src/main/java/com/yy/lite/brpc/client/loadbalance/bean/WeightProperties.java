package com.yy.lite.brpc.client.loadbalance.bean;

import com.baidu.brpc.client.RpcClient;
import com.baidu.brpc.client.channel.BrpcChannel;
import com.yy.common.hostinfo.bean.ServiceInstanceTag;
import org.apache.commons.collections4.CollectionUtils;

public class WeightProperties implements RandomObject<WeightProperties> {

    private ServiceInstanceTag tagInfo;

    private RpcClient rpcClient;

    private BrpcChannel brpcChannel;

    /**
     * 同机房权重为1
     */
    private long idcWeight = 0;
    /**
     * isp权重
     */
    private long ispWeight = 0;

    private long groupWeight = 0;

    private long areaWeight = 0;

    private long regionWeight = 0;

    private double weight = 0;

    public long getIdcWeight() {
        return idcWeight;
    }

    public RpcClient getRpcClient() {
        return rpcClient;
    }

    public void setRpcClient(RpcClient rpcClient) {
        this.rpcClient = rpcClient;
    }

    public void setIdcWeight(long idcWeight) {
        this.idcWeight = idcWeight;
    }

    public long getIspWeight() {
        return ispWeight;
    }

    public void setIspWeight(long ispWeight) {
        this.ispWeight = ispWeight;
    }

    @Override
    public double getWeight() {
        if (weight > 0) {
            return weight;
        }
        double weight = 10;
        weight += this.idcWeight + this.ispWeight + this.groupWeight + this.areaWeight + this.regionWeight;
        if (CollectionUtils.isNotEmpty(this.brpcChannel.getLatencyWindow()) && this.rpcClient.getRpcClientOptions().getReadTimeoutMillis() != 0) {
            long rtt = this.brpcChannel.getLatencyWindow().stream().mapToLong(Integer::longValue).sum() / this.brpcChannel.getLatencyWindow().size();
            double rate = this.rpcClient.getRpcClientOptions().getReadTimeoutMillis() * 1.0 / rtt;
            if (rate < 1) {
                rate = 1;
            }
            if (rate > 1000) {
                rate = 1000;
            }
            weight *= rate;
        }
        this.weight = weight;
        return this.weight;
    }

    @Override
    public WeightProperties get() {
        return this;
    }

    public long getGroupWeight() {
        return groupWeight;
    }

    public void setGroupWeight(long groupWeight) {
        this.groupWeight = groupWeight;
    }

    public long getAreaWeight() {
        return areaWeight;
    }

    public void setAreaWeight(long areaWeight) {
        this.areaWeight = areaWeight;
    }

    public long getRegionWeight() {
        return regionWeight;
    }

    public void setRegionWeight(long regionWeight) {
        this.regionWeight = regionWeight;
    }

    public ServiceInstanceTag getTagInfo() {
        return tagInfo;
    }

    public void setTagInfo(ServiceInstanceTag tagInfo) {
        this.tagInfo = tagInfo;
    }

    public BrpcChannel getBrpcChannel() {
        return brpcChannel;
    }

    public void setBrpcChannel(BrpcChannel brpcChannel) {
        this.brpcChannel = brpcChannel;
    }
}