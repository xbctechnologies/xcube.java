package com.xbctechnologies.core.component.rest;

import com.xbctechnologies.core.component.rest.dto.RestReqDto;
import com.xbctechnologies.core.component.rest.dto.RestResDto;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.Consts;
import org.apache.http.Header;
import org.apache.http.HttpHost;
import org.apache.http.NameValuePair;
import org.apache.http.client.CookieStore;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.*;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.cookie.Cookie;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.*;
import org.apache.http.impl.conn.DefaultProxyRoutePlanner;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.ssl.SSLContextBuilder;
import org.apache.http.util.EntityUtils;

import javax.net.ssl.SSLContext;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Created by alex on 2017. 1. 18..
 */
public class RestHttpClient {
    private RestHttpConfig restHttpConfig;
    private PoolingHttpClientConnectionManager cm;
    private HttpHost proxy;

    public RestHttpClient(RestHttpConfig restHttpConfig) {
        this.restHttpConfig = restHttpConfig;

        cm = new PoolingHttpClientConnectionManager();
        cm.setMaxTotal(restHttpConfig.getMaxConnection());

        if (StringUtils.isEmpty(restHttpConfig.getProxyHost()) && restHttpConfig.getProxyPort() > 0) {
            proxy = new HttpHost(restHttpConfig.getProxyHost(), restHttpConfig.getProxyPort());
        }
    }

    public CloseableHttpClient getClientBuilder(RestReqDto restReqDto, CookieStore cookieStore) {
        if (restReqDto.getOptions() != null && restReqDto.getOptions().isUseSsl()) {
            try {
                SSLContext sslContext = new SSLContextBuilder()
                        .loadTrustMaterial(null, (certificate, authType) -> true).build();
                return HttpClients.custom().setSSLContext(sslContext).setSSLHostnameVerifier(new NoopHostnameVerifier()).build();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        } else {
            HttpClientBuilder hcBuilder;
            if (restReqDto.getOptions() != null && restReqDto.getOptions().isUseProxy()) {
                hcBuilder = HttpClients.custom();
                DefaultProxyRoutePlanner routePlanner = new DefaultProxyRoutePlanner(proxy);
                hcBuilder.setRoutePlanner(routePlanner);
            } else {
                hcBuilder = HttpClientBuilder.create();
            }

            if (cookieStore != null) {
                hcBuilder.setDefaultCookieStore(cookieStore);
            }
            if (restReqDto.getOptions() != null && restReqDto.getOptions().getCookies() != null) {
                cookieStore = cookieStore == null ? new BasicCookieStore() : cookieStore;
                for (Cookie cookie : restReqDto.getOptions().getCookies()) {
                    cookieStore.addCookie(cookie);
                }
                RequestConfig globalConfig = RequestConfig.custom().setCookieSpec(CookieSpecs.DEFAULT).build();
                hcBuilder.setDefaultRequestConfig(globalConfig);
            }

            return hcBuilder
                    .setRetryHandler(new DefaultHttpRequestRetryHandler(restReqDto.getOptions() == null ? 0 : restReqDto.getOptions().getRetryCount(), false))
                    .setConnectionManager(cm)
                    .setConnectionManagerShared(true)
                    .build();
        }
    }

    private RequestConfig makeTimeoutConfig(Integer timeout) {
        int tempTimeout = timeout == null ? restHttpConfig.getDefaultTimeout() : timeout;
        return RequestConfig.custom()
                .setConnectionRequestTimeout(tempTimeout)
                .setConnectTimeout(tempTimeout)
                .setSocketTimeout(tempTimeout).build();
    }

    private List<NameValuePair> convertParam(Map<String, String> params) {
        List<NameValuePair> paramList = new ArrayList<>();

        if (params != null) {
            Iterator<String> keys = params.keySet().iterator();
            while (keys.hasNext()) {
                String key = keys.next();
                paramList.add(new BasicNameValuePair(key, params.get(key)));
            }
        }

        return paramList;
    }

    private void validate(RestReqDto restReqDto) {
        if (restReqDto.isJsonBody()) {
            if (restReqDto.getType() != RestReqDto.Type.POST) {
                throw new RestHttpException("Jsonbody should be set only post");
            }
        } else {
            if (!(restReqDto.getRequestParams() instanceof Map)) {
                throw new RestHttpException("If it is not jsonbody, it should be set to map.");
            }
        }
    }

    public RestResDto request(RestReqDto restReqDto) throws RestHttpException {
        validate(restReqDto);
        CloseableHttpClient client = getClientBuilder(restReqDto, null);

        List<NameValuePair> paramList = null;
        if (!restReqDto.isJsonBody()) {
            paramList = convertParam((Map) restReqDto.getRequestParams());
        }

        HttpRequestBase httpRequest = null;
        Charset charset = Consts.UTF_8;
        if (restReqDto.getOptions() != null && restReqDto.getOptions().getCharset() != null) {
            charset = restReqDto.getOptions().getCharset();
        }

        String reqUrl = StringUtils.isEmpty(restReqDto.getRequestUrl()) ? restHttpConfig.getXnodeUrl() : restReqDto.getRequestUrl();
        switch (restReqDto.getType()) {
            case GET:
            case DELETE:
                String urlParams = paramList.size() > 0 ? reqUrl + "?" + URLEncodedUtils.format(paramList, charset) : reqUrl;
                httpRequest = restReqDto.getType() == RestReqDto.Type.GET ? new HttpGet(urlParams) : new HttpDelete(urlParams);
                break;
            case POST:
                httpRequest = new HttpPost(reqUrl);
                if (restReqDto.isJsonBody()) {
                    httpRequest.setHeader("Content-type", "application/json");
                    ((HttpPost) httpRequest).setEntity(new StringEntity(restReqDto.generateJsonBody(), charset));
                } else {
                    ((HttpPost) httpRequest).setEntity(new UrlEncodedFormEntity(paramList, charset));
                }
                break;
            case PUT:
                httpRequest = new HttpPut(reqUrl);
                ((HttpPut) httpRequest).setEntity(new UrlEncodedFormEntity(paramList, charset));
                break;
        }
        httpRequest.setConfig(makeTimeoutConfig(restReqDto.getTimeout()));

        //Header
        if (restReqDto.getOptions() != null && restReqDto.getOptions().getHeaders() != null) {
            httpRequest.setHeaders(restReqDto.getOptions().getHeaders().toArray(new Header[restReqDto.getOptions().getHeaders().size()]));
        }

        RestResDto restResDto = new RestResDto();
        try (CloseableHttpResponse response = client.execute(restReqDto.isJsonBody() ? (HttpPost) httpRequest : httpRequest)) {
            restResDto.setStatusCode(response.getStatusLine().getStatusCode());
            restResDto.setResponseBody(EntityUtils.toString(response.getEntity(), charset));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return restResDto;
    }
}