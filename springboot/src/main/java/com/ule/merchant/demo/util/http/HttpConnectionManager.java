package com.ule.merchant.demo.util.http;

import org.apache.http.HttpHost;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.DefaultHttpRequestRetryHandler;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.DefaultProxyRoutePlanner;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;


public class HttpConnectionManager {
    private static Logger logger = LogManager.getLogger();

    // 公司CLOSE_WAIT超过50会报警
    private static final int MAX_TOTAL = 32;
    private static final int MAX_PER_ROUTE = 8;

    private static PoolingHttpClientConnectionManager cm;

    private static ScheduledExecutorService connReleaseTask = Executors.newSingleThreadScheduledExecutor();

    static {
        Registry<ConnectionSocketFactory> socketFactoryRegistry = RegistryBuilder
                .<ConnectionSocketFactory>create()
                .register("https", buildSslFactory())
                .register("http", new PlainConnectionSocketFactory())
                .build();
        cm = new PoolingHttpClientConnectionManager(socketFactoryRegistry);
        cm.setMaxTotal(MAX_TOTAL);
        // 每个路由最大连接数，默认是1
        cm.setDefaultMaxPerRoute(MAX_PER_ROUTE);
        // 可以针对特定的路由配置单独的限制
        // cm.setMaxPerRoute(new HttpRoute(new HttpHost("www.ule.com")) , MAX_PER_ROUTE);
        logger.info("init connection manager finished.");

        connReleaseTask.scheduleAtFixedRate(new ConnectionReleaseThread(), 1L, 5 * 60L,
                TimeUnit.SECONDS);
    }

    /**
     * https访问
     *
     * @return
     */
    private static SSLConnectionSocketFactory buildSslFactory() {
        SSLConnectionSocketFactory factory = null;
        try {
            SSLContext context = SSLContext.getInstance("SSL");
            context.init(null, new TrustManager[]{new TrustAnyManager()}, new SecureRandom());
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
    public static void releaseExpiredConn() {
        logger.info("release expired connection...");
        cm.closeExpiredConnections();
    }

    /**
     * 获取一个连接
     *
     * @return
     */
    public static CloseableHttpClient getHttpClient() {
        logger.info("getHttpClient...........");

        CloseableHttpClient httpClient = HttpClients.custom()
                .setRetryHandler(new DefaultHttpRequestRetryHandler(0, false))
                .setConnectionManager(cm)
                .build();

        /* CloseableHttpClient httpClient = HttpClients.createDefault();//如果不采用连接池就是这种方式获取连接 */
        return httpClient;
    }

    /**
     * 获取一个含代理的连接
     *
     * @param host
     * @param port
     * @return
     */
    public static CloseableHttpClient getHttpClientWithProxy(String host, int port) {
        logger.info("getHttpClientWithProxy...........");
        //alterPoolSize();

        HttpHost proxy = new HttpHost(host, port);
        DefaultProxyRoutePlanner routePlanner = new DefaultProxyRoutePlanner(proxy);
        CloseableHttpClient httpClient = HttpClients.custom()
                .setRetryHandler(new DefaultHttpRequestRetryHandler(0, false))
                .setRoutePlanner(routePlanner)
                .setConnectionManager(cm)
                .build();

        return httpClient;
    }

    static class ConnectionReleaseThread implements Runnable {

        @Override
        public void run() {
            releaseExpiredConn();
        }
    }
}
