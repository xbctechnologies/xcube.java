package com.xbctechnologies.core.apis.dto.res.tx;

import com.xbctechnologies.core.apis.dto.res.Response;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
public class TxCheckOriginalResponse extends Response<TxCheckOriginalResponse.Result> {
    public TxCheckOriginalResponse.Result getOrigin() {
        return getResult();
    }

    @Data
    public static class Result {
        private String address;
        private String dataHash;
        private boolean result;
        private long confirmations;
    }
}
