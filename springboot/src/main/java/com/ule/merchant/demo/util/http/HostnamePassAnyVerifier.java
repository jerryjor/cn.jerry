package com.ule.merchant.demo.util.http;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSession;

public class HostnamePassAnyVerifier implements HostnameVerifier {

	@Override
	public boolean verify(String hostname, SSLSession session) {
		// // 仅通过ule的域名
		// if (hostname != null
		// && (hostname.endsWith("ule.com")
		// || hostname.endsWith("ulechina.com")
		// || hostname.endsWith("ulechina.tom.com"))) {
		// System.out.println("hostname[" + hostname + "] is ule, pass.");
		// return true;
		// } else {
		// System.out.println("hostname[" + hostname + "] is not ule, refused.");
		// return false;
		// }
		return true;
	}

}
