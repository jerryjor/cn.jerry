package cn.jerry.blockchain;

import cn.jerry.blockchain.fabric.cache.CaAdminCache;
import cn.jerry.blockchain.fabric.conf.OrgConfig;
import cn.jerry.blockchain.fabric.model.FabricOrg;
import cn.jerry.blockchain.fabric.model.FabricUser;
import cn.jerry.blockchain.fabric.tools.UserTools;
import cn.jerry.blockchain.util.crypto.X509SignUtil;

import java.util.Map;

public class Register {

    public static void main(String[] args) {
        Map<String, String> argParams = ArgsTool.readArgs(args);
        String an = argParams.get("an");
        String ap = argParams.get("ap");
        String un = argParams.get("un");
        String up = argParams.get("up");
        if (an == null || ap == null || un == null) {
            System.out.println("参数不足. 例："
                    + "\n\t-an ca服务管理员用户名"
                    + "\n\t-ap ca服务管理员密码"
                    + "\n\t-un 注册的用户名"
                    + "\n\t-up 注册的密码");
            return;
        }

        try {
            FabricOrg org = OrgConfig.getInstance().getDefaultOrg();
            FabricUser caAdmin = CaAdminCache.getInstance().get(org, an, ap);
            if (caAdmin != null) {
                System.out.println("ca服务管理员用户名/密码不匹配.");
                return;
            }
            if (UserTools.existsUser(caAdmin, un)) {
                System.out.println("用户名" + un + "已被注册.");
                return;
            }

            FabricUser user = UserTools.registerClientUser(caAdmin, un, up);
            System.out.println("用户" + un + "注册成功，\n证书："
                    + "\n" + user.getEnrollment().getCert()
                    + "\n私钥："
                    + "\n" + X509SignUtil.getPEMFromKey(user.getEnrollment().getKey()));
        } catch (Exception e) {
            System.err.println("注册失败: " + e.getMessage());
        }
    }

}
