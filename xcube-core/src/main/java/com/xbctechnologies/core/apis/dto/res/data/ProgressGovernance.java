package com.xbctechnologies.core.apis.dto.res.data;

import com.xbctechnologies.core.apis.dto.xtypes.TxGRProposalBody;
import com.xbctechnologies.core.apis.dto.res.Response;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.math.BigInteger;
import java.util.Map;

@Data
@EqualsAndHashCode(callSuper = false)
public class ProgressGovernance extends Response<ProgressGovernance.Result> {
    public ProgressGovernance.Result getGR() {
        return getResult();
    }

    @Data
    @ToString(callSuper = true)
    @EqualsAndHashCode(callSuper = false)
    public static class Result extends TxGRProposalBody {
        private long expectedGRVersion;

        private Map<String, BigInteger> stake;
        private Map<String, Boolean> votingResult;

        private long agreeRate;
        private long disagreeRate;
        private boolean isPass;

        private long currentBlockNo;
        private long endOfVotingBlockNo;
        private long reflectionBlockNo;
    }
}
