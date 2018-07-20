package com.xbctechnologies.core.apis.dto.res.account;

import com.xbctechnologies.core.apis.dto.res.Response;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.math.BigInteger;
import java.util.Map;

@Data
@EqualsAndHashCode(callSuper = false)
public class AccountBondInfoResponse extends Response<AccountBondInfoResponse.Result> {
    public Result getBonding() {
        return getResult();
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Result {
        private BigInteger bonded;
        private BigInteger delegated;
        private BigInteger delegating;
        private Map<String, BigInteger> delegatingInfo;
    }
}
