package com.xbctechnologies.core.apis.dto.res.tx;

import com.xbctechnologies.core.apis.dto.res.Response;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
public class TxReceiptResponse extends Response<TxReceiptResponse.Result> {
    @Data
    @EqualsAndHashCode(callSuper = false)
    public static class Result extends TxResponse.Result {
        private int status;
    }
}
