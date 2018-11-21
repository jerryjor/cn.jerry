package cn.jerry.net.ip.service;

import java.util.HashMap;
import java.util.Map;

import cn.jerry.json.JsonUtil;
import cn.jerry.model.ResultCode;
import cn.jerry.net.HttpClientUtil;
import cn.jerry.net.ip.Address;
import cn.jerry.net.ip.IpAddressResult;

public class IpAddressTaobao implements IIpAddressService {
	private static final String HOST_TAOBAO = "http://ip.taobao.com";
	private static final String URI_TAOBAO = "/service/getIpInfo.php";

	public String mySericeName() {
		return "taobao";
	}

	/**
	 * 限制：10次/s
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
			params.put("ip", ip);
			response = HttpClientUtil.httpPost(HOST_TAOBAO + URI_TAOBAO, params, null, null);
		} catch (Exception e) {
			result.setCode(ResultCode.FAILED);
			result.setMessage("call service failed:[" + e.getMessage() + "]");
			return result;
		}
		// System.out.println(response);
		Map<String, Object> taobaoResult = null;
		try {
			taobaoResult = JsonUtil.toHashMap(response, String.class, Object.class);
		} catch (Exception e) {
			result.setCode(ResultCode.FAILED);
			result.setMessage("read service response failed:[" + e.getMessage() + "]");
			return result;
		}
		try {
			Integer status = taobaoResult == null ? 1 : (Integer) taobaoResult.get("code");
			if (status != 0) {
				result.setCode(ResultCode.FAILED);
				result.setMessage(taobaoResult == null ? "service no response"
				        : (String) taobaoResult.get("message"));
				return result;
			}
			result.setCode(ResultCode.SUCCEED);
			Map<String, String> data = (Map<String, String>) taobaoResult.get("data");
			if (data != null) {
				Address addr = new Address();
				result.setData(addr);
				addr.setCountry(data.get("country"));
				addr.setCountryId(data.get("country_id"));
				addr.setProvince(data.get("region"));
				addr.setCity(data.get("city"));
			}
		} catch (Exception e) {
			result.setCode(ResultCode.FAILED);
			result.setMessage("read service response failed:[" + e.getMessage() + "]");
		}
		return result;
	}

}
