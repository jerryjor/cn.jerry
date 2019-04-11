package cn.jerry.net;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSession;

class HostnamePassAnyVerifier implements HostnameVerifier {

	@Override
	public boolean verify(String hostname, SSLSession session) {
		// // 仅通过jerry.cn的域名
		// if (hostname != null
		// && (hostname.endsWith("jerry.cn")
		// System.out.println("hostname[" + hostname + "] is jerry.cn, pass.");
		// return true;
		// } else {
		// System.out.println("hostname[" + hostname + "] is not jerry.cn, refused.");
		// return false;
		// }
		return true;
	}

}
