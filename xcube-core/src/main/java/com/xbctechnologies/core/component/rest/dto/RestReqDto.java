package com.xbctechnologies.core.component.rest.dto;

import com.xbctechnologies.core.utils.JsonUtil;
import lombok.Data;
import org.apache.http.Header;
import org.apache.http.cookie.Cookie;

import java.nio.charset.Charset;
import java.util.List;

/**
 * Created by alex on 2017. 1. 18..
 */
@Data
public class RestReqDto {
    private Type type = Type.GET;

    private String requestUrl;
    //Map or Class
    private Object requestParams;

    private Integer timeout;
    private boolean isJsonBody;
    private Options options;

    private RestReqDto(Builder builder) {
        setType(builder.type);
        setRequestUrl(builder.requestUrl);
        setRequestParams(builder.requestParams);
        setTimeout(builder.timeout);
        setJsonBody(builder.isJsonBody);
        setOptions(builder.options);
    }

    public static Builder builder() {
        return new Builder();
    }

    public String generateJsonBody() {
        if (requestParams != null) {
            return JsonUtil.generateClassToJson(requestParams);
        }
        return null;
    }

    public enum Type {
        GET, POST, DELETE, PUT
    }

    @Data
    public static class Options {
        //header ex) new BasicHeader(name, value)
        private List<Header> headers;
        //cookie ex) new BasicClientCookie2(name, value)
        private List<Cookie> cookies;
        //Charset.forName("UTF-8")
        private Charset charset;

        private boolean useProxy = false;
        private boolean useSsl = false;

        private int retryCount;
    }

    public static final class Builder {
        private Options options;
        private boolean isJsonBody;
        private Integer timeout;
        private Object requestParams;
        private String requestUrl;
        private Type type;

        private Builder() {
        }

        public Builder withOptions(Options val) {
            options = val;
            return this;
        }

        public Builder withIsJsonBody(boolean val) {
            isJsonBody = val;
            return this;
        }

        public Builder withTimeout(Integer val) {
            timeout = val;
            return this;
        }

        public Builder withRequestParams(Object val) {
            requestParams = val;
            return this;
        }

        public Builder withRequestUrl(String val) {
            requestUrl = val;
            return this;
        }

        public Builder withType(Type val) {
            type = val;
            return this;
        }

        public RestReqDto build() {
            return new RestReqDto(this);
        }
    }
}
