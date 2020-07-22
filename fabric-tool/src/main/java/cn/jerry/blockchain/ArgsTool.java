package cn.jerry.blockchain;

import java.util.HashMap;
import java.util.Map;

public class ArgsTool {
    public static Map<String, String> readArgs(String[] args) {
        Map<String, String> params = new HashMap<>();
        if (args == null || args.length == 0) return params;

        String key = null, value = null;
        for (String arg : args) {
            if (arg.charAt(0) == '-') {
                key = arg.substring(1);
            } else {
                value = arg;
                if (key != null) {
                    params.put(key, value);
                    key = null;
                    value = null;
                }
            }
        }
        return params;
    }

}
