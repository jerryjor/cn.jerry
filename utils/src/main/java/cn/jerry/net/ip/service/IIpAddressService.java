package cn.jerry.net.ip.service;

import cn.jerry.net.ip.IpAddressResult;

public interface IIpAddressService {

	/**
	 * 服务名
	 * 
	 * @return
	 */
	String mySericeName();

	/**
	 * 查询ip地址
	 * 
	 * @param ip
	 * @return
	 */
	IpAddressResult queryAddressByIp(String ip);
}
