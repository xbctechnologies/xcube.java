package com.xbctechnologies.core.component.rest;

import lombok.Data;

@Data
public class RestHttpConfig {
    private String proxyHost;
    private int proxyPort;

    private int maxConnection = 100;
    private int defaultTimeout = 10000;
    private String xnodeUrl;

    private RestHttpConfig(Builder builder) {
        setProxyHost(builder.proxyHost);
        setProxyPort(builder.proxyPort);
        setMaxConnection(builder.maxConnection);
        setDefaultTimeout(builder.defaultTimeout);
        setXnodeUrl(builder.xnodeUrl);
    }

    public static Builder builder() {
        return new Builder();
    }


    public static final class Builder {
        private String xnodeUrl;
        private int defaultTimeout;
        private int maxConnection;
        private int proxyPort;
        private String proxyHost;

        private Builder() {
        }

        public Builder withXNodeUrl(String val) {
            xnodeUrl = val;
            return this;
        }

        public Builder withDefaultTimeout(int val) {
            defaultTimeout = val;
            return this;
        }

        public Builder withMaxConnection(int val) {
            maxConnection = val;
            return this;
        }

        public Builder withProxyPort(int val) {
            proxyPort = val;
            return this;
        }

        public Builder withProxyHost(String val) {
            proxyHost = val;
            return this;
        }

        public RestHttpConfig build() {
            return new RestHttpConfig(this);
        }
    }
}