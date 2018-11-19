package com.xbctechnologies.core.apis.dto.res.validator;

import com.xbctechnologies.core.apis.dto.res.Response;

import java.util.List;

public class SimpleValidatorsResponse extends Response<List<SimpleValidatorResponse.Result>> {
    public List<SimpleValidatorResponse.Result> getSimpleValidatorList() {
        return getResult();
    }
}
