package cn.jerry.blockchain;

import cn.jerry.blockchain.util.crypto.DigestUtil;

import java.nio.charset.StandardCharsets;
import java.util.Map;

public class Crypto {
    public static void main(String[] args) {
        Map<String, String> params = ArgsTool.readArgs(args);
        String name = params.get("u");
        String pwd = params.get("p");
        if (name == null || pwd == null) {
            System.out.println("参数不足. 例： -u 用户名 -p 密码");
            return;
        }
        try {
            String identityCode = DigestUtil.sha1DigestAsHex(pwd.getBytes(StandardCharsets.UTF_8));
            System.out.println("身份码：" + identityCode);
        } catch (Exception e) {
            System.err.println("生成身份码失败.");
        }
    }
}
