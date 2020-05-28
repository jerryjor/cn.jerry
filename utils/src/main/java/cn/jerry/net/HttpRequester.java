package cn.jerry.net;

import cn.jerry.json.JsonUtil;
import cn.jerry.log4j2.annotation.LogManager;
import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class HttpRequester {
    private static final Logger logger = LogManager.getLogger(HttpRequester.class);

    private static final Charset DEFAULT_CHARSET = StandardCharsets.UTF_8;

    private final RequestProperties properties;
    private final String proxyHost;
    private final Integer proxyPort;

    private HttpRequester(RequestProperties properties, String proxyHost, Integer proxyPort) {
        this.properties = properties;
        this.proxyHost = proxyHost;
        this.proxyPort = proxyPort;
    }

    /**
     * 执行get请求
     *
     * @return 服务器返回结果
     * @throws IOException 请求异常
     */
    public StringHttpResponse doGet() throws IOException {
        return doRequest(this.properties.createGet());
    }

    /**
     * 执行get请求，使用提供的新参数，忽略builder时提供的参数，但header及cookie等仍沿用builder时的配置
     *
     * @param params 新参数
     * @return 服务器返回结果
     * @throws IOException 请求异常
     */
    public StringHttpResponse doGet(Map<String, String> params) throws IOException {
        HttpGet request = this.properties.createGet();
        String url = this.properties.url;
        int index = url.indexOf('?');
        if (index != -1) {
            url = url.substring(0, index);
        }
        url += "?" + URLEncodedUtils.format(createNameValuePairs(params), this.properties.charset);
        request.setURI(URI.create(url));
        return doRequest(request);
    }

    /**
     * 执行post请求，使用builder时设置的参数
     *
     * @return 服务器返回结果
     * @throws IOException 请求异常
     */
    public StringHttpResponse doPost() throws IOException {
        return doRequest(this.properties.createPost());
    }

    /**
     * 执行post请求，使用提供的新参数，忽略builder时提供的参数，但header及cookie等仍沿用builder时的配置
     *
     * @param params 新参数
     * @return 服务器返回结果
     * @throws IOException 请求异常
     */
    public StringHttpResponse doPost(Map<String, String> params) throws IOException {
        HttpPost request = this.properties.createPost();
        HttpEntity httpEntity = this.properties.jsonBody
                ? createJsonEntity(params, this.properties.charset)
                : createSimpleEntity(params, this.properties.charset);
        request.setEntity(httpEntity);
        return doRequest(request);
    }

    /**
     * 执行post请求，使用提供的新参数，忽略builder时提供的参数，强制使用jsonBody方式传输，但header及cookie等仍沿用builder时的配置
     *
     * @param body 新参数
     * @return 服务器返回结果
     * @throws IOException 请求异常
     */
    public StringHttpResponse doJsonBodyPost(Object body) throws IOException {
        HttpPost request = this.properties.createPost();
        request.setHeader("Content-type", "application/json");
        HttpEntity httpEntity = createJsonEntity(body, this.properties.charset);
        request.setEntity(httpEntity);
        return doRequest(request);
    }

    /**
     * 执行文件上传请求，忽略builder时提供的参数，但header及cookie等仍沿用builder时的配置
     *
     * @param params        新参数
     * @param fileParamName 文件名
     * @param filePath      文件路径
     * @return 服务器返回结果
     * @throws IOException 请求异常
     */
    public StringHttpResponse doUpload(Map<String, String> params, String fileParamName, String filePath)
            throws IOException {
        HttpPost request = this.properties.createPost();
        HttpEntity httpEntity = createFileUploadEntity(params, this.properties.charset, fileParamName, filePath, null);
        request.setEntity(httpEntity);
        return doRequest(request);
    }

    /**
     * 执行文件上传请求，忽略builder时提供的参数，但header及cookie等仍沿用builder时的配置
     *
     * @param params        新参数
     * @param fileParamName 文件名
     * @param fileStream    文件输入流
     * @return 服务器返回结果
     * @throws IOException 请求异常
     */
    public StringHttpResponse doUpload(Map<String, String> params, String fileParamName, InputStream fileStream)
            throws IOException {
        HttpPost request = this.properties.createPost();
        HttpEntity httpEntity = createFileUploadEntity(params, this.properties.charset, fileParamName, null, fileStream);
        request.setEntity(httpEntity);
        return doRequest(request);
    }

    public ByteHttpResponse doDownload() throws IOException {
        long st = System.currentTimeMillis();
        HttpGet request = this.properties.createGet();
        // 防止屏蔽程序抓取而返回403错误
        request.addHeader("User-Agent", "Mozilla/4.0 (compatible; MSIE 5.0; Windows NT; DigExt)");
        HttpClientFactory.Client httpClient = HttpClientFactory.getHttpClient(this.proxyHost, this.proxyPort, this.properties.pooled);
        ByteHttpResponse response = httpClient.download(request);
        if (HttpStatus.SC_OK != response.getStatusCode()) {
            logger.warn("doRequest, server status: {}, uri: [{}]", response.getStatusCode(), request.getURI());
        }
        long cost = System.currentTimeMillis() - st;
        if (cost > 3000L) {
            logger.info("Requesting [{}] cost {} ms.", request.getURI(), cost);
        }
        return response;
    }

    /**
     * 执行请求
     *
     * @return response content
     * @throws IOException @see CloseableHttpClient.execute(HttpUriRequest)
     */
    private StringHttpResponse doRequest(HttpRequestBase request) throws IOException {
        long st = System.currentTimeMillis();
        HttpClientFactory.Client httpClient = HttpClientFactory.getHttpClient(this.proxyHost, this.proxyPort, this.properties.pooled);
        StringHttpResponse response = httpClient.execute(request, this.properties.charset);
        if (HttpStatus.SC_OK != response.getStatusCode()) {
            logger.warn("doRequest, server status: {}, uri: [{}], entity : {}", response.getStatusCode(),
                    request.getURI(), (isHtml(response.getEntity()) ? "a html" : response.getEntity()));
        }
        long cost = System.currentTimeMillis() - st;
        if (cost > 1000L) {
            logger.info("Requesting [{}] cost {} ms.", request.getURI(), cost);
        }
        return response;
    }

    private static boolean isHtml(String str) {
        if (str == null || str.isEmpty()) {
            return false;
        }
        str = str.trim();
        return !str.isEmpty() && str.charAt(0) == '<';
    }

    public static Builder newBuilder(String url) {
        return new Builder(url);
    }

    private static List<NameValuePair> createNameValuePairs(Map<String, String> params) {
        List<NameValuePair> paramsPair = new ArrayList<>();
        if (params == null || params.isEmpty()) return paramsPair;
        for (Entry<String, String> param : params.entrySet()) {
            paramsPair.add(new BasicNameValuePair(param.getKey(), param.getValue()));
        }
        return paramsPair;
    }

    private static HttpEntity createJsonEntity(Object body, Charset charset) throws IOException {
        String json = JsonUtil.isJson(body) ? (String) body : JsonUtil.toJson(body);
        return new StringEntity(json, charset);
    }

    private static HttpEntity createSimpleEntity(Map<String, String> params, Charset charset) {
        return new UrlEncodedFormEntity(createNameValuePairs(params), charset);
    }

    private static HttpEntity createFileUploadEntity(Map<String, String> params, Charset charset,
                                                     String fileParamName, String filePath, InputStream fileStream) {
        MultipartEntityBuilder builder = MultipartEntityBuilder.create().setCharset(charset);
        if (fileStream != null) {
            builder.addBinaryBody(fileParamName, fileStream);
        } else {
            File file = new File(filePath);
            if (file.exists()) {
                builder.addBinaryBody(fileParamName, file);
            }
        }
        if (params != null && !params.isEmpty()) {
            for (Entry<String, String> param : params.entrySet()) {
                builder.addTextBody(param.getKey(), param.getValue());
            }
        }
        return builder.build();
    }

    public static class Builder {
        private final RequestProperties properties = new RequestProperties();
        private String proxyHost;
        private Integer proxyPort;

        private Builder(String url) {
            if (StringUtils.isBlank(url)) {
                throw new NullPointerException("url cannot be empty.");
            }
            this.properties.url = url.trim();
        }

        public Builder notKeepAlive() {
            this.properties.pooled = false;
            return this;
        }

        public Builder withoutGzip() {
            this.properties.withGzip = false;
            return this;
        }

        public Builder addHeaders(Map<String, String> headers) {
            if (headers != null) {
                this.properties.headers.putAll(headers);
            }
            return this;
        }

        public Builder addHeader(String key, String value) {
            if (key != null && value != null) {
                this.properties.headers.put(key, value);
            }
            return this;
        }

        public Builder addParams(Map<String, String> params) {
            if (params != null) {
                this.properties.params.putAll(params);
            }
            return this;
        }

        public Builder addParam(String key, String value) {
            if (key != null && value != null) {
                this.properties.params.put(key, value);
            }
            return this;
        }

        public Builder addCookie(String name, String value) {
            if (name != null && value != null) {
                this.properties.cookies.put(name, value);
            }
            return this;
        }

        public Builder setCharset(Charset charset) {
            if (charset != null) this.properties.charset = charset;
            return this;
        }

        public Builder useJsonBody() {
            this.properties.jsonBody = true;
            return this;
        }

        public Builder setSocketTimeout(Integer socketTimeout) {
            this.properties.socketTimeout = socketTimeout;
            return this;
        }

        public Builder setConnTimeout(Integer connTimeout) {
            this.properties.connTimeout = connTimeout;
            return this;
        }

        public Builder setConnReqTimeout(Integer connReqTimeout) {
            this.properties.connReqTimeout = connReqTimeout;
            return this;
        }

        public Builder setProxyHost(String proxyHost) {
            this.proxyHost = proxyHost;
            return this;
        }

        public Builder setProxyPort(Integer proxyPort) {
            this.proxyPort = proxyPort;
            return this;
        }

        public HttpRequester build() {
            return new HttpRequester(this.properties, this.proxyHost, this.proxyPort);
        }

    }

    private static class RequestProperties {
        private String url;
        private final Map<String, String> headers = new HashMap<>();
        private final Map<String, String> params = new HashMap<>();
        private final Map<String, String> cookies = new HashMap<>();
        // 默认使用UTF-8编码
        private Charset charset = DEFAULT_CHARSET;
        // 使用jsonbody方式
        private boolean jsonBody = false;
        // 读取数据超时，默认3秒
        private Integer socketTimeout = 3000;
        // 建立连接超时，默认2秒
        private Integer connTimeout = 2000;
        // 从连接池获取连接超时，默认500，不要太大，避免大量请求堆积在获取链接处，尽快抛出异常发现问题。
        private Integer connReqTimeout = 500;
        // 默认开启gzip压缩
        private boolean withGzip = true;
        // 默认开启keep-alive及连接池
        private boolean pooled = true;

        private HttpGet createGet() {
            String requestUrl = this.url;
            if (!this.params.isEmpty()) {
                requestUrl += "?" + URLEncodedUtils.format(createNameValuePairs(this.params), this.charset);
            }
            HttpGet request = new HttpGet(requestUrl);
            fillProperties(request);
            return request;
        }

        private HttpPost createPost() throws IOException {
            HttpPost request = new HttpPost(this.url);
            if (!this.params.isEmpty()) {
                request.setEntity(this.jsonBody
                        ? createJsonEntity(this.params, this.charset)
                        : createSimpleEntity(this.params, this.charset));
            }
            fillProperties(request);
            return request;
        }

        private void fillProperties(final HttpRequestBase request) {
            if (this.jsonBody) {
                request.addHeader("Content-type", "application/json");
            }
            if (this.withGzip) {
                request.addHeader("Accept-Encoding", "gzip");
            }
            if (this.pooled) {
                // 开启keep-alive
                request.addHeader(HTTP.CONN_DIRECTIVE, HTTP.CONN_KEEP_ALIVE);
            } else {
                // 关闭keep-alive
                request.addHeader(HTTP.CONN_DIRECTIVE, HTTP.CONN_CLOSE);
            }
            if (!this.headers.isEmpty()) {
                for (Entry<String, String> header : this.headers.entrySet()) {
                    request.addHeader(header.getKey(), header.getValue());
                }
            }
            if (!this.cookies.isEmpty()) {
                for (Entry<String, String> header : this.cookies.entrySet()) {
                    request.addHeader("Cookie", header.getKey() + "=" + header.getValue());
                }
            }
            // 设置超时
            RequestConfig.Builder timeoutBuilder = RequestConfig.custom();
            // socket读数据超时时间：从服务器获取响应数据的超时时间
            if (this.socketTimeout != null) {
                timeoutBuilder.setSocketTimeout(this.socketTimeout);
            }
            // 与服务器连接超时时间：httpClient会创建一个异步线程用以创建socket连接，此处设置该socket的连接超时时间
            if (this.connTimeout != null) {
                timeoutBuilder.setConnectTimeout(this.connTimeout);
            }
            //从连接池中获取连接的超时时间
            if (this.connReqTimeout != null) {
                timeoutBuilder.setConnectionRequestTimeout(this.connReqTimeout);
            }
            request.setConfig(timeoutBuilder.build());
        }
    }

}