package com.xbctechnologies.core.apis;

import com.xbctechnologies.core.apis.dto.ApiEnum;
import com.xbctechnologies.core.apis.dto.TxRequest;
import com.xbctechnologies.core.apis.dto.res.tx.TxSendResponse;
import com.xbctechnologies.core.TestParent;
import com.xbctechnologies.core.apis.dto.xtypes.TxCommonBody;
import com.xbctechnologies.core.component.rest.RestHttpClient;
import com.xbctechnologies.core.component.rest.RestHttpConfig;
import org.junit.Before;
import org.junit.Test;

import java.math.BigInteger;
import java.util.concurrent.Executors;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

/**
 * 멀티 쓰레드를 이용하여 XNode에 트랜잭션을 발생시킨다.
 */
public class TestXCubeThreadTx extends TestParent {
    private XCube xCube;
    private BigInteger fee = new BigInteger("1000000000000000000000");
    private BigInteger amount = new BigInteger("1000000000000000000000");

    private String testFile = "/testFile";
    private String testPrivKey = "/testPrivKey.json";

    private int threadCnt = 1000;

    @Before
    public void init() {
        String etherHost = "106.251.231.226:6710";
        String localhost = "localhost:7979";
        xCube = new XCube(new RestHttpClient(
                RestHttpConfig.builder()
                        .withXNodeUrl(String.format("http://%s", localhost))
                        .withMaxConnection(100)
                        .withDefaultTimeout(30000)
                        .build()
        ));
    }

    private TxRequest.Builder makeDefaultBuilder() {
        return TxRequest.builder()
                .withIsSync(true)
                .withTargetChainId(targetChainId)
                .withSender(sender)
                .withReceiver(receiver)
                .withFee(fee)
                .withAmount(amount);
    }

    @Test
    public void TestTxCommonRequest() throws Exception {
        //https://developers.google.com/protocol-buffers/docs/javatutorial
        TxRequest txRequest = makeDefaultBuilder()
                .withPayloadType(ApiEnum.PayloadType.CommonType)
                .withPayloadBody(new TxCommonBody("test"))
                .build();

        for (int i = 0; i < threadCnt; i++) {
            Executors.newSingleThreadExecutor().execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        TxSendResponse txSendResponse = xCube.sendTransaction(txRequest).send();
                        assertNull(txSendResponse.getError());
                        assertNotNull(txSendResponse.getResult());
                    } catch (Exception e) {
                        e.getStackTrace();
                    }
                }
            });

        }
        Thread.sleep(1000 * 50);
    }
}
