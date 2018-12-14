package cn.jerry.test.net;

import cn.jerry.json.JsonUtil;
import cn.jerry.net.HttpClientUploadUtil;
import cn.jerry.net.HttpClientUtil;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class HttpClientUploadUtilTester {
    // admin, 9CAE7E1F85DDC7715D9AC5DE0FD12078
    // tester, 1EDE196E0566E7BB91F0C67398960276
    public static void main(String[] args) {
        testRegister();
        // testTx("update", new String[]{"a", "{\"message\":\"Hello! World.\"}"});
        // testQuery("queryHis", new String[]{"a", "1", "10"});
        //testQuery("queryByTx", new String[]{"a", "018eb34e8dfcf986fd414f6e3096e4d945e3579b0411ea99e30ef83625621cb4"});
    }

    private static void testRegister() {
        String url = "http://blockchain-fabricservice.http.beta.uledns.com/fabricService/user/register.do";
        Map<String, String> headers = new HashMap<>();
        headers.put("user", "admin");
        headers.put("identityCode", "9CAE7E1F85DDC7715D9AC5DE0FD12078");
        Map<String, String> params = new HashMap<>();
        params.put("username", "user124");
        params.put("password", "user123");

        try {
            String response = HttpClientUtil.httpPost(url, headers, params, null, 6000);
            System.out.println(response);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void testDeploy() {
        String url = "http://blockchain-fabricservice.http.beta.uledns.com/fabricService/mgt/deploy.do";
        Map<String, String> headers = new HashMap<>();
        headers.put("user", "admin");
        headers.put("identityCode", "9CAE7E1F85DDC7715D9AC5DE0FD12078");
        Map<String, String> params = new HashMap<>();
        params.put("chaincode", "{\"name\":\"tester\",\"version\":\"1.0\",\"lang\":\"JAVA\"}");

        try {
            String response = HttpClientUploadUtil.httpPost(url, headers, params, "file",
                    "/home/zhaojiarui/merchantChaincode.zip", null, 120000, null, null);
            System.out.println(response);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void testUpgrade() {
        String url = "http://blockchain-fabricservice.http.beta.uledns.com/fabricService/mgt/upgrade.do";
        Map<String, String> headers = new HashMap<>();
        headers.put("user", "admin");
        headers.put("identityCode", "9CAE7E1F85DDC7715D9AC5DE0FD12078");
        Map<String, String> params = new HashMap<>();
        params.put("chaincode", "{\"name\":\"tester\",\"version\":\"1.1\",\"lang\":\"JAVA\"}");

        try {
            String response = HttpClientUploadUtil.httpPost(url, headers, params, "file",
                    "/home/zhaojiarui/merchantChaincode.zip", null, 120000, null, null);
            System.out.println(response);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // 80ef90f97392024f9487bf4dd7a1acf5a4e8a24539190e7da09f984e7568c505
    private static void testTx(String method, String[] args) {
        String url = "http://blockchain-fabricservice.http.beta.uledns.com/fabricService/tx/invoke/" + method + ".do";
        Map<String, String> headers = new HashMap<>();
        headers.put("user", "tester");
        headers.put("identityCode", "1EDE196E0566E7BB91F0C67398960276");
        Map<String, String> params = new HashMap<>();
        params.put("chaincode", "{\"name\":\"tester\",\"version\":\"1.1\",\"lang\":\"JAVA\"}");
        try {
            params.put("args", JsonUtil.toJsonNonNull(args));
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            String response = HttpClientUtil.httpPost(url, headers, params, null, 6000);
            System.out.println(response);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void testQuery(String method, String[] args) {
        String url = "http://blockchain-fabricservice.http.beta.uledns.com/fabricService/tx/query/" + method + ".do";
        Map<String, String> headers = new HashMap<>();
        headers.put("user", "tester");
        headers.put("identityCode", "1EDE196E0566E7BB91F0C67398960276");
        Map<String, String> params = new HashMap<>();
        params.put("chaincode", "{\"name\":\"tester\",\"version\":\"1.1\",\"lang\":\"JAVA\"}");
        try {
            params.put("args", JsonUtil.toJsonNonNull(args));
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            String response = HttpClientUtil.httpPost(url, headers, params, null, 6000);
            System.out.println(JsonUtil.formatJsonStr(response, "  "));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
