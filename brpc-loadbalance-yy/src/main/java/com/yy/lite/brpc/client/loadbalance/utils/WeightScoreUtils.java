package com.yy.lite.brpc.client.loadbalance.utils;

import com.yy.lite.brpc.client.loadbalance.bean.WeightProperties;
import com.yy.common.hostinfo.bean.ServiceInstanceTag;

public class WeightScoreUtils {

    private static int getRoomScore(int isSame){
        return isSame * 40 ;
    }
    private static int getGroupScore(int isSame){
        return isSame * 35 ;
    }
    private static int getRegioneScore(int isSame){
        return isSame * 30 ;
    }
    private static int getAreaScore(int isSame){
        return isSame * 25 ;
    }

    private static int getConnectionsScore(int connectionNum){
        return connectionNum/10 ;
    }
    private static int getFailNumScore(long failNum){
        return 0 ;
    }

    public static  int getScore(ServiceInstanceTag consumerTag, WeightProperties serviceProp){
        ServiceInstanceTag serviceTag = serviceProp.getTagInfo();
        int score = getRoomScore(consumerTag.getRoomId() == serviceTag.getRoomId() ? 1:0) +
                getGroupScore(consumerTag.getGroupId() == serviceTag.getGroupId() ? 1:0) +
                getRegioneScore(consumerTag.getRegionId() == serviceTag.getRegionId() ? 1:0) +
                getAreaScore(consumerTag.getAreaId() == serviceTag.getAreaId() ? 1:0) +
                getConnectionsScore(serviceProp.getBrpcChannel().getActiveConnectionNum()) +
                getFailNumScore(serviceProp.getBrpcChannel().getFailedNum());
        return score;
    }
}