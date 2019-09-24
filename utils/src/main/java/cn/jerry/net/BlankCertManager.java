package cn.jerry.net;

import javax.net.ssl.X509TrustManager;
import java.security.cert.CertStore;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

public class BlankCertManager implements X509TrustManager {

    private CertStore store = loadTrustStore();

    private CertStore loadTrustStore() {
        return null;
    }

    @Override
	public void checkClientTrusted(X509Certificate[] chain, String authType)
	        throws CertificateException {
        if (store != null && chain == null) {
            throw new CertificateException("Certificate chain is null.");
        }
	}

	@Override
	public void checkServerTrusted(X509Certificate[] chain, String authType)
	        throws CertificateException {
        if (store != null && chain == null) {
            throw new CertificateException("Certificate chain is null.");
        }
	}

	@Override
	public X509Certificate[] getAcceptedIssuers() {
		return null;
	}

}
