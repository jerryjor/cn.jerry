package cn.jerry.test.json;

import cn.jerry.json.JsonUtil;
import cn.jerry.json.XmlUtil;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class XmlUtilTester {
    public static void main(String[] args) {
        Map<String, Object> result = new HashMap<>();
        result.put("code", "0000");
        result.put("message", "SUCCEED");
        Map<String, Object> data = new HashMap<>();
        result.put("data", data);
        data.put("escOrderId", "2018052633333333");
        data.put("orderAmount", 28.31D);
        data.put("productNum", 2);
        List<Map<String, Object>> products = new ArrayList<>();
        data.put("products", products);
        Map<String, Object> product;
        for (int i = 0; i < 2; i++) {
            product = new HashMap<>();
            products.add(product);
            product.put("itemId", "10000" + i);
            product.put("itemName", "test" + i);
        }
        //System.out.println(XmlUtil.formatXmlStr(XmlUtil.buildXml(result, "result", false), "  "));
        try {
            System.out.println(JsonUtil.formatJsonStr(JsonUtil.toJson(result), "  "));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
