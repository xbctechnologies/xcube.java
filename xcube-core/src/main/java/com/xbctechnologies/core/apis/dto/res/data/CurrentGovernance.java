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
public class CurrentGovernance extends Response<CurrentGovernance.Result> {
    public CurrentGovernance.Result getGR() {
        return getResult();
    }

    @Data
    @ToString(callSuper = true)
    @EqualsAndHashCode(callSuper = false)
    public static class Result extends TxGRProposalBody {
        private long grVersion;
        private long proposalBlockNo;
        private long endOfVotingBlockNo;
        private long reflectionBlockNo;

        private Map<String, BigInteger> eligibleToVoteMap;
        private Map<String, Boolean> voteHistory;
    }
}
