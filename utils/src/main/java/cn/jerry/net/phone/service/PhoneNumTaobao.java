package cn.jerry.net.phone.service;

import cn.jerry.model.ResultCode;
import cn.jerry.net.HttpRequesterWithPool;
import cn.jerry.net.phone.PhoneNumRegion;
import cn.jerry.net.phone.PhoneNumRegionResult;

import java.nio.charset.Charset;

public class PhoneNumTaobao implements IPhoneNumService {
	private static final String HOST_TAOBAO = "https://tcc.taobao.com";
	private static final String URI_TAOBAO = "/cc/json/mobile_tel_segment.htm";
	private static final String PARAM_TAOBAO = "tel";

	@Override
	public String mySericeName() {
		return "taobao";
	}

	/**
	 * 限制：未知
	 * 
	 * @param phoneNum
	 * @return
	 */
	@Override
	public PhoneNumRegionResult queryRegionByPhoneNum(String phoneNum) {
		PhoneNumRegionResult result = new PhoneNumRegionResult();
		result.setPhoneNum(phoneNum);
		if (phoneNum == null || phoneNum.trim().isEmpty()) {
			result.setCode(ResultCode.FAILED);
			result.setMessage("param[phoneNum] is empty");
			return result;
		}

		String response = null;
		try {
			response = new HttpRequesterWithPool.HttpUriRequestBuilder(HOST_TAOBAO + URI_TAOBAO)
                    .addParam(PARAM_TAOBAO, phoneNum)
                    .setCharset(Charset.forName("GBK"))
                    .setSocketTimeout(30000)
                    .build()
                    .doRequest();
		} catch (Exception e) {
			result.setCode(ResultCode.FAILED);
			result.setMessage("call service failed:[" + e.getMessage() + "]");
			return result;
		}
		System.out.println(response);
		// __GetZoneResult_ = {
		// [缩进，可能是空格，也可能是tab]mts:'1568786',
		// [缩进，可能是空格，也可能是tab]province:'云南',
		// [缩进，可能是空格，也可能是tab]catName:'中国联通',
		// [缩进，可能是空格，也可能是tab]telString:'15687864645',
		// [缩进，可能是空格，也可能是tab]areaVid:'30515',
		// [缩进，可能是空格，也可能是tab]ispVid:'137815084',
		// [缩进，可能是空格，也可能是tab]carrier:'云南联通'
		// }
		int index = response == null ? -1 : response.indexOf("{");
		if (index == -1) {
			result.setCode(ResultCode.FAILED);
			result.setMessage("service is offline or unstable.");
			return result;
		}

		result.setCode(ResultCode.SUCCEED);
		PhoneNumRegion region = new PhoneNumRegion();
		result.setData(region);
		try {
			response = response.substring(index + 1, response.lastIndexOf("}")).trim();
			String[] lines = response.split(",");
			for (String line : lines) {
				String[] kv = line.trim().split(":");
				if ("carrier".equals(kv[0].trim())) {
					region.setProvince(kv[1].substring(1, 3));
					region.setCarrier(kv[1].substring(3, 5));
				}
			}
		} catch (Exception e) {
			result.setCode(ResultCode.FAILED);
			result.setMessage("read service response failed:[" + e.getMessage() + "]");
		}
		return result;
	}

}
