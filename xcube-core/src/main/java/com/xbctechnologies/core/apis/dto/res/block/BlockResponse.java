package com.xbctechnologies.core.apis.dto.res.block;

import com.xbctechnologies.core.apis.dto.res.Response;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = false)
public class BlockResponse extends Response<BlockResponse.Result> {
    public Result getBlock() {
        return getResult();
    }

    @Data
    public static class Result {
        private String chainID;
        private long blockNo;
        private String blockHash;
        private long blockSize;
        private String transactionRoot;
        private List<String> transactions;
        private long numTxs;
        private long total_txs;
        private long time;
    }
}
