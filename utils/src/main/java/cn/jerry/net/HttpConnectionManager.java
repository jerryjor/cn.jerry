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
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

class HttpConnectionManager {
    private static Logger logger = LogManager.getLogger();

	private static final int MAX_TOTAL = 32;
	private static final int MAX_PER_ROUTE = 8;

	private static PoolingHttpClientConnectionManager cm;
    private static ScheduledExecutorService connReleaseTask = Executors.newSingleThreadScheduledExecutor();

	static {
		Registry<ConnectionSocketFactory> socketFactoryRegistry = RegistryBuilder
		        .<ConnectionSocketFactory> create()
		        .register("https", buildSslFactory())
		        .register("http", new PlainConnectionSocketFactory())
		        .build();
		cm = new PoolingHttpClientConnectionManager(socketFactoryRegistry);
		cm.setMaxTotal(MAX_TOTAL);
		// 每个路由最大连接数，默认是1
		cm.setDefaultMaxPerRoute(MAX_PER_ROUTE);
		// 可以针对特定的路由配置单独的限制
		// cm.setMaxPerRoute(new HttpRoute(new HttpHost("jerry.cn")) , MAX_PER_ROUTE);
		logger.info("init connection manager finished.");

        connReleaseTask.scheduleAtFixedRate(new ConnectionReleaseThread(), 1L, 5 * 60L,
                TimeUnit.SECONDS);
	}

	/**
	 * https访问
	 *
	 * @return SSLConnectionSocketFactory
	 */
	private static SSLConnectionSocketFactory buildSslFactory() {
		SSLConnectionSocketFactory factory = null;
		try {
			SSLContext context = SSLContext.getInstance("SSL");
			context.init(null, new TrustManager[] { new TrustAnyManager() }, new SecureRandom());
			factory = new SSLConnectionSocketFactory(context, new HostnamePassAnyVerifier());
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
		logger.info("release expired connection...");
		cm.closeExpiredConnections();
	}

	/**
	 * 获取一个连接
	 * 
	 * @return CloseableHttpClient
	 */
	static CloseableHttpClient getHttpClient(String host, Integer port, boolean pooled) {
		logger.info("getHttpClient...........");

        HttpClientBuilder builder = HttpClients.custom()
                .setRetryHandler(new DefaultHttpRequestRetryHandler(0, false));
        if (host != null && !host.trim().isEmpty() & port != null) {
            builder.setRoutePlanner(new DefaultProxyRoutePlanner(new HttpHost(host, port)));
        }
        if (pooled) {
            builder.setConnectionManager(cm);
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
