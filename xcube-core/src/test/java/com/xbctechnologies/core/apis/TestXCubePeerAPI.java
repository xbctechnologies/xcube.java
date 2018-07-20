package com.xbctechnologies.core.apis;

import com.xbctechnologies.core.apis.dto.res.BoolResponse;
import com.xbctechnologies.core.apis.dto.res.LongResponse;
import com.xbctechnologies.core.apis.dto.res.network.NetworkPeersResponse;
import com.xbctechnologies.core.component.rest.RestHttpClient;
import com.xbctechnologies.core.component.rest.RestHttpConfig;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

public class TestXCubePeerAPI {
    private XCube xCube;
    private TestXCube testXCube;
    private String targetChainId = "1T";

    @Before
    public void init() {
        String etherHost = "106.251.231.226:6710";
        String localhost = "localhost:7979";
        xCube = new XCube(new RestHttpClient(
                RestHttpConfig.builder()
                        .withXNodeUrl(String.format("http://%s", localhost))
                        .withMaxConnection(100)
                        .withDefaultTimeout(60000)
                        .build()
        ));

        testXCube = new TestXCube(new RestHttpClient(
                RestHttpConfig.builder()
                        .withXNodeUrl(String.format("http://%s", localhost))
                        .withMaxConnection(100)
                        .withDefaultTimeout(60000)
                        .build()
        ));
    }

    @Test
    public void TestGetPeer() {
        // 연결된 peer 개수 요청
        LongResponse pRes = xCube.getPeerCnt(null, targetChainId).send();
        assertNull(pRes.getError());
        System.out.println("peer count: " + pRes.getResult());

        // 연결된 peer 정보 요청
        NetworkPeersResponse npRes = xCube.getPeers(null, targetChainId).send();
        assertNull(npRes.getError());
        for (int i = 0; i < npRes.getPeers().size(); i++) {
            System.out.println("node ID: " + npRes.getPeers().get(i).getId());
            System.out.println("node listen address: " + npRes.getPeers().get(i).getListenAddr());
            System.out.println("noed network: " + npRes.getPeers().get(i).getNetwork());
        }
    }

    @Test
    public void TestAddPeer() {
        String[] peers = {"f83096de91580901c67b5fe3829e5080d3ba6ca5@1.1.1.1:8090", "05ec88ab833cc797a50b607ef8629d3af9ac2f41@2.2.2.2:8091"};
        boolean persistent = false;
        BoolResponse boolResponse = testXCube.addPeer(null, targetChainId, peers, persistent).send();
        assertNull(boolResponse.getError());
        assertNotNull(boolResponse.getResult());
    }

    @Test
    public void TestRemovePeer() {
        String[] peers = {"f83096de91580901c67b5fe3829e5080d3ba6ca5@1.1.1.1:8090", "05ec88ab833cc797a50b607ef8629d3af9ac2f41@2.2.2.2:8091"};
        BoolResponse boolResponse = testXCube.removePeer(null, targetChainId, peers).send();
        assertNull(boolResponse.getError());
        assertNotNull(boolResponse.getResult());
    }
}
