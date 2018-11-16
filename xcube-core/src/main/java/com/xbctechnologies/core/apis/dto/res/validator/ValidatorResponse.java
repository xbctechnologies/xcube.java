package com.xbctechnologies.core.apis.dto.res.validator;

import com.xbctechnologies.core.apis.dto.res.Response;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
public class ValidatorResponse extends Response<ValidatorListResponse.Result> {
    public ValidatorListResponse.Result getValidator() {
        return getResult();
    }
}
