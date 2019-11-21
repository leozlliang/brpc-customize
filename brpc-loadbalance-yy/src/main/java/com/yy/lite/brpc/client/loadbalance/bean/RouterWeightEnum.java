package com.yy.lite.brpc.client.loadbalance.bean;

/**
 * 路由权重信息配置
 */
public enum RouterWeightEnum {
    /**
     * IDC的权重配置
     */
    WEIGHT_IDC_SAME(800, "同机房权重"),
    WEIGHT_IDC_NO_SAME(0, "不同机房权重"),

    /**
     * ISP的权重配置
     */
    WEIGHT_ISP_SAME(800, "相同运营商"),
    WEIGHT_ISP_NO_SAME(0, "不同运营商"),
    /**
     * 同组的权重配置
     */
    WEIGHT_GROUP_SAME(400, "相同组"),
    WEIGHT_GROUP_NO_SAME(0, "不同组"),

    /**
     * 同区域的权重配置
     */
    WEIGHT_AREA_SAME(200, "相同区域"),
    WEIGHT_AREA_NO_SAME(0, "不同区域"),

    /**
     * 同省份的权重配置
     */
    WEIGHT_REGION_SAME(100, "相同省份"),
    WEIGHT_REGION_NO_SAME(0, "不同省份");

    RouterWeightEnum(long weight, String desc) {
        this.weight = weight;
        this.desc = desc;
    }

    private long weight;
    private String desc;

    public long getWeight() {
        return weight;
    }

    public void setWeight(long weight) {
        this.weight = weight;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }
}
