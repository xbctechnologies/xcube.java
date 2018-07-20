package com.xbctechnologies.core.apis.dto.res;

import lombok.Data;

@Data
public class Response<T> {
    private String jsonrpc;
    private long id;
    private Error error;
    private T result;

    @Data
    public static class Error {
        private int code;
        private String message;
    }
}
