package cn.jerry.fabric.api.tools.test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import java.util.zip.ZipException;

public class GzipTester {

    private static byte[] gzip(String primStr) throws IOException {
        if (primStr == null || primStr.length() == 0) {
            return new byte[0];
        }

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try (GZIPOutputStream gzip = new GZIPOutputStream(out)) {
            gzip.write(primStr.getBytes(StandardCharsets.UTF_8));
        } catch (IOException e) {
            throw e;
        }
        byte[] bytes = out.toByteArray();
        System.out.println(bytes.length);
        return bytes;
    }

    private static String unzip(byte[] bytes) throws IOException {
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
        } catch (ZipException ze) {
            // Not in GZIP format
            return new String(bytes, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw e;
        }
        try {
            out.close();
        } catch (IOException e) {
            // do nothing...
        }
        return new String(out.toByteArray(), StandardCharsets.UTF_8);
    }

    public static void main(String[] args) {
        String primStr = "{\"orderNo\":\"2018122285931364\",\"merchantId\":800115343,\"storeId\":15579,\"lastEvent\":null,\"currEvent\":\"ORDER_DELIVERY\",\"currEventDay\":20181222,\"orderAmount\":null,\"paymentAmount\":null,\"itemCount\":null,\"itemOrigPrice\":null,\"itemSalePrice\":null,\"itemList\":[{\"orderNo\":\"2018122285931364\",\"merchantId\":800115343,\"storeId\":15579,\"listingId\":3454715,\"itemId\":345471501,\"itemName\":\"鲜惠多 心相印特柔3层卷纸BT810\",\"origPrice\":null,\"salePrice\":3300,\"count\":1,\"merchantSku\":null,\"itemUnit\":null,\"priceDiscount\":null}],\"org\":null,\"discountAmount\":null,\"transAmount\":null,\"orderCreateTime\":null,\"orderUpdateTime\":null} ";
        byte[] bytes = primStr.getBytes(StandardCharsets.UTF_8);
        //System.out.println("before gzip: " + bytes.length);
        try {
            //bytes = gzip(primStr);
            //System.out.println("after gzip: " + bytes.length);
            System.out.println(primStr.equals(unzip(bytes)));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
