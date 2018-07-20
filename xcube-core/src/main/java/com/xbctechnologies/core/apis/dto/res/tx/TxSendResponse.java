package com.xbctechnologies.core.apis.dto.res.tx;

import com.xbctechnologies.core.apis.dto.res.Response;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
public class TxSendResponse extends Response<TxSendResponse.Result> {
    @Data
    public static class Result {
        private String txHash;
        private long txSize;
        private String dataAccountAddr;
    }
}
