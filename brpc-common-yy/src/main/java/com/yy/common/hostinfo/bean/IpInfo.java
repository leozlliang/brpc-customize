package com.yy.common.hostinfo.bean;


import com.yy.common.hostinfo.enums.IspType;

import java.util.Objects;

/**
 * 服务器IP信息，包含ip地址和ISP信息
 *
 * @author weiyukai 20190312
 */
public class IpInfo implements Comparable<IpInfo> {

    private String ip;
    private IspType isp;

    public IspType getIsp() {
        return isp;
    }

    public void setIsp(IspType isp) {
        this.isp = isp;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    @Override
    public String toString() {
        return "ip:" + ip + ",isp:" + isp.getIspName() + ",level:" + isp.getLevel() + ",ispid:" + isp.getIspid();
    }

    @Override
    public int compareTo(IpInfo o) {
        if (this.isp.getLevel() > o.getIsp().getLevel()) {
            return 1;
        } else if (this.isp.getLevel() == o.getIsp().getLevel()) {
            return 0;
        }
        return -1;
    }

}
