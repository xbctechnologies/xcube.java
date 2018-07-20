package com.xbctechnologies.core.apis;

import com.xbctechnologies.core.apis.dto.ApiEnum;
import com.xbctechnologies.core.apis.dto.req.tx.TxRequest;
import com.xbctechnologies.core.apis.dto.res.block.BlockResponse;
import com.xbctechnologies.core.apis.dto.res.tx.TxReceiptResponse;
import com.xbctechnologies.core.apis.dto.res.tx.TxResponse;
import com.xbctechnologies.core.apis.dto.res.tx.TxSendResponse;
import com.xbctechnologies.core.component.rest.RestHttpClient;
import com.xbctechnologies.core.exception.TransactionException;
import com.xbctechnologies.core.apis.dto.xtypes.TxCommonBody;
import com.xbctechnologies.core.component.rest.RestHttpConfig;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.math.BigInteger;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

public class TestXCubeGetTxAPI {
    private XCube xCube;
    private String targetChainId = "1T";
    private String sender = "0x9ac601f1a9c8385cb1fd794d030898168b0b617a";
    private String receiver = "0x7826d36525a285072fd8fe7cbe1597013d8d9761";
    private BigInteger fee = new BigInteger("1000000000000000000000");
    private BigInteger amount = new BigInteger("1000000000000000000000");

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

    private TxRequest.Builder makeDefaultBuilder() {
        return TxRequest.builder()
                //.withIsSync(true)
                .withTargetChainId(targetChainId)
                .withSender(sender)
                .withReceiver(receiver)
                .withFee(fee)
                .withAmount(amount);
    }

    @Test
    public void TestGetTx() throws TransactionException {
        int blockNo = 15;
        BlockResponse blockResponse = xCube.getBlockByNumber(null, targetChainId, blockNo).send();
        assertNull(blockResponse.getError());
        assertNotNull(blockResponse.getBlock());

        System.out.println(blockResponse.getBlock().getBlockHash());
        for (int i = 0; i < blockResponse.getBlock().getTransactions().size(); i++) {
            System.out.println(blockResponse.getBlock().getTransactions().get(i));
            // block Tx만 조회 시도할 경우
            TxReceiptResponse txRes = xCube.getTransactionReceipt(null, targetChainId, blockResponse.getBlock().getTransactions().get(i)).send();
            assertNull(txRes.getError());
            assertNotNull(txRes.getResult());
            Assert.assertEquals(1, txRes.getResult().getStatus());
            if (txRes.getResult() != null) {
                System.out.println("getTransactionReceipt TxHash: " + txRes.getResult().getTxHash());
            }
        }

        TxRequest txRequest = makeDefaultBuilder()
                .withIsSync(false)
                .withPayloadType(ApiEnum.PayloadType.CommonType)
                .withPayloadBody(new TxCommonBody("test"))
                .build();

        TxSendResponse txSendResponse = xCube.sendTransaction(txRequest).send();
        assertNull(txSendResponse.getError());
        assertNotNull(txSendResponse.getResult());

        // mempool Tx도 조회 시도할 경우
        TxResponse txRes = xCube.getTransaction(null, targetChainId, txSendResponse.getResult().getTxHash()).send();
        if (txRes.getResult() != null) {
            System.out.println("getTransaction TxHash: " + txRes.getResult().getTxHash());
        }
    }
}
