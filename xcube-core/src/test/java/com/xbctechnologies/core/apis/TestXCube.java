package com.xbctechnologies.core.apis;

import com.xbctechnologies.core.apis.dto.res.BoolResponse;
import com.xbctechnologies.core.apis.dto.res.account.AccountExportResponse;
import com.xbctechnologies.core.component.rest.RestHttpClient;

import java.util.Arrays;

public class TestXCube {
    private RestHttpClient restHttpClient;

    public TestXCube(RestHttpClient restHttpClient) {
        this.restHttpClient = restHttpClient;
    }

    /**
     * Account
     */
    public Request<?, AccountExportResponse> exportAccount(Long reqId, String accountAddr, String password) {
        return new Request<>(
                restHttpClient,
                reqId,
                "account_export",
                Arrays.asList(accountAddr, password),
                AccountExportResponse.class
        );
    }

    public Request<?, BoolResponse> addPeer(Long reqId, String targetChainId, String[] peers, boolean persistent) {
        return new Request<>(
                restHttpClient,
                reqId,
                "network_addPeer",
                Arrays.asList(targetChainId, peers, persistent),
                BoolResponse.class
        );
    }

    public Request<?, BoolResponse> removePeer(Long reqId, String targetChainId, String[] peers) {
        return new Request<>(
                restHttpClient,
                reqId,
                "network_removePeer",
                Arrays.asList(targetChainId, peers),
                BoolResponse.class
        );
    }
}
