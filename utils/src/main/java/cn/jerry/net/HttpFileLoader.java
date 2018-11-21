package cn.jerry.net;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

public class HttpFileLoader {

	/**
	 * 下载Http服务器文件
	 * 
	 * @param httpUrl
	 * @throws IOException
	 */
	public byte[] loadFromHttp(String httpUrl) throws IOException {
		HttpURLConnection conn = null;
		BufferedInputStream bis = null;
		ByteArrayOutputStream bos = null;
		byte[] data = null;
		try {
			URL url = new URL(httpUrl);
			conn = (HttpURLConnection) url.openConnection();
			conn.setConnectTimeout(20 * 1000); // 设置超时间为20秒
			// 防止屏蔽程序抓取而返回403错误
			conn.setRequestProperty("User-Agent",
					"Mozilla/4.0 (compatible; MSIE 5.0; Windows NT; DigExt)");
			// 得到输入流
			bis = new BufferedInputStream(conn.getInputStream());
			// 获取字节数组
			byte[] buffer = new byte[1024];
			int len = 0;
			bos = new ByteArrayOutputStream();
			while ((len = bis.read(buffer)) != -1) {
				bos.write(buffer, 0, len);
			}
			data = bos.toByteArray();
		} catch (IOException e) {
			throw e;
		} finally {
			if (bis != null) {
				try {
					bis.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			if (bos != null) {
				try {
					bos.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			if (conn != null) {
				conn.disconnect();
			}
		}
		return data;
	}

}
