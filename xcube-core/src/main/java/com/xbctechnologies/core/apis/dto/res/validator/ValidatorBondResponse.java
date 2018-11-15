package com.xbctechnologies.core.apis.dto.res.validator;

import com.xbctechnologies.core.apis.dto.res.Response;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigInteger;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = false)
public class ValidatorBondResponse extends Response<List<ValidatorBondResponse.Bond>> {
    public List<ValidatorBondResponse.Bond> getBonding() {
        return getResult();
    }

    @Data
    public static class Bond {
        private String validatorAccountAddr;
        private BigInteger bondingBalance;
    }
}
