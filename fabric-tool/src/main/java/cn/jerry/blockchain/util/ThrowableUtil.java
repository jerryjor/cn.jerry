package cn.jerry.blockchain.util;

public class ThrowableUtil {

    private ThrowableUtil() {
        super();
    }

    /**
     * 发掘异常的根本原因
     */
    public static Throwable findRootCause(Throwable t) {
        Throwable rootCause = t;
        while (t != null) {
            rootCause = t;
            t = t.getCause();
        }
        return rootCause;
    }

}
