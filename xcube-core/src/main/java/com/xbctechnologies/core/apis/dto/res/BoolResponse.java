package com.xbctechnologies.core.apis.dto.res;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
public class BoolResponse extends Response<Boolean> {
    public boolean isValidator() {
        return getResult();
    }
}
