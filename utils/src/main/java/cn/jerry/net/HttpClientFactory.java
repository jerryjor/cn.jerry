package cn.jerry.net;

import cn.jerry.logging.LogManager;
import org.apache.http.HttpHost;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.DefaultHttpRequestRetryHandler;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.DefaultProxyRoutePlanner;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.logging.log4j.Logger;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class HttpClientFactory {
    private static Logger logger = LogManager.getLogger();

	private static final int MAX_TOTAL = 50;
	private static final int MAX_PER_ROUTE = 30;

    private static final String[] SSL_CONTEXTS = new String[]{"TLSv1.3", "TLSv1.2", "TLSv1.1"};
	private static final PoolingHttpClientConnectionManager CONN_MGR;
	private static final CloseableHttpClient DEFAULT_CLIENT;

	static {
        RegistryBuilder<ConnectionSocketFactory> register = RegistryBuilder
                .<ConnectionSocketFactory>create()
                .register("http", new PlainConnectionSocketFactory());
        for (String contextName : SSL_CONTEXTS) {
            SSLConnectionSocketFactory sslFactory = buildSslFactory(contextName);
            if (sslFactory != null) {
                register.register("https", sslFactory);
                logger.info("sslFactory {} is registered.", contextName);
                break;
            }
        }
        Registry<ConnectionSocketFactory> socketFactoryRegistry = register.build();
        CONN_MGR = new PoolingHttpClientConnectionManager(socketFactoryRegistry);
        CONN_MGR.setMaxTotal(MAX_TOTAL);
		// 每个路由最大连接数，默认是1
        CONN_MGR.setDefaultMaxPerRoute(MAX_PER_ROUTE);
		// 可以针对特定的路由配置单独的限制
		// cm.setMaxPerRoute(new HttpRoute(new HttpHost("jerry.cn")) , MAX_PER_ROUTE)
		logger.info("init connection manager finished.");
        DEFAULT_CLIENT = HttpClients.custom()
                .setRetryHandler(new DefaultHttpRequestRetryHandler(0, false))
                .setConnectionManager(CONN_MGR)
                .build();

        Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(new ConnectionReleaseThread(), 1L, 1L,
                TimeUnit.SECONDS);
	}

	/**
	 * https访问
	 *
	 * @return SSLConnectionSocketFactory
	 */
    private static SSLConnectionSocketFactory buildSslFactory(String contextName) {
        SSLConnectionSocketFactory factory = null;
        try {
            SSLContext context = SSLContext.getInstance(contextName);
			context.init(null, new TrustManager[] { new BlankCertManager() }, new SecureRandom());
			factory = new SSLConnectionSocketFactory(context, new BlankHostVerifier());
		} catch (NoSuchAlgorithmException e) {
			logger.error("create SSLContext failed.", e);
		} catch (KeyManagementException e) {
			logger.error("init SSLContext failed.", e);
		}
		return factory;
	}

	/**
	 * 定时释放长时间不活动的连接
	 */
	private static void releaseExpiredConn() {
        CONN_MGR.closeExpiredConnections();
	}

	/**
	 * 获取一个连接
	 * 
	 * @return CloseableHttpClient
	 */
	public static CloseableHttpClient getHttpClient(String host, Integer port, boolean pooled) {
		if (port == null && pooled) {
		    return DEFAULT_CLIENT;
        }

        HttpClientBuilder builder = HttpClients.custom()
                .setRetryHandler(new DefaultHttpRequestRetryHandler(0, false));
        if (host != null && !host.trim().isEmpty() && port != null) {
            builder.setRoutePlanner(new DefaultProxyRoutePlanner(new HttpHost(host, port)));
        }
        if (pooled) {
            builder.setConnectionManager(CONN_MGR);
        }
		return builder.build();
	}

    static class ConnectionReleaseThread implements Runnable {

        @Override
        public void run() {
            releaseExpiredConn();
        }

    }
}
