package cn.jerry.blockchain.util;

import java.io.*;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class PemFileUtil {
    private static final String PROTOCOL_JAR = "jar:file:";

    private PemFileUtil() {
        super();
    }

    public static String readPemString(String filename) throws IOException {
        return new String(readPemBytes(filename), StandardCharsets.UTF_8);
    }

    public static byte[] readPemBytes(String filename) throws IOException {
        InputStream stream = null;
        try {
            if (filename.contains(".jar!")) {
                stream = new URL(PROTOCOL_JAR + filename).openStream();
            } else {
                // 读取classpath下资源
                stream = PemFileUtil.class.getResourceAsStream(filename);
                if (stream == null) {
                    // 读取绝对路径资源
                    File file = new File(filename);
                    if (!file.exists() || !file.isFile()) {
                        return new byte[0];
                    }
                    stream = new FileInputStream(file);
                }
            }
            return readPemBytes(stream);
        } finally {
            if (stream != null) {
                try {
                    stream.close();
                } catch (Exception e) {
                    // do nothing...
                }
            }
        }
    }

    public static byte[] readPemBytes(InputStream stream) throws IOException {
        if (stream == null) {
            return new byte[0];
        }
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            byte[] buffer = new byte[1024 * 1024];
            int length;
            while ((length = stream.read(buffer)) != -1) {
                baos.write(buffer, 0, length);
            }
            baos.flush();
            return baos.toByteArray();
        }
    }

}
