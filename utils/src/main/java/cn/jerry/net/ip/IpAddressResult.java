package cn.jerry.net.ip;

import cn.jerry.model.Result;
import cn.jerry.model.ResultCode;

public class IpAddressResult extends Result<Address> {
	private static final long serialVersionUID = 1L;

	private String ip;

	public IpAddressResult() {
		super();
	}

	public IpAddressResult(ResultCode code, String message, Address data) {
		super(code, message, data);
	}

	public String getIp() {
		return ip;
	}

	public void setIp(String ip) {
		this.ip = ip;
	}

}
