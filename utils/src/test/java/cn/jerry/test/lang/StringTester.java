package cn.jerry.test.lang;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

public class StringTester {
	private static String line = "LINE";
	private static String text = "000000||20160826|0|0001tmp/13.txt|1| 接水点号: [000186385 ] 最小可用票号: [ ] 已打印行号：[ ]LINE 用户名称: [苏喜漫 ] 手机号码: [ ]LINE 用户地址: [雨花区长重社区2区4栋208() ]LINE 欠费笔数: [ 2]LINE 欠费金额: [ 13.16] 其中违约金: [ 0]LINE 上次节余: [ 0.76] 计费开始日期: [20110720 ]LINE 应缴金额: [ 12.40] 结束日期: [20110820 ]LINELINE--------------------------------欠--费--明--细--------------------------------LINE 欠费年月 欠费金额 水费标识号 其中.欠水费..违约金(滞纳金)LINE [20110720 ] [ 13.16] [ ] [ ] [ ]LINE [20110820 ] [ 13.16] [ ] [ ] [ ]|";

	public static void main(String[] args) {
//		int fileEnd = text.lastIndexOf("|");
//		int fileStart = text.lastIndexOf("|", fileEnd - 1);
//		String fileContent = text.substring(fileStart + 1, fileEnd);
//		System.out.println("fileContent:" + fileContent + "\n");
//
//		LinkedHashMap<String, Object> contentMap = readContent(fileContent, 13);
//		System.out.println("content:" + JsonUtil.toJsonSilently(contentMap));
//
//		fileContent = fileContent.replaceAll("][^\\[|\\]]+\\[", "][").replaceAll(" ", "");
//		fileContent = fileContent.substring(fileContent.indexOf("["));
//		System.out.println(fileContent);
	}

	/**
	 * 解析小结
	 * 
	 * @param fullText 文件内容
	 * @param summaryCount 小结部分内容数量
	 * @return
	 */
	private static LinkedHashMap<String, Object> readContent(String fullText, int summaryCount) {
		LinkedHashMap<String, Object> summary = new LinkedHashMap<String, Object>();
		if (fullText == null || fullText.isEmpty()) return summary;

		int start = 0, end = 0;
		for (int i = 0; i < summaryCount; i++) {
			start = end;
			end = fullText.indexOf("]", start + 1);
			if (end == -1) break;

			String[] nodeKv = fullText.substring(start, end).split("\\[");
			summary.put(nodeKv[0].replaceAll(line + "|其中|:|：|]", "").trim(), nodeKv[1].trim());
		}
		// System.out.println("summary:\n" + summary);

		// 读取detail
		if (end != -1) {
			summary.put("detail", readDetail(fullText.substring(end + 1)));
		}
		return summary;
	}

	/**
	 * 解析明细
	 * 
	 * @param detailText
	 * @return
	 */
	private static List<LinkedHashMap<String, String>> readDetail(String detailText) {
		List<LinkedHashMap<String, String>> details = new ArrayList<LinkedHashMap<String, String>>();
		if (detailText == null || detailText.isEmpty()) return details;

		int headerEnd = detailText.indexOf("[");
		String[] headerLines = detailText.substring(0, headerEnd).split(line);
		String headerLine = null;
		for (int i = headerLines.length - 1; i >= 0; i--) {
			if (headerLines[i] != null && !headerLines[i].trim().isEmpty()) {
				headerLine = headerLines[i];
				break;
			}
		}
		if (headerLine == null) {
			System.out.println("detail:" + detailText);
			throw new RuntimeException("read detail failed, cannot find detail header.");
		}

		headerLine = headerLine.replaceAll("其中|\\.", " ").replaceAll(" +", " ").trim();
		// System.out.println("headerLine:" + headerLine);
		String[] headers = headerLine.split(" ");
		String[] detailsData = detailText.substring(headerEnd).replaceAll(line + "|\\[", " ")
		        .replaceAll(" +", " ").split("]");
		LinkedHashMap<String, String> detailMap = new LinkedHashMap<String, String>();
		for (int i = 0; i < detailsData.length; i++) {
			detailMap.put(headers[i % headers.length], detailsData[i].trim());
			if ((i + 1) % headers.length == 0) {
				if (!detailMap.isEmpty()) {
					details.add(detailMap);
				}
				detailMap = new LinkedHashMap<String, String>();
			}
		}
		// System.out.println("details:\n" + details);
		return details;
	}
}
