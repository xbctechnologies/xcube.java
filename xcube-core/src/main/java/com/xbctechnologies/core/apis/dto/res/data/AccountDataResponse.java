package com.xbctechnologies.core.apis.dto.res.data;

import com.xbctechnologies.core.apis.dto.res.Response;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.math.BigInteger;
import java.util.List;
import java.util.Map;

@Data
@EqualsAndHashCode(callSuper = false)
public class AccountDataResponse extends Response<AccountDataResponse.Result> {
    public AccountDataResponse.Result getAccount() {
        return getResult();
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Result {
        private long txNo;
        private BigInteger balance;
        private BigInteger bondedBalance;
        private Map<String, Bond> bondingMap;
        private List<UnBond> unBondingList;

        @Data
        @AllArgsConstructor
        @NoArgsConstructor
        public static class Bond {
            private BigInteger bondingBalance;
        }

        @Data
        @AllArgsConstructor
        @NoArgsConstructor
        public static class UnBond {
            private String validatorAccountAddr;
            private BigInteger unBondingBalance;
            private BigInteger rewardBalance;
            private long breakBlockNo;
        }
    }
}
