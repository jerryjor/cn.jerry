package cn.jerry.file;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class FileUtil {

	/**
	 * 保存数据到文件
	 * 
	 * @param data
	 * @param file
	 * @throws IOException
	 */
	public static void saveToFile(byte[] data, File file) throws IOException {
		FileOutputStream fos = null;
		try {
			fos = new FileOutputStream(file);
			fos.write(data);
		} catch (IOException e) {
			throw e;
		} finally {
			try {
				fos.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * 读取数据
	 * 
	 * @param bytes
	 * @param charset
	 * @return
	 * @throws IOException
	 */
	public static List<String> read(byte[] bytes, String charset) throws IOException {
		List<String> records = new ArrayList<String>();
		BufferedReader br = null;
		try {
			InputStreamReader isr;
			if (charset == null || charset.isEmpty()) {
				isr = new InputStreamReader(new ByteArrayInputStream(bytes));
			} else {
				isr = new InputStreamReader(new ByteArrayInputStream(bytes), charset);
			}
			br = new BufferedReader(isr);
			String lineStr = null;
			while ((lineStr = br.readLine()) != null) {
				records.add(lineStr);
			}
			return records;
		} catch (IOException e) {
			throw e;
		} finally {
			if (br != null) {
				try {
					br.close();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}

	/**
	 * 读取文本文件
	 * 
	 * @param file
	 * @return
	 */
	public static List<String> readTextByLine(File file) {
		if (file == null || !file.exists()) {
			throw new RuntimeException("file not exists. file:"
					+ (file == null ? "null" : file.getAbsolutePath()));
		}

		List<String> lines = new ArrayList<String>();
		BufferedReader br = null;
		try {
			FileReader fr = new FileReader(file);
			br = new BufferedReader(fr);
			String lineStr = null;
			while ((lineStr = br.readLine()) != null) {
				lines.add(lineStr);
			}
			return lines;
		} catch (Exception e) {
			throw new RuntimeException("read file content failed. file:" + file.getAbsolutePath());
		} finally {
			try {
				br.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
}
