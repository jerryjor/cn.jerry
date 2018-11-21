package cn.jerry.net.phone.service;

import cn.jerry.net.phone.PhoneNumRegionResult;

public interface IPhoneNumService {

	/**
	 * 服务名
	 * 
	 * @return
	 */
	String mySericeName();

	/**
	 * 查询手机号归属地
	 * 
	 * @param phoneNum
	 * @return
	 */
	PhoneNumRegionResult queryRegionByPhoneNum(String phoneNum);
}
