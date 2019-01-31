package cn.jerry.fabric.chaincode.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class GzipUtil {

    public static byte[] gzip(String primStr) {
        if (primStr == null || primStr.length() == 0) {
            return new byte[0];
        }

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try (GZIPOutputStream gzip = new GZIPOutputStream(out)) {
            gzip.write(primStr.getBytes(StandardCharsets.UTF_8));
        } catch (IOException e) {
            return primStr.getBytes(StandardCharsets.UTF_8);
        }
        byte[] bytes = out.toByteArray();
        System.out.println(bytes.length);
        return bytes;
    }

    public static String unzip(byte[] bytes) {
        if (bytes == null || bytes.length == 0) {
            return "";
        }

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try (ByteArrayInputStream in = new ByteArrayInputStream(bytes);
             GZIPInputStream gzip = new GZIPInputStream(in)) {
            byte[] buffer = new byte[1024];
            int offset = -1;
            while ((offset = gzip.read(buffer)) != -1) {
                out.write(buffer, 0, offset);
            }
        } catch (IOException ze) {
            return new String(bytes, StandardCharsets.UTF_8);
        }
        try {
            out.close();
        } catch (IOException e) {
            // do nothing...
        }
        return new String(out.toByteArray(), StandardCharsets.UTF_8);
    }

}
