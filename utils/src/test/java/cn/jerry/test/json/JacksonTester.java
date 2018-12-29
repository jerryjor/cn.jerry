package cn.jerry.test.json;

import cn.jerry.json.JsonUtil;
import cn.jerry.lang.DateUtil;
import cn.jerry.net.HttpRequesterWithPool;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class JacksonTester {
	public static void main(String[] args) {

        Map<String, String> head = new HashMap<String, String>();
        head.put("moduleApp", "finace-merchantEJB");
        head.put("requestId", UUID.randomUUID().toString().replace("-", ""));
        head.put("requestTime", "" + System.currentTimeMillis());
        head.put("Content-Type", "application/json");
        Map<String, String> dataBody = new HashMap<String, String>();
        dataBody.put("escOrderid", "618121400049520203");
        dataBody.put("sellerOnlyid", "800102953");

        String url = "http://soa-shoppingorderdetailservice.http.beta.uledns.com/shoppingorderDetailService/mer/orderInfo/lv1";
        // String response = "{\"msg\":\"SUCCESS\",\"code\":\"0000\",\"data\":{\"ctocOrder\":{\"area\":\"铜陵县\",\"areaCode\":\"340721\",\"businessType\":1501,\"buyerIpAddr\":\"125.82.208.199\",\"buyerLoginid\":\"ule1002999839\",\"buyerMacId\":\"WAP#000#001#vB6dlzH4BTsH8hMd6EPOY7AUBwpxFPFT\",\"buyerMobile\":\"18896146516\",\"buyerName\":\"ule1002999839\",\"buyerOnlyid\":1002999839,\"buyerPayTime\":1543313867000,\"channel\":\"MOBILE\",\"commissionAmount\":0.03,\"deliverTime\":2,\"discountAmount\":0.0,\"escOrderid\":\"618112702129818503\",\"freezeStatus\":1,\"invoiceRequired\":\"N\",\"merchantBackurl\":\"1002999839\",\"orderActiveTime\":1543918654000,\"orderAmount\":1.0,\"orderCreateTime\":1543313854000,\"orderDeleted\":0,\"orderId\":282281182,\"orderPoint\":0,\"orderStatus\":5,\"orderSubstatus\":505,\"orderTag\":\"100001\",\"orderType\":2001,\"partnerPurchaseNote\":\"邮乐枞阳精准扶贫馆\",\"partnerShippingNote\":\"26345\",\"payVersion\":3,\"payedAmount\":1.0,\"paymentAmount\":1.0,\"productAmount\":1.0,\"reserveFlag\":1,\"saleType\":102,\"salesChannel\":\"239\",\"sellerLoginid\":\"铜陵誉升商贸有限责任公司\",\"sellerModifyTime\":1543313868000,\"sellerNote\":\"CUN_YOU_ZHAN\",\"sellerOnlyid\":800130832,\"sellerShipTime\":1543480998000,\"supportedBuyType\":\"V2+:1,85,88,90,70,111,6,9,5,221\",\"transAddress\":\"安徽省铜陵市铜陵县邮政局\",\"transAmount\":0.0,\"transCity\":\"铜陵市\",\"transCityCode\":\"340700\",\"transCountry\":\"中国\",\"transPostalCode\":\"244000\",\"transProvince\":\"安徽省\",\"transProvinceCode\":\"340000\",\"transType\":3,\"transType3Addvalue\":0.0,\"transType3Amount\":5.0,\"transType3Value\":2.0,\"transType4Amount\":0.0,\"transType5Amount\":2.0,\"transUsrName\":\"王薛涵\",\"transUsrPhone\":\"18896146516\",\"unionStatus\":0,\"updateTime\":1543480998000}}}";

        try {
            String response = new HttpRequesterWithPool.HttpUriRequestBuilder(url)
                    .addHeaders(head)
                    .addParams(dataBody)
                    .setSocketTimeout(3000)
                    .build()
                    .doRequest();
            System.out.println(response);
            Map<String, Object> jo = JsonUtil.toHashMap(response, String.class, Object.class);
            Map<String, Object> dataMap = (Map<String, Object>) jo.get("data");
            Map<String, Object> ctocOrderMap = dataMap == null ? null : (Map<String, Object>) dataMap.get("ctocOrder");
            Object o = ctocOrderMap.get("orderCreateTime");
            System.out.println(o);
            System.out.println(o.getClass().getName());
            Date date = DateUtil.parseDateAuto(o.toString());
            System.out.println(date);
        } catch (Exception e) {
            e.printStackTrace();
        }
//		int[] baseIp = new int[] { 100, 100, 100, 100 };
//		Set<String> blackIps = new HashSet<String>();
//		String ip = null;
//		for (int i = 0; i < 50000; i++) {
//			ip = baseIp[0] + "." + baseIp[1] + "." + baseIp[2] + "." + baseIp[3];
//			blackIps.add(ip);
//			baseIp[3]++;
//			for (int j = 3; j > 0; j--) {
//				if (baseIp[j] == 256) {
//					baseIp[j] = 100;
//					baseIp[j - 1]++;
//				}
//			}
//		}
//		System.out.println("last ip:" + ip);
//		int loop = 1000;
//		long st = System.currentTimeMillis();
//		String json;
//		for (int i = 0; i < loop; i++) {
//			try {
//				json = JsonUtil.toJson(blackIps);
//				JsonUtil.toHashSet(json, String.class);
//			} catch (IOException e) {
//				e.printStackTrace();
//			}
//		}
//		long et = System.currentTimeMillis();
//		System.out.println("json read/write " + loop + "times, cost " + (et - st) + "ms.");

		// KeyValue<String, Number> kv = new KeyValue<String, Number>("1", 1);
		// try {
		// String json = JsonUtil.toJson(kv);
		// @SuppressWarnings("unchecked")
		// KeyValue<String, Number> kv1 = JsonUtil.toObject(json, KeyValue.class, String.class,
		// Long.class);
		// System.out.println(kv1);
		// System.out.println(kv1.getKey().getClass().getSimpleName());
		// System.out.println(kv1.getValue().getClass().getSimpleName());
		// } catch (IOException e) {
		// // TODO Auto-generated catch block
		// e.printStackTrace();
		// }
		// List<KeyValue<String, Number>> list = new ArrayList<KeyValue<String, Number>>();
		// for (int i = 1; i < 6; i++) {
		// list.add(new KeyValue<String, Number>(""+i,i));
		// }
		// String json = JsonUtil.toJsonSilently(list);
		// System.out.println(json);
		// try {
		// Set<Map<String, Number>> result2 = JsonUtil.toHashSetFillWithHashMap(json,String.class,
		// Number.class);
		// System.out.println(result2);
		// } catch (IOException e) {
		// // TODO Auto-generated catch block
		// e.printStackTrace();
		// }

		// Map<String, List<Number>> map = new HashMap<String, List<Number>>();
		// for (int i = 0; i < 3; i++) {
		// List<Number> list = new ArrayList<Number>();
		// map.put("" + i, list);
		// for (int j = 0; j < 3; j++) {
		// list.add(j);
		// }
		// }
		// try {
		// String json = JsonUtil.toJson(map);
		// Map<String, Set<Number>> result = JsonUtil.toHashMapFillWithHashSet(json, String.class,
		// Number.class);
		// System.out.println(result);
		// } catch (IOException e) {
		// // TODO Auto-generated catch block
		// e.printStackTrace();
		// }
	}
}
