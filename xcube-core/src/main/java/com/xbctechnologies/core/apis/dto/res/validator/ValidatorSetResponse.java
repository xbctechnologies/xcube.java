package com.xbctechnologies.core.apis.dto.res.validator;

import com.xbctechnologies.core.apis.dto.res.Response;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Set;

@Data
@EqualsAndHashCode(callSuper = false)
public class ValidatorSetResponse extends Response<ValidatorSetResponse.Result> {
    public Set<String> getValidatorSet() {
        return getResult().getValidatorSet();
    }

    @Data
    public static class Result {
        private Set<String> validatorSet;
    }
}
