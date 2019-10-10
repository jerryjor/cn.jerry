package cn.jerry.net;

import cn.jerry.json.JsonUtil;
import cn.jerry.logging.LogManager;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.GzipDecompressingEntity;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class HttpRequester {
    private static Logger logger = LogManager.getLogger();

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
     * 执行get请求，参数必须在builder中设置完毕，请求时不支持重设参数
     *
     * @return 服务器返回结果
     * @throws IOException 请求异常
     */
    public StringHttpResponse doGet() throws IOException {
        return doRequest(this.properties.createGet());
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
        request.addHeader("Content-type", "application/json");
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
    public StringHttpResponse uploadFile(Map<String, String> params, String fileParamName, String filePath)
            throws IOException {
        HttpPost request = this.properties.createPost();
        HttpEntity httpEntity = createFileUploadEntity(params, this.properties.charset, fileParamName, filePath);
        request.setEntity(httpEntity);
        return doRequest(request);
    }

    /**
     * 执行请求
     *
     * @return response content
     * @throws IOException @see CloseableHttpClient.execute(HttpUriRequest)
     */
    private StringHttpResponse doRequest(HttpRequestBase request) throws IOException {
        long st = System.currentTimeMillis();

        StringHttpResponse strResponse = new StringHttpResponse();
        CloseableHttpClient httpClient = null;
        CloseableHttpResponse response = null;
        try {
            httpClient = HttpClientFactory.getHttpClient(this.proxyHost, this.proxyPort, this.properties.pooled);
            response = httpClient.execute(request);
            strResponse.setStatusCode(response.getStatusLine().getStatusCode());
            HttpEntity entity = withGzip(response)
                    ? new GzipDecompressingEntity(response.getEntity()) : response.getEntity();
            // EntityUtils.toString方法会获取InputStream并关闭
            strResponse.setEntity(EntityUtils.toString(entity, this.properties.charset));
            if (HttpStatus.SC_OK != strResponse.getStatusCode()) {
                logger.warn("doRequest, server status: {}, uri: [{}], entity : {}", strResponse.getStatusCode(),
                        request.getURI(), (isHtml(strResponse.getEntity()) ? "a html" : strResponse.getEntity()));
            }
        } catch (IOException e) {
            request.abort();
            logger.error("doRequest failed, uri:[" + request.getURI() + "]", e);
            throw e;
        } finally {
            // 如果使用了 EntityUtils.toString，那么可以省略response.close
//            if (response != null) {
//                try {
//                    response.close();
//                } catch (IOException ioe) {
//                    logger.error("doRequest, close response failed.", ioe);
//                }
//            }
            if (!this.properties.pooled && httpClient != null) {
                // 关闭内置连接池
                try {
                    httpClient.close();
                } catch (IOException ioe) {
                    logger.error("close http client failed.", ioe);
                }
            }
        }

        long cost = System.currentTimeMillis() - st;
        if (cost > 1000L) {
            logger.info("Requesting [" + request.getURI() + "] cost " + cost + " ms.");
        }
        return strResponse;
    }

    private static boolean withGzip(CloseableHttpResponse response) {
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

    private static boolean isHtml(String str) {
        return str != null && !(str = str.trim()).isEmpty() && str.charAt(0) == '<';
    }

    public static Builder newBuilder(String url) throws IOException {
        return new Builder(url);
    }

    private static List<NameValuePair> createNameValuePairs(Map<String, String> params) {
        List<NameValuePair> paramsPair = new ArrayList<NameValuePair>();
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
            String fileParamName, String filePath) {
        MultipartEntityBuilder builder = MultipartEntityBuilder.create().setCharset(charset);
        File file = new File(filePath);
        if (file.exists()) {
            builder.addBinaryBody(fileParamName, file);
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

        private Builder(String url) throws IOException {
            if (url == null || (url = url.trim()).isEmpty()) {
                throw new IOException("url cannot be empty.");
            }
            this.properties.url = url;
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
        private Map<String, String> headers = new HashMap<String, String>();
        private Map<String, String> params = new HashMap<String, String>();
        private Map<String, String> cookies = new HashMap<String, String>();
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
            String url = this.url;
            if (!this.params.isEmpty()) {
                url += "?" + URLEncodedUtils.format(createNameValuePairs(this.params), this.charset);
            }
            HttpGet request = new HttpGet(url);
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