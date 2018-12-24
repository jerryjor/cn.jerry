package cn.jerry.net;

import cn.jerry.logging.LogManager;
import org.apache.commons.lang.StringUtils;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpStatus;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.GzipDecompressingEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.DefaultHttpRequestRetryHandler;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.util.Map;
import java.util.Map.Entry;

/**
 * 如果要启用gzip，请求参数中增加header即可：headers.put("Accept-Encoding", "gzip");
 * 如果要绑定Cookie，也在header中设置即可：headers.put("Cookie", "cookieName=cookieValue");
 *
 * @author zhaojiarui
 */
public class HttpClientUtilWithoutPool {
    private static Logger logger = LogManager.getLogger();

    private static final String DEFAULT_CHARSET = "UTF-8";

    /**
     * post请求
     *
     * @param url
     * @param params
     * @param charset
     * @param timeout
     * @throws IOException
     */
    public static String httpPost(String url, Map<String, String> params, String charset, Integer timeout)
            throws IOException {
        return httpPost(url, null, params, charset, timeout);
    }

    /**
     * post请求
     *
     * @param url
     * @param headers
     * @param params
     * @param charset
     * @param timeout
     * @throws IOException
     */
    public static String httpPost(String url, Map<String, String> headers, Map<String, String> params, String charset,
            Integer timeout) throws IOException {
        logger.info("httpPost, url:[" + url + "], charset:" + charset + ", timeout:" + timeout);
        if (url == null || url.trim().isEmpty()) return null;
        if (StringUtils.isBlank(charset)) charset = DEFAULT_CHARSET;

        HttpEntity httpEntity = null;
        try {
            // 创建httppost
            HttpPost httpPost = new HttpPost(url);
            if (headers != null && !headers.isEmpty()) {
                for (Entry<String, String> header : headers.entrySet()) {
                    if (header.getValue() != null) {
                        httpPost.addHeader(header.getKey(), header.getValue());
                    }
                }
            }
            MultipartEntityBuilder builder = MultipartEntityBuilder.create();
            if (params != null && !params.isEmpty()) {
                for (Entry<String, String> param : params.entrySet()) {
                    builder.addTextBody(param.getKey(), param.getValue());
                }
            }
            httpEntity = builder.build();
            httpPost.setEntity(httpEntity);
            // 设置超时
            if (timeout != null) {
                RequestConfig timeoutConfig = RequestConfig.custom()
                        .setSocketTimeout(timeout).setConnectTimeout(timeout)
                        .setConnectionRequestTimeout(timeout).build();
                httpPost.setConfig(timeoutConfig);
            }
            return doRequest(httpPost, charset);
        } catch (RuntimeException e) {
            logger.error("httpPost failed, url:[" + url + "], charset:" + charset + ", timeout:" + timeout, e);
            throw e;
        } finally {
            EntityUtils.consume(httpEntity);
        }

    }

    /**
     * post请求
     *
     * @param url
     * @param params
     * @param charset
     * @param timeout
     * @throws IOException
     */
    public static String httpGet(String url, Map<String, String> params, String charset, Integer timeout)
            throws IOException {
        return httpGet(url, null, params, charset, timeout);
    }

    /**
     * post请求
     *
     * @param url
     * @param headers
     * @param params
     * @param charset
     * @param timeout
     * @throws IOException
     */
    public static String httpGet(String url, Map<String, String> headers, Map<String, String> params, String charset,
            Integer timeout) throws IOException {
        logger.info("httpGet, url:[" + url + "], charset:" + charset + ", timeout:" + timeout);
        if (url == null || url.trim().isEmpty()) return null;
        if (StringUtils.isBlank(charset)) charset = DEFAULT_CHARSET;

        HttpGet httpGet = null;
        try {
            if (params != null && !params.isEmpty()) {
                StringBuilder paramBuffer = new StringBuilder();
                for (Entry<String, String> param : params.entrySet()) {
                    String value = null;
                    if (param.getValue() != null) {
                        value = URLEncoder.encode(param.getValue(), charset);
                    }
                    paramBuffer.append("&").append(param.getKey()).append("=").append(value);
                }
                url += paramBuffer.toString().replaceFirst("&", "?");
            }
            // 创建httppost
            httpGet = new HttpGet(url);
            if (headers != null && !headers.isEmpty()) {
                for (Entry<String, String> header : headers.entrySet()) {
                    if (header.getValue() != null) {
                        httpGet.addHeader(header.getKey(), header.getValue());
                    }
                }
            }
            // 设置超时
            if (timeout != null) {
                RequestConfig timeoutConfig = RequestConfig.custom()
                        .setSocketTimeout(timeout).setConnectTimeout(timeout)
                        .setConnectionRequestTimeout(timeout).build();
                httpGet.setConfig(timeoutConfig);
            }
        } catch (RuntimeException e) {
            logger.error("httpGet failed, url:[" + url + "], charset:" + charset + ", timeout:" + timeout, e);
            throw e;
        }

        return doRequest(httpGet, charset);
    }

    /**
     * 执行请求
     *
     * @param request
     * @param charset
     * @return
     * @throws IOException
     */
    private static String doRequest(HttpUriRequest request, String charset) throws IOException {
        if (request == null) return null;
        logger.info("doRequest start, uri:[" + request.getURI() + "]");
        long st = System.currentTimeMillis();

        CloseableHttpClient httpClient = null;
        String responseStr = null;
        try {
            // 创建的httpClient实例.
            httpClient = HttpClientBuilder.create()
                    .setRetryHandler(new DefaultHttpRequestRetryHandler(0, false))
                    .build();
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
        } catch (IOException e) {
            if (request != null) request.abort();
            logger.error("doRequest, uri:[" + request.getURI() + "]", e);
            throw e;
        } finally {
            if (httpClient != null) {
                // 关闭连接,释放资源
                try {
                    httpClient.close();
                } catch (IOException ioe) {
                    logger.error("close http client failed.", ioe);
                }
            }
        }

        long et = System.currentTimeMillis();
        logger.info("doRequest finished, uri:[" + request.getURI() + "], cost time:" + (et - st) + "ms.");
        return responseStr;
    }

    /**
     * 判断返回数据是否启用gzip压缩
     *
     * @param response
     * @return
     */
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
}