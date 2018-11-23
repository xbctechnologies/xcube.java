package com.xbctechnologies.core.apis.dto.res.network;

import com.xbctechnologies.core.apis.dto.res.Response;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = false)
public class NetworkPeersResponse extends Response<List<NetworkPeersResponse.Result>> {
    public List<Result> getPeers() {
        return getResult();
    }

    @Data
    public static class Result {
        //private String pubKey;
        private String id;
        private String listenAddr;
        private String network;
    }
}
