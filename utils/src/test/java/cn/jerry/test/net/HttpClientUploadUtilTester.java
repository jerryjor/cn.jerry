package cn.jerry.test.net;

import cn.jerry.net.HttpClientUploadUtil;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class HttpClientUploadUtilTester {
    // admin, 9CAE7E1F85DDC7715D9AC5DE0FD12078
    public static void main(String[] args) {
        String url = "http://blockchain-fabricservice.http.beta.uledns.com/fabricService/mgt/deploy.do";
        // String url = "http://192.168.113.52/uleFabricService/mgt/deploy.do";
        Map<String, String> headers = new HashMap<>();
        headers.put("user", "admin");
        headers.put("identityCode", "9CAE7E1F85DDC7715D9AC5DE0FD12078");
        Map<String, String> params = new HashMap<>();
        params.put("chaincode", "{\"name\":\"tester\",\"version\":\"1.0\",\"lang\":\"JAVA\"}");
        //params.put("chaincode.name", "tester");
        //params.put("chaincode.version", "1.0");
        //params.put("chaincode.lang", "JAVA");

        try {
            String response = HttpClientUploadUtil.httpPost(url, headers, params, "file",
                    "/home/zhaojiarui/merchantChaincode.zip", null, 120000, null, null);
            System.out.println(response);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
