package com.xbctechnologies.core.apis.dto.res.node;

import com.xbctechnologies.core.apis.dto.res.Response;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = false)
public class XChainInfoResponse extends Response<List<XChainInfoResponse.Result>> {
    public List<XChainInfoResponse.Result> getXChainInfo() {
        return getResult();
    }

    @Data
    public static class Result {
        private String xchainID;
        private boolean hasAsset;
        private boolean isAvailTx;
        private String coinName;
        private List<Result> childXChainInfo;
    }
}
