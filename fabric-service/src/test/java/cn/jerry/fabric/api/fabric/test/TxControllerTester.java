package cn.jerry.fabric.api.fabric.test;

import cn.jerry.fabric.api.fabric.util.crypto.BouncyCastleSignatureUtil;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.PrivateKey;
import java.security.SignatureException;
import java.util.*;

public class TxControllerTester {
    private static String ccInfo = "{\"name\":\"testing\",\"lang\":\"JAVA\",\"version\":\"2.0\"}";
    private static String username = "tester1";
    private static String pkString = "-----BEGIN EC PRIVATE KEY-----\n" +
            "MHcCAQEEIHVATXYGyWIYtgFgcRKD4wd8firAN1Dbr2xDllBKQc3LoAoGCCqGSM49\n" +
            "AwEHoUQDQgAEcrWpA8S3MOyvEuoU6VCSTIstG9/dXR8yS41ejPju5yWxybdKQcCU\n" +
            "u6E4sQjJC9uCckUYHScyOedQKIYfxv00qA==\n" +
            "-----END EC PRIVATE KEY-----\n";
    private static PrivateKey pk = null;

    public static void main(String[] args) {
        try {
            pk = BouncyCastleSignatureUtil.getKeyFromPEMString(pkString);
        } catch (IOException e) {
            e.printStackTrace();
        }
        // 成功的： 8e09df5c095ea646fa4aaddde73d5e8b69325d721c7b899f63eb0245070d9137
        // 失败的： c74d3c7428b3f44c47200eefbd59641466b2a67ddcf25067334fef0799933bd8
        String txUrl = "http://localhost:8000/fabricService/tx/invoke/update.do";
        String queryUrl = "http://localhost:8000/fabricService/tx/query/queryTx.do";
        Map<String, String> params = new HashMap<>();
        params.put("chaincode", ccInfo);
        String content = "{\"content\":\"just for test\"}";
        params.put("args", "account1," + content);
        params.put("sync", "1");
        try {
            String sign = BouncyCastleSignatureUtil.sign(pk, buildParamsText(params).getBytes(StandardCharsets.UTF_8));
            System.out.println(sign);
            String signKey = Base64.getEncoder().encodeToString(pkString.getBytes(StandardCharsets.UTF_8));
            System.out.println(signKey);
            HttpRequesterWithoutPool requester = new HttpRequesterWithoutPool.HttpUriRequestBuilder(txUrl)
                    .addHeader("user", username)
                    .addHeader("sign", sign)
                    .addHeader("signKey", signKey)
                    .setSocketTimeout(6000)
                    .build();
            String resp = requester.doPost(params, false);
            System.out.println(resp);
            resp = requester.doPost(params, false);
            System.out.println(resp);
        } catch (SignatureException | InvalidKeyException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private static String buildParamsText(Map<String, String> params) {
        List<String> paramNames = new ArrayList<>();
        params.forEach((k, v) -> {
            paramNames.add(k);
        });
        Collections.sort(paramNames);
        StringBuilder builder = new StringBuilder();
        paramNames.forEach(paramName ->
                builder.append("&").append(paramName).append("=").append(params.get(paramName)));
        return builder.toString();
    }

}
