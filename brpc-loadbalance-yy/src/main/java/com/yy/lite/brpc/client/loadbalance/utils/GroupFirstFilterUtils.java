package com.yy.lite.brpc.client.loadbalance.utils;

import com.yy.common.hostinfo.bean.ServiceInstanceTag;
import com.yy.lite.brpc.client.loadbalance.bean.RouterWeightEnum;
import com.yy.lite.brpc.client.loadbalance.bean.WeightProperties;
import org.apache.commons.collections4.CollectionUtils;

import java.util.List;
import java.util.Set;

public class GroupFirstFilterUtils {

    public static List<WeightProperties> filter(List<WeightProperties> serviceWeightPropList, ServiceInstanceTag consumerTag, Set<Integer> ispSet) {

        if (CollectionUtils.isEmpty(serviceWeightPropList) || consumerTag == null || ispSet == null) {
            return serviceWeightPropList;
        }

        //同运营商(移动/联通/电信)优先
        serviceWeightPropList.stream()
                .filter((properties) -> ispSet.contains(properties.getTagInfo().getIspId()))
                .forEach(p -> p.setIspWeight(RouterWeightEnum.WEIGHT_ISP_SAME.getWeight()));

        //同机房优先
        serviceWeightPropList.stream()
                .filter((properties) -> consumerTag.getRoomId() == properties.getTagInfo().getRoomId())
                .forEach(p -> p.setIdcWeight(RouterWeightEnum.WEIGHT_IDC_SAME.getWeight()));

        //同组优先
        serviceWeightPropList.stream()
                .filter((properties) -> consumerTag.getGroupId() == properties.getTagInfo().getGroupId())
                .forEach(p -> p.setGroupWeight(RouterWeightEnum.WEIGHT_GROUP_SAME.getWeight()));
        //同区域优先
        serviceWeightPropList.stream()
                .filter((properties) -> consumerTag.getAreaId() == properties.getTagInfo().getAreaId())
                .forEach(p -> p.setAreaWeight(RouterWeightEnum.WEIGHT_AREA_SAME.getWeight()));
        //同省份优先
        serviceWeightPropList.stream()
                .filter((properties) -> consumerTag.getRegionId() == properties.getTagInfo().getRegionId())
                .forEach(p -> p.setRegionWeight(RouterWeightEnum.WEIGHT_REGION_SAME.getWeight()));

        return serviceWeightPropList;
    }
}