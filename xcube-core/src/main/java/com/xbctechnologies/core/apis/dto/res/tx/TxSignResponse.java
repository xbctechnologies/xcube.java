package com.xbctechnologies.core.apis.dto.res.tx;

import com.xbctechnologies.core.apis.dto.req.tx.TxRequest;
import com.xbctechnologies.core.apis.dto.res.Response;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigInteger;

@Data
@EqualsAndHashCode(callSuper = false)
public class TxSignResponse extends Response<TxSignResponse.Result> {
    @Data
    public static class Result {
        private int v;
        private BigInteger r;
        private BigInteger s;
    }

    public void setSign(TxRequest txRequest) {
        if (this.getError() != null) {
            throw new RuntimeException(this.getError().getMessage());
        }
        txRequest.setV(this.getResult().v);
        txRequest.setR(this.getResult().r);
        txRequest.setS(this.getResult().s);
    }
}
