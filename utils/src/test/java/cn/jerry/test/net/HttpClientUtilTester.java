package cn.jerry.test.net;

import cn.jerry.net.ip.IpAddressUtil;
import cn.jerry.net.phone.PhoneNumRegionUtil;

public class HttpClientUtilTester {

    public static void main(String[] args) {
        testTaobaoPhoneNoService();
        // testTaobaoIpAddrService();
    }

    private static void testTaobaoPhoneNoService() {
        String phoneNo = "13778953200";
        System.out.println(PhoneNumRegionUtil.queryPhoneNumRegion(phoneNo));
    }

    private static void testTaobaoIpAddrService() {
        String ip = "124.90.103.34";
        System.out.println(IpAddressUtil.queryAddress(ip));
    }
}