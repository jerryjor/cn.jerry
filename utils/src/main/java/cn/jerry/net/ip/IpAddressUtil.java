package cn.jerry.net.ip;

import java.util.ArrayList;
import java.util.List;

import cn.jerry.model.ResultCode;
import cn.jerry.net.ip.service.IIpAddressService;
import cn.jerry.net.ip.service.IpAddressBaidu;
import cn.jerry.net.ip.service.IpAddressTaobao;

public class IpAddressUtil {
	private static List<IIpAddressService> services;

	static {
		services = new ArrayList<>();
		// 注册淘宝服务
		services.add(new IpAddressTaobao());
		services.add(new IpAddressBaidu());
	}

	/**
	 * 查询ip地址
	 * 
	 * @param ip
	 * @return
	 */
	public static IpAddressResult queryAddress(String ip) {
		IpAddressResult result = null;
		for (IIpAddressService service : services) {
			try {
				result = service.queryAddressByIp(ip);
				System.out.println(service.myServiceName() + ":" + result);
				if (result != null && ResultCode.SUCCEED == result.getCode()) {
					return result;
				}
			} catch (Exception e) {
				// do nothing...
			}
		}
		// default
		result = new IpAddressResult(ResultCode.FAILED, "NO service avaliable.", null);
		result.setIp(ip);
		return result;
	}

}
