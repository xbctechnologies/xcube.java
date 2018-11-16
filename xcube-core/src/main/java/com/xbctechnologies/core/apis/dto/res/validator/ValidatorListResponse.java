package com.xbctechnologies.core.apis.dto.res.validator;

import com.xbctechnologies.core.apis.dto.ApiEnum;
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
public class ValidatorListResponse extends Response<List<ValidatorListResponse.Result>> {
    @Data
    public static class Result {
        private String validatorAccountAddr;
        private BigInteger totalBondingBalance;
        private BigInteger totalBondingBalanceOfValidator;
        private BigInteger rewardAmountFee;
        private List<Reward> rewardBlocks;
        private Map<String, Delegator> delegatorMap;

        private String companyName;
        private String companyDesc;
        private String companyUrl;
        private String companyLogoUrl;
        private String companyLat;
        private String companyLon;

        private boolean isFreezing;
        private ApiEnum.FreezingType freezingReason;
        private long freezingBlockNo;
        private long disconnectCnt;

        @Data
        @AllArgsConstructor
        @NoArgsConstructor
        public static class Reward {
            private long startBlockNo;
            private long endBlockNo;
            private BigInteger rewardPerCoin;
        }

        @Data
        @AllArgsConstructor
        @NoArgsConstructor
        public static class Delegator {
            private BigInteger totalBondingBalance;
            private List<Bonding> bondingList;
        }

        @Data
        @AllArgsConstructor
        @NoArgsConstructor
        public static class Bonding {
            private long blockNo;
            private BigInteger bondingBalance;
        }
    }
}
