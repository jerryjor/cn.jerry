package cn.jerry.net;

import cn.jerry.logging.LogManager;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.GzipDecompressingEntity;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.*;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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
    public StringHttpResponse doRequest() throws IOException {
        logger.debug("doRequest start, uri:[" + request.getURI() + "]");
        long st = System.currentTimeMillis();

        StringHttpResponse strResponse = new StringHttpResponse();
        CloseableHttpResponse response = null;
        InputStream in = null;
        try {
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

    public static class HttpUriRequestBuilder {
        private boolean post = true;
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

        public HttpRequesterWithPool build() {
            HttpRequestBase request;
            List<NameValuePair> paramsPair = new ArrayList<>();
            for (Entry<String, String> param : this.params.entrySet()) {
                paramsPair.add(new BasicNameValuePair(param.getKey(), param.getValue()));
            }
            if (this.post) {
                request = new HttpPost(url);
                if (!this.params.isEmpty()) {
                    UrlEncodedFormEntity httpEntity = new UrlEncodedFormEntity(paramsPair, charset);
                    ((HttpPost) request).setEntity(httpEntity);
                }
            } else {
                if (!this.params.isEmpty()) {
                    this.url += "?" + URLEncodedUtils.format(paramsPair, charset);
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