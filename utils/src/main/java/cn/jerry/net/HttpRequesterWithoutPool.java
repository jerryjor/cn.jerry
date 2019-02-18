package cn.jerry.net;

import cn.jerry.logging.LogManager;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpStatus;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.GzipDecompressingEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.DefaultHttpRequestRetryHandler;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.DefaultProxyRoutePlanner;
import org.apache.http.util.EntityUtils;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

public class HttpRequesterWithoutPool implements AutoCloseable {
    private static Logger logger = LogManager.getLogger();

    private static final Charset DEFAULT_CHARSET = StandardCharsets.UTF_8;

    private HttpPost request;
    private Charset charset;
    private CloseableHttpClient httpClient;

    private HttpRequesterWithoutPool(HttpPost request, Charset charset, String proxyHost, Integer proxyPort) {
        this.request = request;
        this.charset = charset;
        // 创建默认的httpClient实例.
        if (proxyHost == null || proxyHost.trim().isEmpty() || proxyPort == null) {
            httpClient = HttpClientBuilder.create()
                    .setRetryHandler(new DefaultHttpRequestRetryHandler(0, false))
                    .build();
        } else {
            httpClient = HttpClientBuilder.create()
                    .setRetryHandler(new DefaultHttpRequestRetryHandler(0, false))
                    .setRoutePlanner(new DefaultProxyRoutePlanner(new HttpHost(proxyHost, proxyPort)))
                    .build();
        }
    }

    /**
     * 执行普通请求
     *
     * @return response content
     * @throws IOException @see CloseableHttpClient.execute(HttpUriRequest)
     */
    public StringHttpResponse doPost(Map<String, String> params) throws IOException {
        return doPost(params, null, null);
    }

    /**
     * 执行文件上传请求
     *
     * @return response content
     * @throws IOException @see CloseableHttpClient.execute(HttpUriRequest)
     */
    public StringHttpResponse doPost(Map<String, String> params, String fileParamName, String filePath)
            throws IOException {
        logger.debug("doRequest start, uri:[" + request.getURI() + "]");
        long st = System.currentTimeMillis();

        StringHttpResponse strResponse = new StringHttpResponse();
        CloseableHttpResponse response = null;
        InputStream in = null;
        try {
            request.setEntity(null);
            MultipartEntityBuilder builder = MultipartEntityBuilder.create().setCharset(charset);
            if (fileParamName != null && !fileParamName.isEmpty()
                    && filePath != null && !(filePath = filePath.trim()).isEmpty()) {
                File file = new File(filePath);
                if (file.exists()) {
                    builder.addBinaryBody(fileParamName, file);
                }
            }
            if (!params.isEmpty()) {
                for (Entry<String, String> param : params.entrySet()) {
                    builder.addTextBody(param.getKey(), param.getValue());
                }
            }
            HttpEntity httpEntity = builder.build();
            request.setEntity(httpEntity);

            response = httpClient.execute(request);
            strResponse.setStatusCode(response.getStatusLine().getStatusCode());
            in = response.getEntity().getContent();
            HttpEntity entity = withGzip(response)
                    ? new GzipDecompressingEntity(response.getEntity()) : response.getEntity();
            strResponse.setEntity(EntityUtils.toString(entity, charset));
            if (HttpStatus.SC_OK != strResponse.getStatusCode()) {
                logger.warn("doRequest, server status: {}, uri: [{}], entity : {}", strResponse.getStatusCode(),
                        request.getURI(), (isHtml(strResponse.getEntity()) ? "a html" : strResponse.getEntity()));
            }
        } catch (IOException e) {
            request.abort();
            logger.error("doRequest failed, uri:[" + request.getURI() + "]", e);
            throw e;
        } finally {
            // in.close()作用就是将用完的连接释放，下次请求可以复用
            // 这里特别注意的是，如果不使用in.close()而仅仅使用response.close()，
            // 结果就是连接会被关闭，并且不能被复用，就失去了采用连接池的意义。
            if (in != null) {
                try {
                    in.close();
                } catch (IOException ioe) {
                    logger.error("doRequest, close response failed.", ioe);
                }
            }
            if (response != null) {
                try {
                    response.close();
                } catch (IOException ioe) {
                    logger.error("doRequest, close response failed.", ioe);
                }
            }
        }

        long et = System.currentTimeMillis();
        logger.info("doRequest finished, uri:[" + request.getURI() + "], cost time:" + (et - st) + "ms.");
        return strResponse;
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

    private boolean isHtml(String str) {
        return str != null && !(str = str.trim()).isEmpty() && str.charAt(0) == '<';
    }

    /**
     * 关闭连接,释放资源
     */
    @Override
    public void close() {
        if (httpClient == null) return;

        try {
            httpClient.close();
        } catch (IOException ioe) {
            logger.error("close http client failed.", ioe);
        }

        httpClient = null;
    }

    public static class HttpUriRequestBuilder {
        private String url;
        private Map<String, String> headers = new HashMap<>();
        private Map<String, String> params = new HashMap<>();
        private Charset charset = DEFAULT_CHARSET;
        private Integer socketTimeout;
        private Integer connTimeout;
        private Integer connReqTimeout;
        private boolean withGzip = false;
        private String proxyHost;
        private Integer proxyPort;
        private Map<String, String> cookies = new HashMap<>();

        public HttpUriRequestBuilder(String url) throws IOException {
            if (url == null || (url = url.trim()).isEmpty()) {
                throw new IOException("url cannot be empty.");
            }
            this.url = url;
        }

        public HttpUriRequestBuilder addHeaders(Map<String, String> headers) {
            if (headers != null) {
                this.headers.putAll(headers);
            }
            return this;
        }

        public HttpUriRequestBuilder addHeader(String key, String value) {
            if (key != null && value != null) {
                this.headers.put(key, value);
            }
            return this;
        }

        public HttpUriRequestBuilder setCharset(Charset charset) {
            if (charset != null) this.charset = charset;
            return this;
        }

        public HttpUriRequestBuilder setSocketTimeout(Integer socketTimeout) {
            this.socketTimeout = socketTimeout;
            return this;
        }

        public HttpUriRequestBuilder setConnTimeout(Integer connTimeout) {
            this.connTimeout = connTimeout;
            return this;
        }

        public HttpUriRequestBuilder setConnReqTimeout(Integer connReqTimeout) {
            this.connReqTimeout = connReqTimeout;
            return this;
        }

        public HttpUriRequestBuilder withGzip() {
            this.withGzip = true;
            headers.put("Accept-Encoding", "gzip");
            return this;
        }

        public HttpUriRequestBuilder setProxyHost(String proxyHost) {
            this.proxyHost = proxyHost;
            return this;
        }

        public HttpUriRequestBuilder setProxyPort(Integer proxyPort) {
            this.proxyPort = proxyPort;
            return this;
        }

        public HttpUriRequestBuilder addCookie(String name, String value) {
            if (name != null && value != null) {
                this.cookies.put(name, value);
            }
            return this;
        }

        public HttpRequesterWithoutPool build() {
            HttpPost request = new HttpPost(url);
            if (!this.headers.isEmpty()) {
                for (Entry<String, String> header : this.headers.entrySet()) {
                    request.addHeader(header.getKey(), header.getValue());
                }
            }
            if (!this.cookies.isEmpty()) {
                for (Entry<String, String> header : this.headers.entrySet()) {
                    request.addHeader("Cookie", header.getKey() + "=" + header.getValue());
                }
            }
            // 设置超时
            RequestConfig.Builder timeoutBuilder = RequestConfig.custom();
            // socket读数据超时时间：从服务器获取响应数据的超时时间
            if (this.socketTimeout != null) {
                timeoutBuilder.setSocketTimeout(this.socketTimeout);
            }
            // 与服务器连接超时时间：httpclient会创建一个异步线程用以创建socket连接，此处设置该socket的连接超时时间
            if (this.connTimeout != null) {
                timeoutBuilder.setConnectTimeout(this.connTimeout);
            }
            //从连接池中获取连接的超时时间
            if (this.connReqTimeout != null) {
                timeoutBuilder.setConnectionRequestTimeout(this.connReqTimeout);
            }
            request.setConfig(timeoutBuilder.build());

            return new HttpRequesterWithoutPool(request, this.charset, this.proxyHost, this.proxyPort);
        }
    }
}