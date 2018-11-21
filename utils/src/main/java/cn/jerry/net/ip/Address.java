package cn.jerry.net.ip;

import java.io.Serializable;

import cn.jerry.json.JsonUtil;

public class Address implements Serializable {
	private static final long serialVersionUID = 1L;

	private String country;
	private String countryId;
	private String province;
	private String city;
	private String area;

	public String getCountry() {
		return country;
	}

	public void setCountry(String country) {
		this.country = country;
	}

	public String getCountryId() {
		return countryId;
	}

	public void setCountryId(String countryId) {
		this.countryId = countryId;
	}

	public String getProvince() {
		return province;
	}

	public void setProvince(String province) {
		this.province = province;
	}

	public String getCity() {
		return city;
	}

	public void setCity(String city) {
		this.city = city;
	}

	public String getArea() {
		return area;
	}

	public void setArea(String area) {
		this.area = area;
	}

	@Override
	public String toString() {
		try {
			return JsonUtil.toJson(this);
		} catch (Exception e) {
			return "Address ["
			        + (country != null ? "country=" + country + ", " : "")
			        + (countryId != null ? "countryId=" + countryId + ", " : "")
			        + (province != null ? "province=" + province + ", " : "")
			        + (city != null ? "city=" + city + ", " : "")
			        + (area != null ? "area=" + area + ", " : "")
			        + "]";
		}
	}

}
