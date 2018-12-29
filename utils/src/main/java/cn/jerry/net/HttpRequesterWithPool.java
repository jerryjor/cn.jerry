package cn.jerry.net;

import cn.jerry.logging.LogManager;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpStatus;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.GzipDecompressingEntity;
import org.apache.http.client.methods.*;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

public class HttpRequesterWithPool {
    private static Logger logger = LogManager.getLogger();

    private static final Charset DEFAULT_CHARSET = StandardCharsets.UTF_8;

    private HttpUriRequest request;
    private Charset charset;
    private CloseableHttpClient httpClient;

    private HttpRequesterWithPool(HttpUriRequest request, Charset charset, String proxyHost, Integer proxyPort) {
        this.request = request;
        this.charset = charset;
        // 创建默认的httpClient实例.
        if (proxyHost == null || proxyHost.trim().isEmpty() || proxyPort == null) {
            this.httpClient = HttpConnectionManager.getHttpClient();
        } else {
            this.httpClient = HttpConnectionManager.getHttpClientWithProxy(proxyHost, proxyPort);
        }
    }

    /**
     * 执行请求
     *
     * @return response content
     * @throws IOException @see CloseableHttpClient.execute(HttpUriRequest)
     */
    public String doRequest() throws IOException {
        logger.debug("doRequest start, uri:[" + request.getURI() + "]");
        long st = System.currentTimeMillis();

        String responseStr;
        CloseableHttpResponse response = null;
        InputStream in = null;
        try {
            response = httpClient.execute(request);
            int statusCode = response.getStatusLine().getStatusCode();
            if (HttpStatus.SC_OK == statusCode) {
                in = response.getEntity().getContent();
                if (withGzip(response)) {
                    responseStr = EntityUtils.toString(
                            new GzipDecompressingEntity(response.getEntity()), charset);
                } else {
                    responseStr = EntityUtils.toString(response.getEntity(), charset);
                }
            } else {
                request.abort();
                responseStr = "{\"server status\":" + statusCode + "}";
                logger.warn("doRequest, server status:" + statusCode + ", uri:[" + request.getURI() + "]");
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
        return responseStr;
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

    public static class HttpUriRequestBuilder {
        private boolean post = true;
        private String url;
        private Map<String, String> headers = new HashMap<>();
        private Map<String, String> params = new HashMap<>();
        private Charset charset;
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

        public HttpUriRequestBuilder throughGet(Charset charset) {
            this.post = false;
            return setCharset(charset);
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

        public HttpUriRequestBuilder addParams(Map<String, String> params) {
            if (params != null) {
                this.params.putAll(params);
            }
            return this;
        }

        public HttpUriRequestBuilder addParam(String key, String value) {
            if (this.params == null) this.params = new HashMap<>();
            if (key != null && value != null) {
                this.params.put(key, value);
            }
            return this;
        }

        public HttpUriRequestBuilder setCharset(Charset charset) {
            this.charset = charset == null ? DEFAULT_CHARSET : charset;
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

        public HttpRequesterWithPool build() {
            HttpRequestBase request;
            if (this.post) {
                request = new HttpPost(url);
                if (!this.params.isEmpty()) {
                    MultipartEntityBuilder builder = MultipartEntityBuilder.create();
                    for (Entry<String, String> param : this.params.entrySet()) {
                        builder.addTextBody(param.getKey(), param.getValue());
                    }
                    HttpEntity httpEntity = builder.build();
                    ((HttpPost) request).setEntity(httpEntity);
                }
            } else {
                if (!this.params.isEmpty()) {
                    StringBuilder paramBuffer = new StringBuilder();
                    for (Entry<String, String> param : this.params.entrySet()) {
                        String value = null;
                        try {
                            value = URLEncoder.encode(param.getValue(), this.charset.name());
                        } catch (UnsupportedEncodingException e) {
                            logger.error("Unsupported encoding: " + this.charset.name());
                        }
                        paramBuffer.append("&").append(param.getKey()).append("=").append(value);
                    }
                    this.url += paramBuffer.replace(0, 1, "?").toString();
                }
                request = new HttpGet(this.url);
            }
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

            return new HttpRequesterWithPool(request, this.charset, this.proxyHost, this.proxyPort);
        }
    }
}