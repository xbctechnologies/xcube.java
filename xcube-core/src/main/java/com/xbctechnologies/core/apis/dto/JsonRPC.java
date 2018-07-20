package com.xbctechnologies.core.apis.dto;

import com.xbctechnologies.core.params.DefaultParams;
import lombok.Data;

import java.util.List;

@Data
public class JsonRPC<T> {
    private String jsonrpc = DefaultParams.JSON_RPC_VERSION;
    private String method;
    private long id;
    private List<T> params;

    private JsonRPC(Builder builder) {
        setJsonrpc(builder.jsonrpc);
        setMethod(builder.method);
        setId(builder.id);
        setParams(builder.params);
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder<T> {
        private List<T> params;
        private String method;
        private long id;
        private String jsonrpc;

        private Builder() {
        }

        public Builder withParams(List<T> val) {
            params = val;
            return this;
        }

        public Builder withMethod(String val) {
            method = val;
            return this;
        }

        public Builder withID(long val) {
            id = val;
            return this;
        }

        public Builder withJsonrpc(String val) {
            jsonrpc = val;
            return this;
        }

        public JsonRPC build() {
            return new JsonRPC(this);
        }
    }
}
