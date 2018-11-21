package cn.jerry.test.net;

import cn.jerry.net.HttpClientUtil;
import cn.jerry.net.ip.IpAddressUtil;
import cn.jerry.net.phone.PhoneNumRegionUtil;
import org.apache.http.client.CookieStore;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.cookie.CookieSpecProvider;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.cookie.BasicClientCookie;
import org.apache.http.impl.cookie.BestMatchSpecFactory;
import org.apache.http.impl.cookie.BrowserCompatSpecFactory;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class HttpClientUtilTester {

    public static void main(String[] args) {
        // testTaobaoPhoneNoService();
        // testTaobaoIpAddrService();

        String url = "http://localhost:8000/fabricService/tx/update.do";
        Map<String, String> headers = new HashMap<>();
        headers.put("user", "user2");
        headers.put("identityCode", "2B109459621393468DCC06F408F7D7E1");
        Map<String, String> params = new HashMap<>();
        //params.put("chaincode", "{\"name\":\"我的merchant_contract\",\"version\":\"1\",\"lang\":\"JAVA\"}"); //10960
        params.put("chaincode.name", "merchant_contract"); //10960
        params.put("chaincode.version", "1"); //10960
        params.put("chaincode.lang", "JAVA"); //10960
        params.put("txAccount", "a");
        params.put("txData", "2");
        try {
            String resp = HttpClientUtil.httpGet(url, headers, params, "utf-8", 10 * 1000);
            System.out.println(resp);
        } catch (IOException e) {
            e.printStackTrace();
        }
//        String url = "http://cpadmin.ule.com/cpshop/provinceDecoration/detail.do";
//        String url1 = "http://cpadmin.ule.com/cpshop/cpshop/queryLayoutPage.do";
//        String cookieKey = "CPADMIN_COOKIE";
//        String cookieValue = "\"23sf3dwio|Uqt+o3oFcQA4Rbkn0DQR5KDJfKHFIAd1yVmkGE1nAyj4t5IvU/Kihfs44IPsoyNu0QGHGSYnM4CsS+"
//                + "KZPLz2E8jfda66PEigylC4LbUlqYjTGuMyOaahaent0uNG92SDh089r8yGqCfO4BQR5XXVJDghlxVxOQqGRjXBNbQpx88=|P7QywR"
//                + "eWsFNLt3fGBfYZowvAsnQJFpEnguP53g1Hcjq3FnKdkCIxrZrP73lBKSi0YJKjxy4HEXHGT1h+fLedG4UDKW/CSVsAsqCSXjPFP2R"
//                + "2abR/WefPfZUDsrhdtG9fnkYvMVa8p+hkylHxBf9OG/hyJ+07fPOG+W2kQxGlyuTmgZNAfY+n1q6Fu4DgoKenQnHGI7HJvBkUfxoY"
//                + "qLB3Ebzcz+u59JDwZ41/G3M19g0CzgFY9rAeuOQgPTn/ez2aleh0x5o6kk92Ah+js+U5AGYoo5wmo3I2AgE1nhuDl+gQxHeyWG5z6"
//                + "O84wRTzEeJAEZDA2n1ru4/5ze0a3OHyUmT5m1aO0ZxhlAsmEtVwZFaVIbXUomX1jw/3hYtMWxSzGrDDB9RvyWy20Hln69sR4n6K1C"
//                + "1Pv5oo2Xf9MiHzxkSVBJB/IUjbo43HLt6sOt2ViBjcs105Yx+kB58Ur1Oq5Yh0aByjcvYnw6ymLiOhMp0teQoW10EAhJ..\"";
//
//        CookieStore cookieStore = new BasicCookieStore();
//        // 新建一个Cookie
//        BasicClientCookie cookie = new BasicClientCookie(cookieKey, cookieValue);
//        // cookie.setVersion(0);
//        cookie.setDomain("cpadmin.ule.com");
//        cookie.setPath("/cpshop");
//        cookieStore.addCookie(cookie);
//        Map<String, String> headers = new HashMap<>();
//        headers.put("Accept-Encoding", "gzip");
//        headers.put("Cookie", cookieKey + "=" + cookieValue);
//        Map<String, String> params = new HashMap<>();
////        params.put("pageId", "11790"); //10960
////        params.put("editmode", "true");
//
//        try {
//            String resp = HttpClientUtil.httpGet(url1, headers, params, "utf-8", 10 * 1000);
//            System.out.println(resp);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }

//        HttpClientContext context = HttpClientContext.create();
//        Registry<CookieSpecProvider> registry = RegistryBuilder
//                .<CookieSpecProvider> create()
//                .register(CookieSpecs.BEST_MATCH, new BestMatchSpecFactory())
//                .register(CookieSpecs.BROWSER_COMPATIBILITY, new BrowserCompatSpecFactory())
//                .build();
//        context.setCookieSpecRegistry(registry);
//        context.setCookieStore(cookieStore);
    }

    private static void testMerchantApi() {
        // beta:4a1904620d14bfd1
        // prd:923fc0ceac7b8745
        String url = "https://service.beta.ule.com/merchant/orderTradesGet.do";
        HashMap<String, String> headers = new HashMap<>();
        //headers.put("Accept","application/json, text/javascript, */*; q=0.01");
        //headers.put("Accept-Encoding","gzip, deflate, br");
        headers.put("Content-Type", "application/x-www-form-urlencoded");
        headers.put("connection", "close");
        headers.put("appkey", "4a1904620d14bfd1");
        HashMap<String, String> params = new HashMap<>();
        //params.put("pageNo","1");
        //params.put("pageSize","1");
        params.put("startTime", "2018-08-24 00:00:00");
        params.put("endTime", "2018-08-25 00:00:00");
        //params.put("startTime","2018-08-25 12:44:35");
        //params.put("endTime","2018-08-25 12:44:35");
        params.put("format", "1");
        params.put("sign", "NEVGRDY4RkZCRUVDNUZCODBDMTFCMUUyMzY1RTMyNkU=");
        params.put("version_no", "apr_2010_build01");
        String reps = null;
        try {
            reps = HttpClientUtil.httpPost(url, headers, params, null, null);
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println(reps);
    }

    private static void testTaobaoPhoneNoService() {
        String phoneNo = "15687864645";
        System.out.println(PhoneNumRegionUtil.queryPhoneNumRegion(phoneNo));
    }

    private static void testTaobaoIpAddrService() {
        String ip = "124.90.103.34";
        System.out.println(IpAddressUtil.queryAddress(ip));
    }
}