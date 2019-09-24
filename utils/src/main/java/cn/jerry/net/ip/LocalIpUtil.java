package cn.jerry.net.ip;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

public class LocalIpUtil {
    private static final String DEFAULT_IP4 = "127.0.0.1";
    private static final String LOCAL_IP4 = isWindowsOS() ? getWindowsIp4() : getLinuxIp4();

    private LocalIpUtil() {}

    public static String getLocalIp4() {
        return LOCAL_IP4;
    }

    private static boolean isWindowsOS() {
        return System.getProperty("os.name").toLowerCase().contains("windows");
    }

    private static String getWindowsIp4() {
        Enumeration<NetworkInterface> allNetInterfaces;
        try {
            allNetInterfaces = NetworkInterface.getNetworkInterfaces();
        } catch (SocketException e) {
            return DEFAULT_IP4;
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
                    if (DEFAULT_IP4.equals(ip.getHostAddress())) {
                        continue;
                    }
                    ip4 = ip.getHostAddress();
                }
            }
        }
        return ip4 == null ? DEFAULT_IP4 : ip4;
    }

    private static String getLinuxIp4() {
        Enumeration<NetworkInterface> allNetInterfaces;
        try {
            allNetInterfaces = NetworkInterface.getNetworkInterfaces();
        } catch (SocketException e) {
            return DEFAULT_IP4;
        }
        InetAddress ip;
        String ip4 = null;
        while (allNetInterfaces.hasMoreElements()) {
            NetworkInterface netInterface = allNetInterfaces.nextElement();
            if (netInterface.getName().contains("lo") || netInterface.getName().contains("docker")) {
                continue;
            }
            Enumeration<InetAddress> addresses = netInterface.getInetAddresses();
            while (addresses.hasMoreElements()) {
                ip = addresses.nextElement();
                if (ip instanceof Inet4Address && !ip.isLoopbackAddress()) {
                    if (DEFAULT_IP4.equals(ip.getHostAddress())) {
                        continue;
                    }
                    ip4 = ip.getHostAddress();
                }
            }
        }
        return ip4 == null ? DEFAULT_IP4 : ip4;
    }

}
