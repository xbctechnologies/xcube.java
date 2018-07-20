package com.xbctechnologies.core.apis;

import com.xbctechnologies.core.apis.dto.res.block.BlockResponse;
import com.xbctechnologies.core.apis.dto.res.block.BlockTxCntResponse;
import com.xbctechnologies.core.component.rest.RestHttpClient;
import com.xbctechnologies.core.component.rest.RestHttpConfig;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.junit.Assert.*;

public class TestXCubeBlockAPI {
    private XCube xCube;
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
    }

    @Test
    public void TestGetBlockNumber() {
        int blockNo = 1;
        BlockResponse blockResponse = xCube.getBlockByNumber(null, targetChainId, blockNo).send();
        assertNull(blockResponse.getError());
        assertNotNull(blockResponse.getBlock());
        System.out.println(blockResponse.getBlock().getBlockHash());

        blockNo = 3244444;
        blockResponse = xCube.getBlockByNumber(null, targetChainId, blockNo).send();
        if (blockResponse.getError() != null && blockResponse.getError().getCode() == 342) {
            return;
        }
        assertNull(blockResponse.getError());
        assertNotNull(blockResponse.getBlock());
    }

    @Test
    public void TestGetBlockHash() {
        int blockNo = 1;
        BlockResponse blockResponse = xCube.getBlockByNumber(null, targetChainId, blockNo).send();
        assertNull(blockResponse.getError());
        assertNotNull(blockResponse.getBlock());
        String firstBlockHash = blockResponse.getBlock().getBlockHash();
        System.out.println(firstBlockHash);

        blockResponse = xCube.getBlock(null, targetChainId, firstBlockHash).send();
        assertNull(blockResponse.getError());
        assertNotNull(blockResponse.getBlock());
        assertEquals(firstBlockHash, blockResponse.getBlock().getBlockHash());
        System.out.println(blockResponse.getBlock());
    }

    @Test
    public void TestGetBlockTxCount() {
        int blockNo = 1;
        BlockResponse blockResponse = xCube.getBlockByNumber(null, targetChainId, blockNo).send();
        assertNull(blockResponse.getError());
        assertNotNull(blockResponse.getBlock());

        BlockTxCntResponse blockTxCntResponse = xCube.getBlockTxCount(null, targetChainId, blockNo).send();
        System.out.println("BlockNo getTxCnt: " + blockTxCntResponse.getTxCnt());
        assertNull(blockTxCntResponse.getError());
        assertThat(blockTxCntResponse.getTxCnt(), greaterThanOrEqualTo(0L));

        blockTxCntResponse = xCube.getBlockTxCount(null, targetChainId, blockResponse.getBlock().getBlockHash()).send();
        System.out.println("BlockHash getTxCnt: " + blockTxCntResponse.getTxCnt());
        assertNull(blockTxCntResponse.getError());
        assertThat(blockTxCntResponse.getTxCnt(), greaterThanOrEqualTo(0L));
    }
}
