package com.xbctechnologies.core.apis.dto.res.validator;

import com.xbctechnologies.core.apis.dto.ApiEnum;
import com.xbctechnologies.core.apis.dto.res.Response;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigInteger;

@Data
@EqualsAndHashCode(callSuper = false)
public class SimpleValidatorResponse extends Response<SimpleValidatorResponse.Result> {
    public SimpleValidatorResponse.Result getSimpleValidator() {
        return getResult();
    }

    @Data
    public static class Result {
        private String validatorAccountAddr;
        private BigInteger totalBondingBalance;
        private BigInteger totalBondingBalanceOfValidator;

        private String companyName;
        private String companyDesc;
        private String companyUrl;
        private String companyLogoUrl;
        private String companyLat;
        private String companyLon;

        private long startBlockNo;
        private long endBlockNo;

        private boolean isFreezing;
        private ApiEnum.FreezingType freezingReason;
        private long freezingBlockNo;
    }
}
