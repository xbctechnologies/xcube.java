package com.xbctechnologies.core.apis.dto.res.validator;

import com.xbctechnologies.core.apis.dto.res.Response;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigInteger;
import java.util.Map;

@Data
@EqualsAndHashCode(callSuper = false)
public class ValidatorBondResponse extends Response<Map<String, ValidatorBondResponse.Bond>> {
    public Map<String, Bond> getBonding() {
        return getResult();
    }

    @Data
    public static class Bond {
        private BigInteger bondingBalance;
    }
}
