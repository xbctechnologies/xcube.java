package com.xbctechnologies.core.apis.dto.xtypes;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.apache.commons.lang3.StringUtils;

import java.math.BigInteger;

public abstract class TxParentBody implements TxBody {
    @JsonIgnore
    final String METHOD = "tx_sendTransaction";

    boolean isEmpty(CharSequence cs) {
        return StringUtils.isEmpty(cs);
    }

    boolean checkBigInt(BigInteger val) {
        if (val == null || "0".equals(val.toString())) {
            return false;
        }

        return true;
    }
}
