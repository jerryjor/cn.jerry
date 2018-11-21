package cn.jerry.net.phone;

import java.io.Serializable;

import cn.jerry.json.JsonUtil;

public class PhoneNumRegion implements Serializable {
	private static final long serialVersionUID = 1L;

	private String province;
	private String carrier;

	public String getProvince() {
		return province;
	}

	public void setProvince(String province) {
		this.province = province;
	}

	public String getCarrier() {
		return carrier;
	}

	public void setCarrier(String carrier) {
		this.carrier = carrier;
	}

	@Override
	public String toString() {
		try {
			return JsonUtil.toJson(this);
		} catch (Exception e) {
			return "PhoneNumRegion ["
			        + (province != null ? "province=" + province + ", " : "")
			        + (carrier != null ? "carrier=" + carrier + ", " : "")
			        + "]";
		}
	}

}
