package com.xbctechnologies.core.apis.dto.res.block;

import com.xbctechnologies.core.apis.dto.res.Response;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
public class BlockTxCntResponse extends Response<Long> {
    public Long getTxCnt() {
        return getResult();
    }
}
