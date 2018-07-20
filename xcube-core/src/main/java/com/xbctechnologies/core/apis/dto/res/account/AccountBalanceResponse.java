package com.xbctechnologies.core.apis.dto.res.account;

import com.xbctechnologies.core.apis.dto.res.Response;
import com.xbctechnologies.core.utils.CurrencyUtil;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigInteger;

@Data
@EqualsAndHashCode(callSuper = false)
public class AccountBalanceResponse extends Response<AccountBalanceResponse.Result> {
    public Result getBalance() {
        return getResult();
    }

    @Data
    public static class Result {
        private String address;
        private BigInteger totalBalance;
        private BigInteger availableBalance;
        private BigInteger stakingBalance;
        private BigInteger predictionRewardBalance;
        private BigInteger lockingBalance;
        private CurrencyUtil.CurrencyType currencyType;
    }
}
