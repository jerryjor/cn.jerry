package cn.jerry.test.filter;

import java.io.File;
import java.util.List;
import java.util.Set;

import cn.jerry.file.FileUtil;
import cn.jerry.model.SensitiveWords;

public class SensitiveWordsTester {
	private static String fileDir = "D:\\data\\test\\senstive.txt";
	private static SensitiveWords sw;

	/**
	 * 初始化敏感词库
	 */
	static {
		sw = new SensitiveWords();
		List<String> txtContents = FileUtil.readTextByLine(new File(fileDir));
		for (String one : txtContents) {
			sw.addNewWords(one);
		}
		// System.out.println(sw.toString());
	}

	public static void main(String[] args) {
		sw.removeWords("八九学潮");
		Set<String> sws = sw.lookup("八九学潮裸");
		System.out.println("包含的敏感词:" + sws);
	}
}
