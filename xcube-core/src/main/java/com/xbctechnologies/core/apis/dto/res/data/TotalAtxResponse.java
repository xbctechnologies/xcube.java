package com.xbctechnologies.core.apis.dto.res.data;

import com.xbctechnologies.core.apis.dto.res.Response;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigInteger;

@Data
@EqualsAndHashCode(callSuper = false)
public class TotalAtxResponse extends Response<TotalAtxResponse.Result> {
    @Data
    public static class Result {
        private int totalAccount;
        private BigInteger totalBalance;
        private BigInteger availableBalance;
        private BigInteger stakingBalance;
        private BigInteger predictionRewardBalance;
        private BigInteger lockingBalance;
    }
}
