package com.xbctechnologies.core.apis.dto.res.account;

import com.xbctechnologies.core.apis.dto.res.Response;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
public class AccountNewResponse extends Response<String> {
    public String getAddress() {
        return getResult();
    }
}
