package cn.jerry.test.net;

import java.io.File;
import java.io.IOException;
import java.util.List;

import cn.jerry.file.FileUtil;
import cn.jerry.net.FtpFileLoader;
import cn.jerry.net.HttpFileLoader;

public class FtpTester {
	public static void main(String[] args) {
		FtpTester tester = new FtpTester();

		// byte[] content = tester.testDownloadFtp("119.254.20.12", 21, "youle", "youle121@121",
		// "/",
		// "youle_20141004_ALL.txt", "ISO-8859-1");
		// List<String> records = tester.testReadText(content, "ISO-8859-1");
		byte[] content = tester
				.testDownloadHttp("https://raw.githubusercontent.com/sparanoid/7z/master/zh-cn/History.txt");
		tester.testSaveFile(content);
	}

	public byte[] testDownloadHttp(String url) {
		HttpFileLoader loader = new HttpFileLoader();
		try {
			return loader.loadFromHttp(url);
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}

	public byte[] testDownloadFtp(String host, Integer port, String user, String pwd,
			String baseDir,
			String fileName, String charset) {
		FtpFileLoader loader = new FtpFileLoader();
		byte[] content = null;
		// 19pay
		try {
			content = loader.download(host, port, user, pwd, baseDir, fileName, charset);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return content;
	}

	public List<String> testReadText(byte[] content, String charset) {
		List<String> records = null;
		try {
			records = FileUtil.read(content, charset);
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.out.println(records.get(0));
		return records;
	}

	public void testSaveFile(byte[] content) {
		try {
			FileUtil.saveToFile(content, new File("/data/test/test.txt"));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
