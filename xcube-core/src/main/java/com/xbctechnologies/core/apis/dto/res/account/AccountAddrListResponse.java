package com.xbctechnologies.core.apis.dto.res.account;

import com.xbctechnologies.core.apis.dto.res.Response;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = false)
public class AccountAddrListResponse extends Response<List<String>> {
    public List<String> getAccountList() {
        return getResult();
    }
}
