package cn.jerry.net.phone;

import java.util.ArrayList;
import java.util.List;

import cn.jerry.model.ResultCode;
import cn.jerry.net.phone.service.IPhoneNumService;
import cn.jerry.net.phone.service.PhoneNumTaobao;

public class PhoneNumRegionUtil {

	private static List<IPhoneNumService> services;

	static {
		services = new ArrayList<>();
		// 注册淘宝服务
		services.add(new PhoneNumTaobao());
	}

	/**
	 * 查询手机号归属
	 * 
	 * @param phoneNum
	 * @return
	 */
	public static PhoneNumRegionResult queryPhoneNumRegion(String phoneNum) {
		PhoneNumRegionResult result = null;
		for (IPhoneNumService service : services) {
			try {
				result = service.queryRegionByPhoneNum(phoneNum);
				System.out.println(service.mySericeName() + ":" + result);
				if (result != null && ResultCode.SUCCEED == result.getCode()) {
					return result;
				}
			} catch (Exception e) {
				// do nothing...
			}
		}
		// default
		result = new PhoneNumRegionResult(ResultCode.FAILED, "NO service avaliable.", null);
		result.setPhoneNum(phoneNum);
		return result;
	}
}
