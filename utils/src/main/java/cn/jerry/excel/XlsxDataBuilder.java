package cn.jerry.excel;

import java.util.Map;

public interface XlsxDataBuilder {

	void buildByRow(Map<String, Object> data);

}
