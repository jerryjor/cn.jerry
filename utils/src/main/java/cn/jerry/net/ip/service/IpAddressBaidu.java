package cn.jerry.net.ip.service;

import java.util.HashMap;
import java.util.Map;

import cn.jerry.json.JsonUtil;
import cn.jerry.model.ResultCode;
import cn.jerry.net.HttpClientUtil;
import cn.jerry.net.ip.Address;
import cn.jerry.net.ip.IpAddressResult;

public class IpAddressBaidu implements IIpAddressService {
	private static final String HOST_BAIDU = "http://api.map.baidu.com";
	private static final String URI_BAIDU = "/location/ip";
	private static final String APPKEY_BAIDU = "cTjGnacaWKhyRWPddoxeU3xymLVGFvYa";
	// private static final String APPSEC_BAIDU = "de3FSgmEU23WElAk7QGeVkzO1wGRe30e";

	public String mySericeName() {
		return "baidu";
	}

	/**
	 * 限制：100万次/天
	 * 
	 * @param ip
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public IpAddressResult queryAddressByIp(String ip) {
		IpAddressResult result = new IpAddressResult();
		result.setIp(ip);
		if (ip == null || ip.trim().isEmpty()) {
			result.setCode(ResultCode.FAILED);
			result.setMessage("param[ip] is empty");
			return result;
		}

		String response = null;
		try {
			Map<String, String> params = new HashMap<String, String>();
			params.put("ak", APPKEY_BAIDU);
			params.put("ip", ip);
			// params.put("sn", MD5.genMd5(URI + "?ak=" + APPKEY_BAIDU + "&ip=" + ip +
			// APPSEC_BAIDU));
			response = HttpClientUtil.httpPost(HOST_BAIDU + URI_BAIDU, params, null, null);
		} catch (Exception e) {
			result.setCode(ResultCode.FAILED);
			result.setMessage("call service failed:[" + e.getMessage() + "]");
			return result;
		}
		// System.out.println(response);
		Map<String, Object> baiduResult = null;
		try {
			baiduResult = JsonUtil.toHashMap(response, String.class, Object.class);
		} catch (Exception e) {
			result.setCode(ResultCode.FAILED);
			result.setMessage("read service response failed:[" + e.getMessage() + "]");
			return result;
		}
		try {
			Integer status = baiduResult == null ? 1 : (Integer) baiduResult.get("status");
			if (status != 0) {
				result.setCode(ResultCode.FAILED);
				result.setMessage((String) baiduResult.get("message"));
				return result;
			}
			result.setCode(ResultCode.SUCCEED);
			Address addr = new Address();
			result.setData(addr);
			String addrStr = (String) baiduResult.get("address");
			addr.setCountryId(addrStr == null ? null : addrStr.split("\\|")[0]);
			Map<String, Object> content = (Map<String, Object>) baiduResult.get("content");
			if (content != null) {
				Map<String, String> detail = (Map<String, String>) content.get("address_detail");
				if (detail != null) {
					addr.setProvince(detail.get("province"));
					addr.setCity(detail.get("city"));
				}
			}
		} catch (Exception e) {
			result.setCode(ResultCode.FAILED);
			result.setMessage("read service response failed:[" + e.getMessage() + "]");
		}
		return result;
	}

}
