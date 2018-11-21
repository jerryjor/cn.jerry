package cn.jerry.compress;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang.StringUtils;

public class ZipUtil {

	/**
	 * 使用gzip进行压缩
	 * 
	 * @param primStr
	 * @param charset
	 * @return
	 */
	public static String gZip(String primStr, String charset) {
		if (StringUtils.isEmpty(primStr)) return "";

		ByteArrayOutputStream out = new ByteArrayOutputStream();

		GZIPOutputStream gzip = null;
		try {
			gzip = new GZIPOutputStream(out);
			gzip.write(getCompressBytes(primStr, charset));
		} catch (IOException e) {
			throw new RuntimeException(e);
		} finally {
			if (gzip != null) {
				try {
					gzip.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		try {
			return encode(out.toByteArray(), charset);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public static String gZipWithGbk(String primStr) {
		return gZip(primStr, "GBK");
	}

	/**
	 * 使用gzip进行解压缩
	 * 
	 * @param compressedStr
	 * @param charset
	 * @return
	 */
	public static String gUnzip(String compressedStr, String charset) {
		if (StringUtils.isEmpty(compressedStr)) return "";

		ByteArrayOutputStream out = new ByteArrayOutputStream();
		ByteArrayInputStream in = null;
		GZIPInputStream ginzip = null;
		byte[] compressed = null;
		String decompressed = null;
		try {
			compressed = decode(compressedStr, charset);
			in = new ByteArrayInputStream(compressed);
			ginzip = new GZIPInputStream(in);

			byte[] buffer = new byte[1024];
			int offset = -1;
			while ((offset = ginzip.read(buffer)) != -1) {
				out.write(buffer, 0, offset);
			}
			decompressed = toOutString(out.toByteArray(), charset);
		} catch (IOException e) {
			throw new RuntimeException(e);
		} finally {
			if (ginzip != null) {
				try {
					ginzip.close();
				} catch (IOException e) {
				}
			}
			if (in != null) {
				try {
					in.close();
				} catch (IOException e) {
				}
			}
			if (out != null) {
				try {
					out.close();
				} catch (IOException e) {
				}
			}
		}

		return decompressed;
	}

	public static String gUnzipWithGbk(String compressedStr) {
		return gUnzip(compressedStr, "GBK");
	}

	/**
	 * 使用zip进行压缩
	 * 
	 * @param primStr 压缩前的文本
	 * @param charset
	 * @return 返回压缩后的文本
	 */
	public static final String zip(String primStr, String charset) {
		if (StringUtils.isEmpty(primStr)) return "";

		byte[] compressed;
		ByteArrayOutputStream out = null;
		ZipOutputStream zout = null;
		String compressedStr = null;
		try {
			out = new ByteArrayOutputStream();
			zout = new ZipOutputStream(out);
			zout.putNextEntry(new ZipEntry("0"));
			zout.write(getCompressBytes(primStr, charset));
			zout.closeEntry();
			compressed = out.toByteArray();
			compressedStr = encode(compressed, charset);
		} catch (IOException e) {
			throw new RuntimeException(e);
		} finally {
			if (zout != null) {
				try {
					zout.close();
				} catch (IOException e) {
				}
			}
			if (out != null) {
				try {
					out.close();
				} catch (IOException e) {
				}
			}
		}
		return compressedStr;
	}

	public static final String zipWithGbk(String primStr) {
		return zip(primStr, "GBK");
	}

	/**
	 * 使用zip进行解压缩
	 * 
	 * @param compressedStr 压缩后的文本
	 * @param charset
	 * @return 解压后的字符串
	 */
	public static final String unzip(String compressedStr, String charset) {
		if (StringUtils.isEmpty(compressedStr)) return "";

		ByteArrayOutputStream out = null;
		ByteArrayInputStream in = null;
		ZipInputStream zin = null;
		String decompressed = null;
		try {
			byte[] compressed = decode(compressedStr, charset);
			out = new ByteArrayOutputStream();
			in = new ByteArrayInputStream(compressed);
			zin = new ZipInputStream(in);
			zin.getNextEntry();
			byte[] buffer = new byte[1024];
			int offset = -1;
			while ((offset = zin.read(buffer)) != -1) {
				out.write(buffer, 0, offset);
			}
			decompressed = toOutString(out.toByteArray(), charset);
		} catch (IOException e) {
			throw new RuntimeException(e);
		} finally {
			if (zin != null) {
				try {
					zin.close();
				} catch (IOException e) {
				}
			}
			if (in != null) {
				try {
					in.close();
				} catch (IOException e) {
				}
			}
			if (out != null) {
				try {
					out.close();
				} catch (IOException e) {
				}
			}
		}
		return decompressed;
	}

	public static final String unzipWithGbk(String compressedStr) {
		return unzip(compressedStr, "GBK");
	}

	public static byte[] getCompressBytes(String str, String charset) throws IOException {
		return str.getBytes(charset);
	}

	public static String toOutString(byte[] bytes, String charset) throws IOException {
		return new String(bytes, charset);
	}

	private static String encode(byte[] bytes, String charset) throws IOException {
		return new String(Base64.encodeBase64(bytes), charset);
	}

	private static byte[] decode(String str, String charset) throws IOException {
		return Base64.decodeBase64(str.getBytes(charset));
	}
}