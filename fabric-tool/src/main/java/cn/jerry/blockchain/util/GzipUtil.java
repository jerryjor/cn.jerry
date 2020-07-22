package cn.jerry.blockchain.util;

import cn.jerry.blockchain.util.crypto.Base64Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class GzipUtil {
    private static final Logger log = LoggerFactory.getLogger(GzipUtil.class);

    private GzipUtil() {
        super();
    }

    public static String gzip(String primStr) {
        if (primStr == null || primStr.isEmpty()) {
            return "";
        }

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try (GZIPOutputStream gzip = new GZIPOutputStream(out)) {
            gzip.write(primStr.getBytes(StandardCharsets.UTF_8));
        } catch (IOException e) {
            log.error("Failed to zip String: {}, {}", primStr, e.getMessage());
            return primStr;
        }
        return Base64Util.encodeFromBytes(out.toByteArray());
    }

    public static String unzip(String zippedStr) {
        if (zippedStr == null || zippedStr.isEmpty()) {
            return "";
        }

        byte[] bytes;
        try {
            bytes = Base64Util.decodeToBytes(zippedStr);
        } catch (IllegalArgumentException e) {
            // 不是base64转码后的数据，肯定不是本工具类压缩的数据，则直接返回原始字符串
            return zippedStr;
        }
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            try (ByteArrayInputStream in = new ByteArrayInputStream(bytes);
                 GZIPInputStream gzip = new GZIPInputStream(in)) {
                byte[] buffer = new byte[1024];
                int offset;
                while ((offset = gzip.read(buffer)) != -1) {
                    out.write(buffer, 0, offset);
                }
            } catch (IOException ze) {
                log.error("Failed to unzip bytes... {}", ze.getMessage());
                // 解压失败，直接返回原始字符串
                return zippedStr;
            }
            return new String(out.toByteArray(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            log.error("Failed to close output stream...");
            // 解压失败，直接返回原始字符串
            return zippedStr;
        }
    }

}
