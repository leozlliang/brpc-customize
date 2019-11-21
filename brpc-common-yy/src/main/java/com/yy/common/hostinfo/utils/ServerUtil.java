package com.yy.common.hostinfo.utils;

import com.yy.common.hostinfo.bean.IpInfo;
import com.yy.common.hostinfo.bean.ServerInfo;
import com.yy.common.hostinfo.enums.IspType;
import org.apache.commons.lang3.SystemUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.naming.Context;
import javax.naming.InitialContext;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.TreeSet;

public class ServerUtil {

    private static final Logger logger = LoggerFactory.getLogger(ServerUtil.class);

    /**
     * hostinfo file path
     */
    private static final String HOST_INFO_FILE = (SystemUtils.IS_OS_LINUX || SystemUtils.IS_OS_UNIX) ? "/home/dspeak/yyms/hostinfo.ini" : "C:\\hostinfo.ini";

    private static final String BMC_ISP = "BMC";

    public ServerUtil() {
    }

    private static ServerInfo serverInfo = null;

    public static boolean isIncludeIspType(IspType ispType) {
        for (IpInfo ispInfo : getServerInfoByHostInfo().getIpInfo()) {
            if (ispInfo.getIsp() == ispType) {
                return true;
            }
        }
        return false;
    }

    /**
     * 获取hostInfoFile的文件路径
     *
     * @return
     */
    public static String getHostInfoFilePath() {
        return HOST_INFO_FILE;
    }

    /**
     * 获取groupid
     *
     * @return
     */
    public static int getServerGroupidByHostInfo() {
        return getServerInfoByHostInfo().getGroupid();
    }

    /**
     * 获取内网IP
     *
     * @return
     */
    public static String getIntranetIp() {
        for (IpInfo ipInfo : getServerInfoByHostInfo().getIpInfo()) {
            if (ipInfo.getIsp() == IspType.INTRANET) {
                return ipInfo.getIp();
            }
        }
        return null;
    }

    /**
     * 通过配置文件获取机器ip和GroupId信息
     *
     * @return
     */
    public static ServerInfo getServerInfoByHostInfo() {
        if (serverInfo != null) {
            return serverInfo;
        }
        try {
            IniReader read = new IniReader(HOST_INFO_FILE);
            ServerInfo serverInfo = new ServerInfo();
            String[] iplist = read.getValue("ip_isp_list", "").split(",");
            TreeSet<IpInfo> ipInfoList = new TreeSet<>();

            for (String iptemp : iplist) {
                String[] itemlist = iptemp.split(":");
                if (itemlist.length < 2) {
                    continue;
                }
                if (BMC_ISP.equals(itemlist[1])) {
                    continue;
                }
                IpInfo ipInfo = new IpInfo();
                ipInfo.setIp(itemlist[0]);
                IspType isp = IspType.create(itemlist[1]);
                ipInfo.setIp(itemlist[0]);
                ipInfo.setIsp(isp);
                ipInfoList.add(ipInfo);
            }
            String serverGroup = read.getValue("pri_group_id", "0");
            serverInfo.setGroupid(Integer.valueOf(serverGroup));
            serverInfo.setIpInfo(ipInfoList);
            serverInfo.setRegionId(NumberUtils.toInt(read.getValue("region_id"), 0));
            serverInfo.setAreaId(NumberUtils.toInt(read.getValue("area_id"), 0));
            serverInfo.setRoomId(NumberUtils.toInt(read.getValue("room_id"), 0));
            ServerUtil.serverInfo = serverInfo;
            return ServerUtil.serverInfo;
        } catch (Exception er) {
            throw new RuntimeException(er);
        }
    }

    public static String getDescIP() {
        try {
            IniReader read = new IniReader(HOST_INFO_FILE);
            String ctlip = "";
            String[] iplist = read.getValue("ip_isp_list", "").split(",");
            for (String iptemp : iplist) {
                if (iptemp.indexOf(":CTL") != -1)
                    ctlip = iptemp.substring(0, iptemp.indexOf(":CTL"));
            }
            String room = read.getValue("room", "");
            return ctlip + "(" + room + ")";
        } catch (Exception er) {
            throw new RuntimeException(er);
        }
    }

    /**
     * 通过配置文件获取机器ＩＰ
     *
     * @return
     */
    private static String getIpByHostInfo() {
        try {
            IniReader read = new IniReader(HOST_INFO_FILE);
            String ctlip = "";
            String[] iplist = read.getValue("ip_isp_list", "").split(",");
            for (String iptemp : iplist) {
                if (iptemp.indexOf(":CTL") != -1)
                    ctlip = iptemp.substring(0, iptemp.indexOf(":CTL"));
            }
            return ctlip;
        } catch (Exception er) {
            throw new RuntimeException(er);
        }
    }

    /**
     * 根据域名获取指向的IP(无缓存，使用时请注意性能)
     *
     * @param host
     * @return
     * @throws Exception
     */
    public static String getIp(String host) {
        try {
            InetAddress inet = InetAddress.getByName(host);
            String ip = inet.getHostAddress();
            return ip;
        } catch (UnknownHostException e) {
            logger.info(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    private static List<String> getHostAddress() {
        Enumeration<NetworkInterface> netInterfaces;
        try {
            netInterfaces = NetworkInterface.getNetworkInterfaces();
        } catch (SocketException e) {
            logger.info(e.getMessage(), e);
            return null;
        }
        List<String> list = new ArrayList<String>();
        while (netInterfaces.hasMoreElements()) {
            NetworkInterface ni = netInterfaces.nextElement();
            String displayName = ni.getDisplayName();
            //			System.out.println("getDisplayName:" + ni.getDisplayName());
            Enumeration<InetAddress> ips = ni.getInetAddresses();

            List<String> subList = null;
            if ("eth0".equalsIgnoreCase(displayName)) {
                Enumeration<NetworkInterface> subInterfaces = ni.getSubInterfaces();
                subList = getHostAddress(subInterfaces);
            }
            String currentIp = null;
            while (ips.hasMoreElements()) {
                InetAddress inet = ips.nextElement();
                String ip = inet.getHostAddress();
                if (ip.indexOf(":") == -1) {
                    // 不要使用IPv6
                    if (currentIp == null) {
                        currentIp = ip;
                    } else if (subList != null && !subList.contains(ip)) {
                        currentIp = ip;
                    } else {
                        //忽略
                    }
                }
            }
            if (currentIp != null) {
                list.add(currentIp);
            }
        }
        return list;
    }

    private static List<String> getHostAddress(Enumeration<NetworkInterface> netInterfaces) {
        List<String> list = new ArrayList<String>();
        //		System.out.println("getHostAddress:" + netInterfaces);

        while (netInterfaces.hasMoreElements()) {
            NetworkInterface ni = netInterfaces.nextElement();
            //			System.out.println("getDisplayName:" + ni.getDisplayName());
            Enumeration<InetAddress> ips = ni.getInetAddresses();
            while (ips.hasMoreElements()) {

                InetAddress inet = ips.nextElement();
                String ip = inet.getHostAddress();

                if (ip.indexOf(":") == -1) {
                    // 不要使用IPv6
                    list.add(ip);
                    //					System.out.println("ip:" + ip);
                }
            }
        }
        return list;
    }

    private static Boolean IS_RESIN = null;

    public static boolean isResin() {
        if (IS_RESIN == null) {
            IS_RESIN = check();
        }
        return IS_RESIN;
    }

    public static boolean check() {
        try {
            Context env = (Context) new InitialContext().lookup("java:comp/env");
            if (env == null) {
                return false;
            }
        } catch (javax.naming.NamingException e) {
            return false;
        }
        return true;
    }

    public static void main(String[] args) {
        System.out.println(ServerUtil.getIpByHostInfo());
        System.out.println(ServerUtil.getServerInfoByHostInfo());
        // run com.duowan.utils.ServerUtil
        //		String ip = ServerUtil.getIp();
        //		Systemout.println("ip:" + ip);
    }

}
