package com.yy.common.hostinfo.bean;

import java.util.TreeSet;

public class ServerInfo {
	
	private TreeSet<IpInfo> ipInfo;
	private int groupid;
	private int regionId;
	private int areaId;
	private int roomId;

	public ServerInfo(){
		groupid = 0;
	}
	
	public TreeSet<IpInfo> getIpInfo() {
		return ipInfo;
	}
	public void setIpInfo(TreeSet<IpInfo> ipInfo) {
		this.ipInfo = ipInfo;
	}
	public int getGroupid() {
		return groupid;
	}
	public void setGroupid(int groupid) {
		this.groupid = groupid;
	}


	public int getRegionId() {
		return regionId;
	}

	public void setRegionId(int regionId) {
		this.regionId = regionId;
	}

	public int getAreaId() {
		return areaId;
	}

	public void setAreaId(int areaId) {
		this.areaId = areaId;
	}

	public int getRoomId() {
		return roomId;
	}

	public void setRoomId(int roomId) {
		this.roomId = roomId;
	}

	@Override
	public String toString() {
		return "ServerInfo{" +
				"ipInfo=" + ipInfo +
				", groupid=" + groupid +
				", regionId=" + regionId +
				", areaId=" + areaId +
				", roomId=" + roomId +
				'}';
	}
}
