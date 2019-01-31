package cn.jerry.fabric.api.fabric.convert;

import cn.jerry.fabric.api.fabric.util.DateUtil;
import cn.jerry.fabric.api.fabric.util.StringUtils;
import org.springframework.core.convert.converter.Converter;

import java.util.Date;

public class StringToDateConverter implements Converter<String, Date> {
	@Override
	public Date convert(String s) {
		if (StringUtils.isBlank(s))
		return null;

		return DateUtil.parseDateAuto(s);
	}
}
