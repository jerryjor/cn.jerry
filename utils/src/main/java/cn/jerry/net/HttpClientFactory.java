package cn.jerry.net;

import cn.jerry.logging.LogManager;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.client.entity.GzipDecompressingEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpRequestBase;
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
import org.apache.http.util.EntityUtils;
import org.apache.logging.log4j.Logger;

import javax.net.ssl.*;
import java.io.*;
import java.nio.charset.Charset;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.CertStore;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class HttpClientFactory {
    private static final Logger logger = LogManager.getLogger(HttpClientFactory.class);

    private static final String[] SSL_CONTEXTS = new String[]{"TLSv1.3", "TLSv1.2", "TLSv1.1"};
    private static final int MAX_TOTAL = 50;
    private static final int MAX_PER_ROUTE = 30;

    private static final PoolingHttpClientConnectionManager CONN_MGR;
    private static final Client DEFAULT_CLIENT;

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
        CloseableHttpClient dftClient = HttpClients.custom()
                .setRetryHandler(new DefaultHttpRequestRetryHandler(0, false))
                .setConnectionManager(CONN_MGR)
                .build();
        DEFAULT_CLIENT = new Client(dftClient, true);

        Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(new ConnectionReleaseThread(), 1L, 1L,
                TimeUnit.SECONDS);
    }

    private HttpClientFactory() {
        super();
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
            context.init(null, new TrustManager[]{
                    new X509TrustManager() {
                        private final CertStore store = loadTrustStore();

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
                            return new X509Certificate[0];
                        }
                    }
            }, new SecureRandom());
            factory = new SSLConnectionSocketFactory(context, (hostname, session) -> {
                if (hostname == null) {
                    logger.info("hostname is null, refused.");
                    return false;
                } else {
                    return true;
                }
            });
        } catch (NoSuchAlgorithmException e) {
            logger.warn("No {} algorithm available.", contextName);
        } catch (KeyManagementException e) {
            logger.error("init {} SSLContext failed.", contextName, e);
        }
        return factory;
    }

    /**
     * 获取一个连接
     *
     * @return CloseableHttpClient
     */
    public static Client getHttpClient(String host, Integer port, boolean pooled) {
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
        CloseableHttpClient httpClient = builder.build();
        return new Client(httpClient, pooled);
    }

    /**
     * 定时释放长时间不活动的连接
     */
    static class ConnectionReleaseThread implements Runnable {
        @Override
        public void run() {
            CONN_MGR.closeExpiredConnections();
        }
    }

    static class Client {
        private final CloseableHttpClient httpClient;
        private final boolean pooled;

        public Client(CloseableHttpClient client, boolean pooled) {
            this.httpClient = client;
            this.pooled = pooled;
        }

        public StringHttpResponse execute(HttpRequestBase request, Charset charset) throws IOException {
            StringHttpResponse strResponse = new StringHttpResponse();
            try {
                CloseableHttpResponse response = httpClient.execute(request);
                strResponse.setStatusCode(response.getStatusLine().getStatusCode());
                HttpEntity entity = withGzip(response)
                        ? new GzipDecompressingEntity(response.getEntity()) : response.getEntity();
                // EntityUtils.toString方法会获取InputStream并关闭
                strResponse.setEntity(EntityUtils.toString(entity, charset));
                return strResponse;
            } catch (IOException ioe) {
                request.abort();
                throw ioe;
            } finally {
                close();
            }
        }

        public ByteHttpResponse download(HttpRequestBase request) throws IOException {
            ByteHttpResponse byteResponse = new ByteHttpResponse();
            BufferedInputStream bis = null;
            ByteArrayOutputStream bos = null;
            try {
                CloseableHttpResponse response = httpClient.execute(request);
                byteResponse.setStatusCode(response.getStatusLine().getStatusCode());
                HttpEntity entity = withGzip(response)
                        ? new GzipDecompressingEntity(response.getEntity()) : response.getEntity();
                // EntityUtils.toString方法会获取InputStream并关闭
                bis = new BufferedInputStream(entity.getContent());
                byte[] buffer = new byte[1024];
                int len;
                bos = new ByteArrayOutputStream();
                while ((len = bis.read(buffer)) != -1) {
                    bos.write(buffer, 0, len);
                }
                byteResponse.setEntity(bos.toByteArray());
                return byteResponse;
            } catch (IOException ioe) {
                request.abort();
                throw ioe;
            } finally {
                close(bis);
                close(bos);
                close();
            }
        }

        private boolean withGzip(CloseableHttpResponse response) {
            Header[] headers = response.getHeaders("Content-Encoding");
            if (headers == null || headers.length == 0) return false;
            for (Header h : headers) {
                if (h.getValue().equals("gzip")) {
                    // 返回头中含有gzip
                    return true;
                }
            }
            return false;
        }

        public void close() {
            if (!pooled) {
                try {
                    httpClient.close();
                } catch (IOException ioe) {
                    logger.error("Failed to close http client.", ioe);
                }
            }
        }

        private void close(Closeable ca) {
            if (ca != null) {
                try {
                    ca.close();
                } catch (Exception e) {
                    logger.error("Failed to close {}", ca.getClass().getSimpleName());
                }
            }
        }
    }
}
