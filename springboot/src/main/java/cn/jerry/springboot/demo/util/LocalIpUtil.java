package cn.jerry.springboot.demo.util;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

public class LocalIpUtil {

    public static String getLocalIp4() {
        Enumeration<NetworkInterface> allNetInterfaces;
        try {
            allNetInterfaces = NetworkInterface.getNetworkInterfaces();
        } catch (SocketException e) {
            return null;
        }
        InetAddress ip;
        String ip4 = null;
        // 取最后一条不是127.0.0.1的ip4
        while (allNetInterfaces.hasMoreElements()) {
            NetworkInterface netInterface = allNetInterfaces.nextElement();
            Enumeration<InetAddress> addresses = netInterface.getInetAddresses();
            while (addresses.hasMoreElements()) {
                ip = addresses.nextElement();
                if (ip instanceof Inet4Address) {
                    if ("127.0.0.1".equals(ip.getHostAddress())) continue;
                    ip4 = ip.getHostAddress();
                }
            }
        }
        return ip4;
    }

}
