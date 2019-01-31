package cn.jerry.fabric.api.fabric.conf;

public class LocalPathTool {

    public static String removeWindowsDrive(String path) {
        int index = path.indexOf(":");
        return index == -1 ? path : path.substring(index + 1);
    }

}
