package cn.jerry.net.phone;

import cn.jerry.model.Result;
import cn.jerry.model.ResultCode;

public class PhoneNumRegionResult extends Result<PhoneNumRegion> {
	private static final long serialVersionUID = 1L;

	private String phoneNum;

	public PhoneNumRegionResult() {
		super();
	}

	public PhoneNumRegionResult(ResultCode code, String message, PhoneNumRegion data) {
		super(code, message, data);
	}

	public String getPhoneNum() {
		return phoneNum;
	}

	public void setPhoneNum(String phoneNum) {
		this.phoneNum = phoneNum;
	}

}
