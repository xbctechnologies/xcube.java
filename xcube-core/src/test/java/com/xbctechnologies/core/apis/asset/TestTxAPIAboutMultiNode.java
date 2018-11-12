package com.xbctechnologies.core.apis.asset;

import com.xbctechnologies.core.TestParent;
import com.xbctechnologies.core.apis.XCube;
import com.xbctechnologies.core.apis.dto.ApiEnum;
import com.xbctechnologies.core.apis.dto.req.tx.TxRequest;
import com.xbctechnologies.core.apis.dto.res.account.AccountBalanceResponse;
import com.xbctechnologies.core.apis.dto.res.account.AccountBondInfoResponse;
import com.xbctechnologies.core.apis.dto.res.data.DataAccountResponse;
import com.xbctechnologies.core.apis.dto.res.data.ValidatorListResponse;
import com.xbctechnologies.core.apis.dto.res.tx.TxCheckOriginalResponse;
import com.xbctechnologies.core.apis.dto.res.tx.TxSendResponse;
import com.xbctechnologies.core.apis.dto.xtypes.TxBondingBody;
import com.xbctechnologies.core.apis.dto.xtypes.TxCommonBody;
import com.xbctechnologies.core.apis.dto.xtypes.TxFileBody;
import com.xbctechnologies.core.apis.dto.xtypes.TxUnbondingBody;
import com.xbctechnologies.core.component.rest.RestHttpClient;
import com.xbctechnologies.core.component.rest.RestHttpConfig;
import com.xbctechnologies.core.order.Order;
import com.xbctechnologies.core.order.OrderedRunner;
import com.xbctechnologies.core.utils.CurrencyUtil;
import com.xbctechnologies.core.utils.DateUtil;
import com.xbctechnologies.core.utils.JsonUtil;
import com.xbctechnologies.core.utils.SignUtil;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import static com.xbctechnologies.core.utils.CurrencyUtil.CurrencyType.CoinType;
import static com.xbctechnologies.core.utils.CurrencyUtil.CurrencyType.XTOType;
import static org.junit.Assert.*;

/**
 * 사내 테스트네트워크에 총 10가지 Transaction에 대해 XNode와의 통합 테스트를 진행한다.
 * 최종 테스트 완료 후 각 노드의 Validator, Account, Block 정보 등이 모두 동잃한지 검증한다.
 */
@RunWith(OrderedRunner.class)
public class TestTxAPIAboutMultiNode extends TestParent {
    private static long startCurrentBlockNo = 26;
    private static long startEndOfVotingBlockNo = 28;
    private static long startReflectionBlockNo = 29;

    private TxRequest.Builder makeDefaultBuilder() {
        return TxRequest.builder()
                .withIsSync(true)
                .withTargetChainId(targetChainId)
                .withNotCheckValidation(true);
    }

    @Before
    public void init() {
        String etherHost = "106.251.231.226:7120";
        xCube = new XCube(new RestHttpClient(
                RestHttpConfig.builder()
                        .withXNodeUrl(String.format("http://%s", etherHost))
                        .withMaxConnection(100)
                        .withDefaultTimeout(60000)
                        .build()
        ));
    }

    /**
     * 공통 - 유효성 체크
     *
     * @throws Exception
     */
    @Test
    @Order(order = 1)
    public void CheckValidationCommonFields() throws Exception {
        //TargetChainID 설정하지 않음
        TxRequest txRequest = makeDefaultBuilder()
                .withTargetChainId(null)
                .withSender(sender)
                .withReceiver(receiver)
                .withPayloadType(ApiEnum.PayloadType.CommonType)
                .withFee(CurrencyUtil.generateXTO(CoinType, 1))
                .withAmount(CurrencyUtil.generateXTO(CoinType, 10))
                .withPayloadBody(new TxCommonBody("test"))
                .build();
        TxSendResponse txSendResponse = xCube.sendTransaction(txRequest).send();
        assertNotNull(txSendResponse.getError());
        Assert.assertEquals(508, txSendResponse.getError().getCode());

        //TargetChainID 와 CLI에 설정되 ChainID가 다른경우.
        txRequest = makeDefaultBuilder()
                .withTargetChainId("100T")
                .withSender(sender)
                .withReceiver(receiver)
                .withPayloadType(ApiEnum.PayloadType.CommonType)
                .withFee(CurrencyUtil.generateXTO(CoinType, 1))
                .withAmount(CurrencyUtil.generateXTO(CoinType, 10))
                .withPayloadBody(new TxCommonBody("test"))
                .build();
        txSendResponse = xCube.sendTransaction(txRequest).send();
        assertNotNull(txSendResponse.getError());
        Assert.assertEquals(508, txSendResponse.getError().getCode());

        //Fee가 부족한 경우.
        txRequest = makeDefaultBuilder()
                .withSender(sender)
                .withReceiver(receiver)
                .withPayloadType(ApiEnum.PayloadType.CommonType)
                .withFee(CurrencyUtil.generateXTO(CoinType, 100000000))
                .withAmount(CurrencyUtil.generateXTO(CoinType, 10))
                .withPayloadBody(new TxCommonBody())
                .build();
        txSendResponse = xCube.sendTransaction(txRequest).send();
        assertNotNull(txSendResponse.getError());
        Assert.assertEquals(201, txSendResponse.getError().getCode());

        //Sender 설정하지 않은 경우.
        txRequest = makeDefaultBuilder()
                .withSender(null)
                .withReceiver(receiver)
                .withPayloadType(ApiEnum.PayloadType.CommonType)
                .withFee(CurrencyUtil.generateXTO(CoinType, 1))
                .withAmount(CurrencyUtil.generateXTO(CoinType, 10))
                .withPayloadBody(new TxCommonBody("test"))
                .build();
        txSendResponse = xCube.sendTransaction(txRequest).send();
        assertNotNull(txSendResponse.getError());
        Assert.assertEquals(302, txSendResponse.getError().getCode());

        //Receiver 설정하지 않은 경우.
        txRequest = makeDefaultBuilder()
                .withSender(sender)
                .withPayloadType(ApiEnum.PayloadType.CommonType)
                .withFee(CurrencyUtil.generateXTO(CoinType, 1))
                .withAmount(CurrencyUtil.generateXTO(CoinType, 10))
                .withPayloadBody(new TxCommonBody("test"))
                .build();
        txSendResponse = xCube.sendTransaction(txRequest).send();
        assertNotNull(txSendResponse.getError());
        Assert.assertEquals(303, txSendResponse.getError().getCode());

        //정의되지 않은 PayloadType 설정
        txRequest = makeDefaultBuilder()
                .withSender(sender)
                .withReceiver(receiver)
                .withFee(CurrencyUtil.generateXTO(CoinType, 1))
                .withAmount(CurrencyUtil.generateXTO(CoinType, 10))
                .withPayloadBody(new TxCommonBody("test"))
                .build();
        txSendResponse = xCube.sendTransaction(txRequest).send();
        assertNotNull(txSendResponse.getError());
        Assert.assertEquals(335, txSendResponse.getError().getCode());

        //클라이언트에서 서명한 Account와 Sender가 다른 경우.
        txRequest = makeDefaultBuilder()
                .withSender(receiver)
                .withReceiver(receiver)
                .withFee(CurrencyUtil.generateXTO(CoinType, 1))
                .withAmount(CurrencyUtil.generateXTO(CoinType, 10))
                .withPayloadType(ApiEnum.PayloadType.CommonType)
                .withPayloadBody(new TxCommonBody("test"))
                .build();
        SignUtil.signTx(txRequest, privKeyPassword, privKeyJson);

        txSendResponse = xCube.sendTransaction(txRequest).send();
        assertNotNull(txSendResponse.getError());
        Assert.assertEquals(323, txSendResponse.getError().getCode());


        //정의되지 않은 PayloadType 설정
        /*txRequest = makeDefaultBuilder()
                .withSender(sender)
                .withReceiver(receiver)
                .withPayloadType(CommonType)
                .withFee(CurrencyUtil.generateXTO(CurrencyUtil.CurrencyType.CoinType, 1))
                .withAmount(CurrencyUtil.generateXTO(CurrencyUtil.CurrencyType.CoinType, 10))
                .withPayloadBody(new TxBondingBody(new BigInteger("100")))
                .build();
        txSendResponse = xCube.sendTransaction(txRequest).send();
        assertNull(txSendResponse.getError());*/
    }

    /**
     * CommonTx - 유효성 체크
     *
     * @throws Exception
     */
    @Test
    @Order(order = 2)
    public void CommonTxCheckValidation() throws Exception {
        //Amount를 음수로 설정
        TxRequest txRequest = makeDefaultBuilder()
                .withSender(sender)
                .withReceiver(receiver)
                .withPayloadType(ApiEnum.PayloadType.CommonType)
                .withAmount(new BigInteger("-1"))
                .withFee(CurrencyUtil.generateXTO(CoinType, 1))
                .withPayloadBody(new TxCommonBody("test"))
                .build();
        TxSendResponse txSendResponse = xCube.sendTransaction(txRequest).send();
        assertNotNull(txSendResponse.getError());
        Assert.assertEquals(306, txSendResponse.getError().getCode());

        //Input 데이터를 1KB 이상으로 설정 후 Fee를 1로 설정
        //1KB (3bit = Identifier)
        String data = "";
        for (int i = 0; i < 1022; i++) {
            data += "a";
        }
        txRequest = makeDefaultBuilder()
                .withSender(sender)
                .withReceiver(receiver)
                .withPayloadType(ApiEnum.PayloadType.CommonType)
                .withFee(CurrencyUtil.generateXTO(CoinType, 1))
                .withAmount(CurrencyUtil.generateXTO(CoinType, 10))
                .withPayloadBody(new TxCommonBody(data))
                .build();
        txSendResponse = xCube.sendTransaction(txRequest).send();
        assertNotNull(txSendResponse.getError());
        Assert.assertEquals(1, txSendResponse.getError().getCode());
    }

    /**
     * CommonTx - Sender가 Receiver에게 Amount 만큼의 코인을 전송 (txCnt : 1, totalTxCnt : 1, fee : 1, total fee : 1)
     *
     * @throws Exception
     */
    @Test
    @Order(order = 3)
    public void CommonTx() throws Exception {
        //1KB (3bit = Identifier)
        String data = "";
        for (int i = 0; i < 1021; i++) {
            data += "a";
        }

        TxRequest txRequest = makeDefaultBuilder()
                .withSender(sender)
                .withReceiver(receiver)
                .withPayloadType(ApiEnum.PayloadType.CommonType)
                .withFee(CurrencyUtil.generateXTO(CoinType, 1))
                .withAmount(CurrencyUtil.generateXTO(CoinType, 10))
                .withPayloadBody(new TxCommonBody(data))
                .build();

        TxSendResponse txSendResponse = xCube.sendTransaction(txRequest).send();
        assertNull(txSendResponse.getError());
        assertNotNull(txSendResponse.getResult());

        AccountBalanceResponse expectedSender = makeAccountBalance(sender, "6,999,989,000,000,000,000,000,000", "6,999,989,000,000,000,000,000,000", "0", "0", "0", XTOType);
        AccountBalanceResponse actualSender = xCube.getBalance(null, targetChainId, sender, XTOType).send();
        assertEquals(expectedSender.getBalance(), actualSender.getBalance());


        AccountBalanceResponse expectedReceiver = makeAccountBalance(receiver, "4,000,010,000,000,000,000,000,000", "3,999,910,000,000,000,000,000,000", "100,000,000,000,000,000,000", "0", "0", XTOType);

        AccountBalanceResponse actualReceiver = xCube.getBalance(null, targetChainId, receiver, XTOType).send();
        assertEquals(expectedReceiver.getBalance(), actualReceiver.getBalance());
    }

    /**
     * CommonTx - Sender가 Input 데이터를 3KB로 설정. (txCnt : 1, totalTxCnt : 2, fee : 3, total fee : 4)
     *
     * @throws Exception
     */
    @Test
    @Order(order = 4)
    public void CommonTxOverTxSize() throws Exception {
        //Over 3KB = 3069bit (3bit = Identifier)
        String data = "";
        for (int i = 0; i < 3069; i++) {
            data += "a";
        }

        TxRequest txRequest = makeDefaultBuilder()
                .withSender(sender)
                .withReceiver(receiver)
                .withPayloadType(ApiEnum.PayloadType.CommonType)
                .withFee(CurrencyUtil.generateXTO(CoinType, 3))
                .withAmount(CurrencyUtil.generateXTO(CoinType, 10))
                .withPayloadBody(new TxCommonBody(data))
                .build();

        TxSendResponse txSendResponse = xCube.sendTransaction(txRequest).send();
        assertNull(txSendResponse.getError());
        assertNotNull(txSendResponse.getResult());

        AccountBalanceResponse expectedSender = makeAccountBalance(sender, "6,999,976,000,000,000,000,000,000", "6,999,976,000,000,000,000,000,000", "0", "0", "0", XTOType);
        AccountBalanceResponse actualSender = xCube.getBalance(null, targetChainId, sender, XTOType).send();
        assertEquals(expectedSender.getBalance(), actualSender.getBalance());

        AccountBalanceResponse expectedReceiver = makeAccountBalance(receiver, "4,000,020,250,000,100,000,000,000", "3,999,920,000,000,000,000,000,000", "100,000,000,000,000,000,000", "250,000,100,000,000,000", "0", XTOType);
        AccountBalanceResponse actualReceiver = xCube.getBalance(null, targetChainId, receiver, XTOType).send();
        assertEquals(expectedReceiver.getBalance(), actualReceiver.getBalance());
    }

    /**
     * CommonTx - Sender와 Receiver가 같도록 설정 (txCnt : 1, totalTxCnt : 3, fee : 2, total fee : 6)
     *
     * @throws Exception
     */
    @Test
    @Order(order = 5)
    public void CommonTxSameSenderAndReceiver() throws Exception {
        TxRequest txRequest = makeDefaultBuilder()
                .withSender(sender)
                .withReceiver(sender)
                .withPayloadType(ApiEnum.PayloadType.CommonType)
                .withFee(CurrencyUtil.generateXTO(CoinType, 2))
                .withAmount(CurrencyUtil.generateXTO(CoinType, 10))
                .withPayloadBody(new TxCommonBody(""))
                .build();

        TxSendResponse txSendResponse = xCube.sendTransaction(txRequest).send();
        assertNull(txSendResponse.getError());
        assertNotNull(txSendResponse.getResult());

        AccountBalanceResponse expectedSender = makeAccountBalance(sender, "6,999,974,000,000,000,000,000,000", "6,999,974,000,000,000,000,000,000", "0", "0", "0", XTOType);
        AccountBalanceResponse actualSender = xCube.getBalance(null, targetChainId, sender, XTOType).send();
        assertEquals(expectedSender.getBalance(), actualSender.getBalance());
    }

    /**
     * FileTx - 유효성 체크
     *
     * @throws Exception
     */
    @Test
    @Order(order = 6)
    public void FileTxCheckValidation() throws Exception {
        //Op 필드가 정의되지 않은 값인 경우.
        TxFileBody.Builder txFileBuilder = new TxFileBody().builder()
                .withFile(new File(this.getClass().getResource(testFile).getFile()))
                .withInfo("xchain auth data");
        TxRequest txRequest = makeDefaultBuilder()
                .withSender(sender)
                .withReceiver(sender)
                .withPayloadType(ApiEnum.PayloadType.FileType)
                .withFee(CurrencyUtil.generateXTO(CoinType, 1))
                .withAmount(CurrencyUtil.generateXTO(CoinType, 0))
                .withPayloadBody(txFileBuilder.build())
                .build();
        TxSendResponse originSendResponse = xCube.sendTransaction(txRequest).send();
        assertNotNull(originSendResponse.getError());
        Assert.assertEquals(1, originSendResponse.getError().getCode());

        //RPC Size보다 큰 파일을 보낸경우. (Default : 1MB)
        txFileBuilder.withOp(ApiEnum.OpType.RegisterType).withFile(new File(this.getClass().getResource(testFileOver1MB).getFile()));
        txRequest = makeDefaultBuilder()
                .withSender(sender)
                .withReceiver(sender)
                .withPayloadType(ApiEnum.PayloadType.FileType)
                .withFee(CurrencyUtil.generateXTO(CoinType, 1))
                .withAmount(CurrencyUtil.generateXTO(CoinType, 0))
                .withPayloadBody(txFileBuilder.build())
                .build();
        originSendResponse = xCube.sendTransaction(txRequest).send();
        assertNotNull(originSendResponse.getError());
        Assert.assertEquals(506, originSendResponse.getError().getCode());

        //Sender와 Receiver가 다른경우.(최초 원본파일 등록시)
        txFileBuilder.withFile(new File(this.getClass().getResource(testFile).getFile()));
        txRequest = makeDefaultBuilder()
                .withSender(sender)
                .withReceiver(receiver)
                .withPayloadType(ApiEnum.PayloadType.FileType)
                .withFee(CurrencyUtil.generateXTO(CoinType, 1))
                .withAmount(CurrencyUtil.generateXTO(CoinType, 0))
                .withPayloadBody(txFileBuilder.build())
                .build();
        originSendResponse = xCube.sendTransaction(txRequest).send();
        assertNotNull(originSendResponse.getError());
        Assert.assertEquals(1, originSendResponse.getError().getCode());

        //Sender와 Receiver가 같은경우. (업데이트시)
        txFileBuilder.withOp(ApiEnum.OpType.UpdateType);
        txFileBuilder.withFile(new File(this.getClass().getResource(testFile).getFile()));
        txRequest = makeDefaultBuilder()
                .withSender(sender)
                .withReceiver(sender)
                .withPayloadType(ApiEnum.PayloadType.FileType)
                .withFee(CurrencyUtil.generateXTO(CoinType, 1))
                .withAmount(CurrencyUtil.generateXTO(CoinType, 0))
                .withPayloadBody(txFileBuilder.build())
                .build();
        originSendResponse = xCube.sendTransaction(txRequest).send();
        assertNotNull(originSendResponse.getError());
        Assert.assertEquals(1, originSendResponse.getError().getCode());

        //Amount 0보다 크게 설정한 경우.
        txRequest = makeDefaultBuilder()
                .withSender(sender)
                .withReceiver(sender)
                .withPayloadType(ApiEnum.PayloadType.FileType)
                .withFee(CurrencyUtil.generateXTO(CoinType, 1))
                .withAmount(CurrencyUtil.generateXTO(CoinType, 1))
                .withPayloadBody(txFileBuilder.build())
                .build();
        originSendResponse = xCube.sendTransaction(txRequest).send();
        assertNotNull(originSendResponse.getError());
        Assert.assertEquals(307, originSendResponse.getError().getCode());

        //Authors 입력하지 않은 경우.(최초 원본파일 등록시)
        txFileBuilder.withOp(ApiEnum.OpType.RegisterType);
        txFileBuilder.withFile(new File(this.getClass().getResource(testFile).getFile()));
        txRequest = makeDefaultBuilder()
                .withSender(sender)
                .withReceiver(sender)
                .withPayloadType(ApiEnum.PayloadType.FileType)
                .withFee(CurrencyUtil.generateXTO(CoinType, 1))
                .withAmount(CurrencyUtil.generateXTO(CoinType, 0))
                .withPayloadBody(txFileBuilder.build())
                .build();
        originSendResponse = xCube.sendTransaction(txRequest).send();
        assertNotNull(originSendResponse.getError());
        Assert.assertEquals(329, originSendResponse.getError().getCode());

        //모든 사용자 허용으로 한경우. 다른 주소를 추가했을때. (최초 원본파일 또는 업데이트시)
        List<String> authors = new ArrayList<>();
        authors.add(sender);
        authors.add("0xffffffffffffffffffffffffffffffffffffffff");
        txFileBuilder.withAuthors(authors);
        txRequest = makeDefaultBuilder()
                .withSender(sender)
                .withReceiver(sender)
                .withPayloadType(ApiEnum.PayloadType.FileType)
                .withFee(CurrencyUtil.generateXTO(CoinType, 1))
                .withAmount(CurrencyUtil.generateXTO(CoinType, 0))
                .withPayloadBody(txFileBuilder.build())
                .build();
        originSendResponse = xCube.sendTransaction(txRequest).send();
        assertNotNull(originSendResponse.getError());
        Assert.assertEquals(331, originSendResponse.getError().getCode());

        //모든 사용자 비허용으로 한경우. 다른 주소를 추가했을때. (최초 원본파일 또는 업데이트시)
        authors = new ArrayList<>();
        authors.add(sender);
        authors.add("0x0000000000000000000000000000000000000000");
        txRequest = makeDefaultBuilder()
                .withSender(sender)
                .withReceiver(sender)
                .withPayloadType(ApiEnum.PayloadType.FileType)
                .withFee(CurrencyUtil.generateXTO(CoinType, 1))
                .withAmount(CurrencyUtil.generateXTO(CoinType, 0))
                .withPayloadBody(txFileBuilder.build())
                .build();
        originSendResponse = xCube.sendTransaction(txRequest).send();
        assertNotNull(originSendResponse.getError());
        Assert.assertEquals(331, originSendResponse.getError().getCode());

        //dataHash 값을 설정하지 않은 경우.
        txRequest = makeDefaultBuilder()
                .withSender(sender)
                .withReceiver(sender)
                .withPayloadType(ApiEnum.PayloadType.FileType)
                .withFee(CurrencyUtil.generateXTO(CoinType, 1))
                .withAmount(CurrencyUtil.generateXTO(CoinType, 0))
                .withPayloadBody(txFileBuilder.allowAll().withFile(null).build())
                .build();
        originSendResponse = xCube.sendTransaction(txRequest).send();
        assertNotNull(originSendResponse.getError());
        Assert.assertEquals(1, originSendResponse.getError().getCode());
    }

    /**
     * FileTx - 유효성 체크 (txCnt : 4, totalTxCnt : 7, fee : 4, total fee : 10)
     *
     * @throws Exception
     */
    @Test
    @Order(order = 7)
    public void FileTxCheckRegisterValidation() throws Exception {
        TxFileBody.Builder txFileBuilder = new TxFileBody().builder()
                .withOp(ApiEnum.OpType.RegisterType)
                .withFile(new File(this.getClass().getResource(testFile).getFile()))
                .withInfo("xchain auth data");

        //아래 테스트 진행을 위한 파일등록.
        TxRequest txRequest = makeDefaultBuilder()
                .withSender(sender)
                .withReceiver(sender)
                .withPayloadType(ApiEnum.PayloadType.FileType)
                .withFee(CurrencyUtil.generateXTO(CoinType, 1))
                .withAmount(CurrencyUtil.generateXTO(CoinType, 0))
                .withPayloadBody(txFileBuilder.withAuthors(Arrays.asList(sender)).build())
                .build();
        SignUtil.signTx(txRequest, privKeyPassword, privKeyJson);

        TxSendResponse dataResponse = xCube.sendTransaction(txRequest).send();
        assertNull(dataResponse.getError());

        AccountBalanceResponse expectedSender = makeAccountBalance(sender, "6,999,973,000,000,000,000,000,000", "6,999,973,000,000,000,000,000,000", "0", "0", "0", XTOType);
        AccountBalanceResponse actualSender = xCube.getBalance(null, targetChainId, sender, XTOType).send();
        assertEquals(expectedSender.getBalance(), actualSender.getBalance());

        //파일 등록시 이미 존재하는 경우.
        TxSendResponse originSendResponse = xCube.sendTransaction(txRequest).send();
        assertNotNull(originSendResponse.getError());
        assertTrue(originSendResponse.getError().getCode() == 327 || originSendResponse.getError().getCode() == 336);

        //업데이트시 원본데이터가 존재하지 않는 경우.
        txRequest = makeDefaultBuilder()
                .withSender(sender)
                .withReceiver("0x60c4c997fd71a8822441d4f21a997a2acbab3333")
                .withPayloadType(ApiEnum.PayloadType.FileType)
                .withFee(CurrencyUtil.generateXTO(CoinType, 1))
                .withAmount(CurrencyUtil.generateXTO(CoinType, 0))
                .withPayloadBody(txFileBuilder.withOp(ApiEnum.OpType.UpdateType).withAuthors(Arrays.asList(sender)).build())
                .build();

        originSendResponse = xCube.sendTransaction(txRequest).send();
        assertNotNull(originSendResponse.getError());
        Assert.assertEquals(333, originSendResponse.getError().getCode());

        //업데이트 할 수 없는 사용자가 업데이트를 시도한 경우.
        txRequest = makeDefaultBuilder()
                .withSender(receiver)
                .withReceiver(dataResponse.getResult().getDataAccountAddr())
                .withPayloadType(ApiEnum.PayloadType.FileType)
                .withFee(CurrencyUtil.generateXTO(CoinType, 1))
                .withAmount(CurrencyUtil.generateXTO(CoinType, 0))
                .withPayloadBody(txFileBuilder.withOp(ApiEnum.OpType.UpdateType).withAuthors(Arrays.asList(sender)).build())
                .build();

        originSendResponse = xCube.sendTransaction(txRequest).send();
        assertNotNull(originSendResponse.getError());
        Assert.assertEquals(328, originSendResponse.getError().getCode());

        //모두 수정가능 하도록 파일등록
        txFileBuilder = new TxFileBody().builder()
                .withOp(ApiEnum.OpType.RegisterType)
                .allowAll()
                .withFile(new File(this.getClass().getResource(testFile).getFile()))
                .withInfo("xchain auth data");

        txRequest = makeDefaultBuilder()
                .withSender(sender)
                .withReceiver(sender)
                .withPayloadType(ApiEnum.PayloadType.FileType)
                .withFee(CurrencyUtil.generateXTO(CoinType, 1))
                .withAmount(CurrencyUtil.generateXTO(CoinType, 0))
                .withPayloadBody(txFileBuilder.build())
                .build();
        SignUtil.signTx(txRequest, privKeyPassword, privKeyJson);

        dataResponse = xCube.sendTransaction(txRequest).send();
        assertNull(dataResponse.getError());

        expectedSender = makeAccountBalance(sender, "6,999,972,000,000,000,000,000,000", "6,999,972,000,000,000,000,000,000", "0", "0", "0", XTOType);
        actualSender = xCube.getBalance(null, targetChainId, sender, XTOType).send();
        assertEquals(expectedSender.getBalance(), actualSender.getBalance());

        //모두 수정가능한 파일이 수정되는지 확인.
        txRequest = makeDefaultBuilder()
                .withSender(receiver)
                .withReceiver(dataResponse.getResult().getDataAccountAddr())
                .withPayloadType(ApiEnum.PayloadType.FileType)
                .withFee(CurrencyUtil.generateXTO(CoinType, 1))
                .withAmount(CurrencyUtil.generateXTO(CoinType, 0))
                .withPayloadBody(txFileBuilder.withOp(ApiEnum.OpType.UpdateType).build())
                .build();

        originSendResponse = xCube.sendTransaction(txRequest).send();
        assertNull(originSendResponse.getError());

        expectedSender = makeAccountBalance(receiver, "4,000,021,000,000,500,000,000,000", "3,999,919,000,000,000,000,000,000", "100,000,000,000,000,000,000", "2,000,000,500,000,000,000", "0", XTOType);
        actualSender = xCube.getBalance(null, targetChainId, receiver, XTOType).send();
        assertEquals(expectedSender.getBalance(), actualSender.getBalance());

        //모두 수정 불가능 하도록 파일등록
        txFileBuilder = new TxFileBody().builder()
                .withOp(ApiEnum.OpType.RegisterType)
                .notAllowAll()
                .withFile(new File(this.getClass().getResource(testFile).getFile()))
                .withInfo("xchain auth data");

        txRequest = makeDefaultBuilder()
                .withTime(new BigInteger(String.valueOf(DateUtil.getMicroSecond())))
                .withSender(sender)
                .withReceiver(sender)
                .withPayloadType(ApiEnum.PayloadType.FileType)
                .withFee(CurrencyUtil.generateXTO(CoinType, 1))
                .withAmount(CurrencyUtil.generateXTO(CoinType, 0))
                .withPayloadBody(txFileBuilder.build())
                .build();
        SignUtil.signTx(txRequest, privKeyPassword, privKeyJson);

        dataResponse = xCube.sendTransaction(txRequest).send();
        assertNull(dataResponse.getError());

        expectedSender = makeAccountBalance(sender, "6,999,971,000,000,000,000,000,000", "6,999,971,000,000,000,000,000,000", "0", "0", "0", XTOType);
        actualSender = xCube.getBalance(null, targetChainId, sender, XTOType).send();
        assertEquals(expectedSender.getBalance(), actualSender.getBalance());

        //모두 수정이 불가능한지 확인
        txRequest = makeDefaultBuilder()
                .withSender(receiver)
                .withReceiver(dataResponse.getResult().getDataAccountAddr())
                .withPayloadType(ApiEnum.PayloadType.FileType)
                .withFee(CurrencyUtil.generateXTO(CoinType, 1))
                .withAmount(CurrencyUtil.generateXTO(CoinType, 0))
                .withPayloadBody(txFileBuilder.withOp(ApiEnum.OpType.UpdateType).build())
                .build();

        originSendResponse = xCube.sendTransaction(txRequest).send();
        assertNotNull(originSendResponse.getError());
        Assert.assertEquals(328, originSendResponse.getError().getCode());
    }

    /**
     * FileTx - 원본체크 (txCnt : 1, totalTxCnt : 8, fee : 1, total fee : 11)
     *
     * @throws Exception
     */
    @Test
    @Order(order = 8)
    public void FileTxCheckOrigin() throws Exception {
        File file = new File(this.getClass().getResource(testFile).getFile());
        TxFileBody.Builder txFileBuilder = new TxFileBody().builder()
                .withOp(ApiEnum.OpType.RegisterType)
                .withFile(file)
                .withInfo("xchain auth data");

        //아래 테스트 진행을 위한 파일등록.
        TxRequest txRequest = makeDefaultBuilder()
                .withSender(sender)
                .withReceiver(sender)
                .withPayloadType(ApiEnum.PayloadType.FileType)
                .withFee(CurrencyUtil.generateXTO(CoinType, 1))
                .withAmount(CurrencyUtil.generateXTO(CoinType, 0))
                .withPayloadBody(txFileBuilder.withAuthors(Arrays.asList(sender)).build())
                .build();
        TxSendResponse dataResponse = xCube.sendTransaction(txRequest).send();
        assertNull(dataResponse.getError());

        DataAccountResponse dataAccountResponse = xCube.getDataAccount(null, targetChainId, dataResponse.getResult().getDataAccountAddr()).send();
        assertNull(dataAccountResponse.getError());
        assertNotNull(dataAccountResponse.getResult());

        //원본파일 확인, 위에서 저장한 파일과 같은 파일 (다음 블록이 생성되기전 일치여부 확인)
        TxCheckOriginalResponse checkOriginal = xCube.checkOriginal(null, targetChainId, dataResponse.getResult().getDataAccountAddr(), file).send();
        assertNotNull(checkOriginal.getError());

        //Wait for proof block creation
        Thread.sleep(4000);

        //원본파일 확인, 위에서 저장한 파일과 같은 파일 (일치여부 확인)
        checkOriginal = xCube.checkOriginal(null, targetChainId, dataResponse.getResult().getDataAccountAddr(), file).send();
        assertNull(checkOriginal.getError());
        assertNotNull(checkOriginal.getOrigin());
        Assert.assertEquals(dataResponse.getResult().getDataAccountAddr(), checkOriginal.getOrigin().getAddress());
        assertEquals(true, checkOriginal.getOrigin().isResult());
        assertEquals(1, checkOriginal.getOrigin().getConfirmations());
        assertTrue(checkOriginal.getOrigin().getDataHash() != null && !"".equals(checkOriginal.getOrigin().getDataHash()));

        //원본파일 확인, 위에서 저장한 파일과 다른 파일 (불일치여부 확인)
        checkOriginal = xCube.checkOriginal(null, targetChainId, dataResponse.getResult().getDataAccountAddr(), new File(this.getClass().getResource(testDummyFile).getFile())).send();
        assertNotNull(checkOriginal.getError());
        Assert.assertEquals(334, checkOriginal.getError().getCode());
    }

    /**
     * FileTx - File 오버라이딩 (txCnt : 5, totalTxCnt : 13, fee : 5, total fee : 16)
     *
     * @throws Exception
     */
    @Test
    @Order(order = 9)
    public void FileTxOverriding() throws Exception {
        File file = new File(this.getClass().getResource(testFile).getFile());
        TxFileBody.Builder txFileBuilder = new TxFileBody().builder()
                .withOp(ApiEnum.OpType.RegisterType)
                .withFile(file)
                .withReserved("test reserve")
                .withInfo("xchain auth data");

        //아래 테스트 진행을 위한 파일등록.
        TxRequest txRequest = makeDefaultBuilder()
                .withSender(sender)
                .withReceiver(sender)
                .withPayloadType(ApiEnum.PayloadType.FileType)
                .withFee(CurrencyUtil.generateXTO(CoinType, 1))
                .withAmount(CurrencyUtil.generateXTO(CoinType, 0))
                .withPayloadBody(txFileBuilder.allowAll().build())
                .build();
        TxSendResponse dataResponse = xCube.sendTransaction(txRequest).send();
        assertNull(dataResponse.getError());

        AccountBalanceResponse expectedSender = makeAccountBalance(sender, "6,999,969,000,000,000,000,000,000", "6,999,969,000,000,000,000,000,000", "0", "0", "0", XTOType);
        AccountBalanceResponse actualSender = xCube.getBalance(null, targetChainId, sender, XTOType).send();
        assertEquals(expectedSender.getBalance(), actualSender.getBalance());

        DataAccountResponse dataAccountResponse = xCube.getDataAccount(null, targetChainId, dataResponse.getResult().getDataAccountAddr()).send();
        Assert.assertEquals(dataResponse.getResult().getTxHash(), dataAccountResponse.getResult().getTxHash());
        Assert.assertEquals(dataResponse.getResult().getDataAccountAddr(), dataAccountResponse.getResult().getAddress());
        assertNotNull(dataAccountResponse.getResult());
        assertNotNull(dataAccountResponse.getResult().getAddress());

        //전체필드 변경없음.
        //이전 데이터 상속필드 : Authors, DataHash, Reserved, Info
        DataAccountResponse.Result expectedDataAccount = DataAccountResponse.builder()
                .withAddress(dataAccountResponse.getResult().getAddress())
                .withOpCnt(dataAccountResponse.getResult().getOpCnt() + 1)
                .withCreator(dataAccountResponse.getResult().getCreator())
                .withBlockNo(dataAccountResponse.getResult().getBlockNo() + 1)
                .withAuthors(dataAccountResponse.getResult().getAuthors())
                .withDataHash(dataAccountResponse.getResult().getDataHash())
                .withReserved(dataAccountResponse.getResult().getReserved())
                .withInfo(dataAccountResponse.getResult().getInfo())
                .build();

        txFileBuilder.withOp(ApiEnum.OpType.UpdateType);
        txRequest = makeDefaultBuilder()
                .withSender(sender)
                .withReceiver(dataResponse.getResult().getDataAccountAddr())
                .withPayloadType(ApiEnum.PayloadType.FileType)
                .withFee(CurrencyUtil.generateXTO(CoinType, 1))
                .withAmount(CurrencyUtil.generateXTO(CoinType, 0))
                .withPayloadBody(txFileBuilder.build())
                .build();
        dataResponse = xCube.sendTransaction(txRequest).send();
        assertNull(dataResponse.getError());

        expectedSender = makeAccountBalance(sender, "6,999,968,000,000,000,000,000,000", "6,999,968,000,000,000,000,000,000", "0", "0", "0", XTOType);
        actualSender = xCube.getBalance(null, targetChainId, sender, XTOType).send();
        assertEquals(expectedSender.getBalance(), actualSender.getBalance());

        dataAccountResponse = xCube.getDataAccount(null, targetChainId, dataResponse.getResult().getDataAccountAddr()).send();
        Assert.assertEquals(dataResponse.getResult().getTxHash(), dataAccountResponse.getResult().getTxHash());
        Assert.assertEquals(dataResponse.getResult().getDataAccountAddr(), dataAccountResponse.getResult().getAddress());
        Assert.assertEquals(expectedDataAccount.getAddress(), dataAccountResponse.getResult().getAddress());
        Assert.assertEquals(expectedDataAccount.getOpCnt(), dataAccountResponse.getResult().getOpCnt());
        Assert.assertEquals(expectedDataAccount.getBlockNo(), dataAccountResponse.getResult().getBlockNo());
        Assert.assertEquals(expectedDataAccount.getAuthors(), dataAccountResponse.getResult().getAuthors());
        Assert.assertEquals(expectedDataAccount.getReserved(), dataAccountResponse.getResult().getReserved());
        Assert.assertEquals(expectedDataAccount.getInfo(), dataAccountResponse.getResult().getInfo());
        Assert.assertEquals(expectedDataAccount.getDataHash(), dataAccountResponse.getResult().getDataHash());

        //일부필드 변경 : Authors
        //이전 데이터 상속필드 : DataHash, Reserved, Info
        expectedDataAccount = DataAccountResponse.builder()
                .withAddress(dataAccountResponse.getResult().getAddress())
                .withOpCnt(dataAccountResponse.getResult().getOpCnt() + 1)
                .withCreator(dataAccountResponse.getResult().getCreator())
                .withBlockNo(dataAccountResponse.getResult().getBlockNo() + 1)
                .withAuthors(dataAccountResponse.getResult().getAuthors())
                .withDataHash(dataAccountResponse.getResult().getDataHash())
                .withReserved(dataAccountResponse.getResult().getReserved())
                .withInfo(dataAccountResponse.getResult().getInfo())
                .build();

        txFileBuilder.withOp(ApiEnum.OpType.UpdateType).withAuthors(Arrays.asList(sender));
        txRequest = makeDefaultBuilder()
                .withSender(sender)
                .withReceiver(dataResponse.getResult().getDataAccountAddr())
                .withPayloadType(ApiEnum.PayloadType.FileType)
                .withFee(CurrencyUtil.generateXTO(CoinType, 1))
                .withAmount(CurrencyUtil.generateXTO(CoinType, 0))
                .withPayloadBody(txFileBuilder.build())
                .build();
        dataResponse = xCube.sendTransaction(txRequest).send();
        assertNull(dataResponse.getError());

        expectedSender = makeAccountBalance(sender, "6,999,967,000,000,000,000,000,000", "6,999,967,000,000,000,000,000,000", "0", "0", "0", XTOType);
        actualSender = xCube.getBalance(null, targetChainId, sender, XTOType).send();
        assertEquals(expectedSender.getBalance(), actualSender.getBalance());

        dataAccountResponse = xCube.getDataAccount(null, targetChainId, dataResponse.getResult().getDataAccountAddr()).send();
        Assert.assertEquals(dataResponse.getResult().getTxHash(), dataAccountResponse.getResult().getTxHash());
        Assert.assertEquals(dataResponse.getResult().getDataAccountAddr(), dataAccountResponse.getResult().getAddress());
        Assert.assertEquals(expectedDataAccount.getAddress(), dataAccountResponse.getResult().getAddress());
        Assert.assertEquals(expectedDataAccount.getOpCnt(), dataAccountResponse.getResult().getOpCnt());
        Assert.assertEquals(expectedDataAccount.getBlockNo(), dataAccountResponse.getResult().getBlockNo());
        Assert.assertNotEquals(expectedDataAccount.getAuthors(), dataAccountResponse.getResult().getAuthors());
        Assert.assertEquals(expectedDataAccount.getReserved(), dataAccountResponse.getResult().getReserved());
        Assert.assertEquals(expectedDataAccount.getInfo(), dataAccountResponse.getResult().getInfo());
        Assert.assertEquals(expectedDataAccount.getDataHash(), dataAccountResponse.getResult().getDataHash());

        //일부필드 변경 : Reserved
        //이전 데이터 상속필드 : Authors, DataHash, Info
        expectedDataAccount = DataAccountResponse.builder()
                .withAddress(dataAccountResponse.getResult().getAddress())
                .withOpCnt(dataAccountResponse.getResult().getOpCnt() + 1)
                .withCreator(dataAccountResponse.getResult().getCreator())
                .withBlockNo(dataAccountResponse.getResult().getBlockNo() + 1)
                .withAuthors(dataAccountResponse.getResult().getAuthors())
                .withDataHash(dataAccountResponse.getResult().getDataHash())
                .withReserved(dataAccountResponse.getResult().getReserved())
                .withInfo(dataAccountResponse.getResult().getInfo())
                .build();

        txFileBuilder.withOp(ApiEnum.OpType.UpdateType).withReserved("change reserved");
        txRequest = makeDefaultBuilder()
                .withSender(sender)
                .withReceiver(dataResponse.getResult().getDataAccountAddr())
                .withPayloadType(ApiEnum.PayloadType.FileType)
                .withFee(CurrencyUtil.generateXTO(CoinType, 1))
                .withAmount(CurrencyUtil.generateXTO(CoinType, 0))
                .withPayloadBody(txFileBuilder.build())
                .build();
        dataResponse = xCube.sendTransaction(txRequest).send();
        assertNull(dataResponse.getError());

        expectedSender = makeAccountBalance(sender, "6,999,966,000,000,000,000,000,000", "6,999,966,000,000,000,000,000,000", "0", "0", "0", XTOType);
        actualSender = xCube.getBalance(null, targetChainId, sender, XTOType).send();
        assertEquals(expectedSender.getBalance(), actualSender.getBalance());

        dataAccountResponse = xCube.getDataAccount(null, targetChainId, dataResponse.getResult().getDataAccountAddr()).send();
        Assert.assertEquals(dataResponse.getResult().getTxHash(), dataAccountResponse.getResult().getTxHash());
        Assert.assertEquals(dataResponse.getResult().getDataAccountAddr(), dataAccountResponse.getResult().getAddress());
        Assert.assertEquals(expectedDataAccount.getAddress(), dataAccountResponse.getResult().getAddress());
        Assert.assertEquals(expectedDataAccount.getOpCnt(), dataAccountResponse.getResult().getOpCnt());
        Assert.assertEquals(expectedDataAccount.getBlockNo(), dataAccountResponse.getResult().getBlockNo());
        Assert.assertEquals(expectedDataAccount.getAuthors(), dataAccountResponse.getResult().getAuthors());
        Assert.assertNotEquals(expectedDataAccount.getReserved(), dataAccountResponse.getResult().getReserved());
        Assert.assertEquals(expectedDataAccount.getInfo(), dataAccountResponse.getResult().getInfo());
        Assert.assertEquals(expectedDataAccount.getDataHash(), dataAccountResponse.getResult().getDataHash());

        //일부필드 변경 : Info, DataHash
        //이전 데이터 상속필드 : Authors, Reserved
        expectedDataAccount = DataAccountResponse.builder()
                .withAddress(dataAccountResponse.getResult().getAddress())
                .withOpCnt(dataAccountResponse.getResult().getOpCnt() + 1)
                .withCreator(dataAccountResponse.getResult().getCreator())
                .withBlockNo(dataAccountResponse.getResult().getBlockNo() + 1)
                .withAuthors(dataAccountResponse.getResult().getAuthors())
                .withDataHash(dataAccountResponse.getResult().getDataHash())
                .withReserved(dataAccountResponse.getResult().getReserved())
                .withInfo(dataAccountResponse.getResult().getInfo())
                .build();

        file = new File(this.getClass().getResource(testDummyFile).getFile());
        txFileBuilder.withOp(ApiEnum.OpType.UpdateType).withFile(file).withInfo("change info");
        txRequest = makeDefaultBuilder()
                .withSender(sender)
                .withReceiver(dataResponse.getResult().getDataAccountAddr())
                .withPayloadType(ApiEnum.PayloadType.FileType)
                .withFee(CurrencyUtil.generateXTO(CoinType, 1))
                .withAmount(CurrencyUtil.generateXTO(CoinType, 0))
                .withPayloadBody(txFileBuilder.build())
                .build();
        dataResponse = xCube.sendTransaction(txRequest).send();
        assertNull(dataResponse.getError());

        expectedSender = makeAccountBalance(sender, "6,999,965,000,000,000,000,000,000", "6,999,965,000,000,000,000,000,000", "0", "0", "0", XTOType);
        actualSender = xCube.getBalance(null, targetChainId, sender, XTOType).send();
        assertEquals(expectedSender.getBalance(), actualSender.getBalance());

        dataAccountResponse = xCube.getDataAccount(null, targetChainId, dataResponse.getResult().getDataAccountAddr()).send();
        Assert.assertEquals(dataResponse.getResult().getTxHash(), dataAccountResponse.getResult().getTxHash());
        Assert.assertEquals(dataResponse.getResult().getDataAccountAddr(), dataAccountResponse.getResult().getAddress());
        Assert.assertEquals(expectedDataAccount.getAddress(), dataAccountResponse.getResult().getAddress());
        Assert.assertEquals(expectedDataAccount.getOpCnt(), dataAccountResponse.getResult().getOpCnt());
        Assert.assertEquals(expectedDataAccount.getBlockNo(), dataAccountResponse.getResult().getBlockNo());
        Assert.assertEquals(expectedDataAccount.getAuthors(), dataAccountResponse.getResult().getAuthors());
        Assert.assertEquals(expectedDataAccount.getReserved(), dataAccountResponse.getResult().getReserved());
        Assert.assertNotEquals(expectedDataAccount.getInfo(), dataAccountResponse.getResult().getInfo());
        Assert.assertNotEquals(expectedDataAccount.getDataHash(), dataAccountResponse.getResult().getDataHash());
    }

    /**
     * BondingTx - 유효성 체크
     *
     * @throws Exception
     */
    @Test
    @Order(order = 10)
    public void BondingTxCheckValidation() throws Exception {
        //GR에 설정된 Fee보다 작게 설정한 경우.
        TxRequest txRequest = makeDefaultBuilder()
                .withSender(sender)
                .withReceiver(sender)
                .withPayloadType(ApiEnum.PayloadType.BondingType)
                .withFee(CurrencyUtil.generateXTO(CoinType, 1))
                .withAmount(CurrencyUtil.generateXTO(CoinType, 0))
                .withPayloadBody(new TxBondingBody())
                .build();
        TxSendResponse sendResponse = xCube.bonding(txRequest).send();
        assertNotNull(sendResponse.getError());
        Assert.assertEquals(1, sendResponse.getError().getCode());

        //Sender와 Receiver를 다르게 설정한 경우.
        txRequest = makeDefaultBuilder()
                .withSender(sender)
                .withReceiver(receiver)
                .withPayloadType(ApiEnum.PayloadType.BondingType)
                .withFee(CurrencyUtil.generateXTO(CoinType, 10000))
                .withAmount(CurrencyUtil.generateXTO(CoinType, 1))
                .withPayloadBody(new TxBondingBody())
                .build();
        sendResponse = xCube.bonding(txRequest).send();
        assertNotNull(sendResponse.getError());
        Assert.assertEquals(309, sendResponse.getError().getCode());

        //BondingBody의 Amount가 0보다 크지않은 경우.
        txRequest = makeDefaultBuilder()
                .withSender(sender)
                .withReceiver(sender)
                .withPayloadType(ApiEnum.PayloadType.BondingType)
                .withFee(CurrencyUtil.generateXTO(CoinType, 10000))
                .withAmount(new BigInteger("0"))
                .withPayloadBody(new TxBondingBody())
                .build();
        sendResponse = xCube.bonding(txRequest).send();
        assertNotNull(sendResponse.getError());
        Assert.assertEquals(1, sendResponse.getError().getCode());

        //BondingBody의 Amount가 계정 잔액보다 큰경우.
        txRequest = makeDefaultBuilder()
                .withSender(sender)
                .withReceiver(sender)
                .withPayloadType(ApiEnum.PayloadType.BondingType)
                .withFee(CurrencyUtil.generateXTO(CoinType, 10000))
                .withAmount(CurrencyUtil.generateXTO(CoinType, 699000100))
                .withPayloadBody(new TxBondingBody())
                .build();
        sendResponse = xCube.bonding(txRequest).send();
        assertNotNull(sendResponse.getError());
        Assert.assertEquals(201, sendResponse.getError().getCode());

        //ATX 단위로 본딩을 하지 않은 경우.
        txRequest = makeDefaultBuilder()
                .withSender(sender)
                .withReceiver(sender)
                .withPayloadType(ApiEnum.PayloadType.BondingType)
                .withFee(CurrencyUtil.generateXTO(CoinType, 10000))
                .withAmount(CurrencyUtil.generateXTO(XTOType, Long.parseLong("1,000,000,000,000,000,001".replaceAll(",", ""))))
                .withPayloadBody(new TxBondingBody())
                .build();
        sendResponse = xCube.bonding(txRequest).send();
        assertNotNull(sendResponse.getError());
        Assert.assertEquals(344, sendResponse.getError().getCode());
    }

    /**
     * BondintTx - 본딩 (txCnt : 1, totalTxCnt : 14, fee : 10,000, total fee : 10,016)
     *
     * @throws Exception
     */
    @Test
    @Order(order = 11)
    public void BondingTxBonding() throws Exception {
        //위의까지 테스트 결과 Validator Account 잔고 확인
        AccountBalanceResponse expectedSender = makeAccountBalance(receiver, "4,000,022,000,001,000,000,000,000", "3,999,919,000,000,000,000,000,000", "100,000,000,000,000,000,000", "3,000,001,000,000,000,000", "0", XTOType);
        AccountBalanceResponse actualSender = xCube.getBalance(null, targetChainId, receiver, XTOType).send();
        assertEquals(expectedSender.getBalance(), actualSender.getBalance());

        //Account Bonding 확인
        AccountBondInfoResponse expectedBondingInfo = new AccountBondInfoResponse();
        expectedBondingInfo.setResult(new AccountBondInfoResponse.Result(
                CurrencyUtil.generateXTO(CoinType, 100),
                CurrencyUtil.generateXTO(CoinType, 0),
                CurrencyUtil.generateXTO(CoinType, 0),
                new HashMap<>()
        ));
        AccountBondInfoResponse actualBondingInfo = xCube.getBonding(null, targetChainId, receiver, null).send();
        Assert.assertEquals(expectedBondingInfo.getResult(), actualBondingInfo.getResult());

        //Validator 추가 본딩
        TxRequest txRequest = makeDefaultBuilder()
                .withSender(receiver)
                .withReceiver(receiver)
                .withPayloadType(ApiEnum.PayloadType.BondingType)
                .withFee(CurrencyUtil.generateXTO(CoinType, 10000))
                .withAmount(CurrencyUtil.generateXTO(CoinType, 1))
                .withPayloadBody(new TxBondingBody())
                .build();
        TxSendResponse sendResponse = xCube.bonding(txRequest).send();
        assertNull(sendResponse.getError());

        //추가 본딩 후 Validator Account 잔고 확인
        ValidatorListResponse validatorListResponse = xCube.getValidatorList(null, targetChainId).send();
        expectedSender = makeAccountBalance(receiver, "3,990,023,000,001,400,000,000,000", "3,999,918,000,000,000,000,000,000", "101,000,000,000,000,000,000", "4,000,001,400,000,000,000", "0", XTOType);
        actualSender = xCube.getBalance(null, targetChainId, receiver, XTOType).send();
        assertEquals(expectedSender.getBalance(), actualSender.getBalance());

        //추가 본딩 후 Account Bonding 확인
        expectedBondingInfo.setResult(new AccountBondInfoResponse.Result(
                CurrencyUtil.generateXTO(CoinType, 101),
                CurrencyUtil.generateXTO(CoinType, 0),
                CurrencyUtil.generateXTO(CoinType, 0),
                new HashMap<>()
        ));
        actualBondingInfo = xCube.getBonding(null, targetChainId, receiver, XTOType).send();
        Assert.assertEquals(expectedBondingInfo.getResult(), actualBondingInfo.getResult());
    }

    @Test
    @Order(order = 12)
    public void UnbondingTxCheckValidation() throws Exception {
        //Sender와 Receiver를 다르게 설정한 경우.
        TxRequest txRequest = makeDefaultBuilder()
                .withSender(validator)
                .withReceiver(receiver)
                .withPayloadType(ApiEnum.PayloadType.UnbondingType)
                .withFee(CurrencyUtil.generateXTO(CoinType, 1))
                .withAmount(CurrencyUtil.generateXTO(CoinType, 1))
                .withPayloadBody(new TxUnbondingBody())
                .build();
        TxSendResponse sendResponse = xCube.unbonding(txRequest).send();
        assertNotNull(sendResponse.getError());
        Assert.assertEquals(309, sendResponse.getError().getCode());

        //UnbondingBody의 Amount가 0보다 크지않은 경우.
        txRequest = makeDefaultBuilder()
                .withSender(validator)
                .withReceiver(validator)
                .withPayloadType(ApiEnum.PayloadType.UnbondingType)
                .withFee(CurrencyUtil.generateXTO(CoinType, 1))
                .withAmount(new BigInteger("0"))
                .withPayloadBody(new TxUnbondingBody())
                .build();
        sendResponse = xCube.unbonding(txRequest).send();
        assertNotNull(sendResponse.getError());
        Assert.assertEquals(1, sendResponse.getError().getCode());

        //UnbondingBody의 Amount가 Bonding한 양보다 큰경우.
        txRequest = makeDefaultBuilder()
                .withSender(validator)
                .withReceiver(validator)
                .withPayloadType(ApiEnum.PayloadType.UnbondingType)
                .withFee(CurrencyUtil.generateXTO(CoinType, 1))
                .withAmount(CurrencyUtil.generateXTO(CoinType, 8000002))
                .withPayloadBody(new TxUnbondingBody())
                .build();
        sendResponse = xCube.unbonding(txRequest).send();
        assertNotNull(sendResponse.getError());
        Assert.assertEquals(403, sendResponse.getError().getCode());

        //Unbonding 하고자 하는 Validator가 존재하지 않는 경우.
        txRequest = makeDefaultBuilder()
                .withSender(receiver)
                .withReceiver(receiver)
                .withPayloadType(ApiEnum.PayloadType.UnbondingType)
                .withFee(CurrencyUtil.generateXTO(CoinType, 1))
                .withAmount(CurrencyUtil.generateXTO(CoinType, 100))
                .withPayloadBody(new TxUnbondingBody())
                .build();
        sendResponse = xCube.unbonding(txRequest).send();
        assertNotNull(sendResponse.getError());
        Assert.assertEquals(401, sendResponse.getError().getCode());

        //ATX 단위로 언본딩을 하지 않은 경우.
        txRequest = makeDefaultBuilder()
                .withSender(receiver)
                .withReceiver(receiver)
                .withPayloadType(ApiEnum.PayloadType.UnbondingType)
                .withFee(CurrencyUtil.generateXTO(CoinType, 1))
                .withAmount(CurrencyUtil.generateXTO(XTOType, Long.parseLong("1,000,000,000,000,000,001".replaceAll(",", ""))))
                .withPayloadBody(new TxUnbondingBody())
                .build();
        sendResponse = xCube.bonding(txRequest).send();
        assertNotNull(sendResponse.getError());
        Assert.assertEquals(344, sendResponse.getError().getCode());
    }

    /**
     * UnbondintTx - unbonding (txCnt : 1, totalTxCnt : 15, fee : 1, total fee : 10,017)
     *
     * @throws Exception
     */
    @Test
    @Order(order = 13)
    public void UnbondingTxUnbonding() throws Exception {
        //위의까지 테스트 결과 Validator Account 잔고 확인
        AccountBalanceResponse expectedSender = makeAccountBalance(receiver, "9,990,016,128,000,000,000,000,000", "1,989,999,000,000,000,000,000,000", "8,000,001,000,000,000,000,000,000", "16,128,000,000,000,000,000", "0", XTOType);
        AccountBalanceResponse actualSender = xCube.getBalance(null, targetChainId, receiver, XTOType).send();
        System.out.println(actualSender.getBalance());
//        assertEquals(expectedSender.getBalance(), actualSender.getBalance());

        //Account Bonding 확인
        AccountBondInfoResponse expectedBondingInfo = new AccountBondInfoResponse();
        expectedBondingInfo.setResult(new AccountBondInfoResponse.Result(
                CurrencyUtil.generateXTO(CoinType, 101),
                CurrencyUtil.generateXTO(CoinType, 0),
                CurrencyUtil.generateXTO(CoinType, 0),
                new HashMap<>()
        ));
        AccountBondInfoResponse actualBondingInfo = xCube.getBonding(null, targetChainId, receiver, null).send();
        Assert.assertEquals(expectedBondingInfo.getResult(), actualBondingInfo.getResult());

        TxRequest txRequest = makeDefaultBuilder()
                .withSender(receiver)
                .withReceiver(receiver)
                .withPayloadType(ApiEnum.PayloadType.UnbondingType)
                .withFee(CurrencyUtil.generateXTO(CoinType, 1))
                .withAmount(CurrencyUtil.generateXTO(CoinType, 10))
                .withPayloadBody(new TxUnbondingBody())
                .build();
        TxSendResponse sendResponse = xCube.unbonding(txRequest).send();
        assertNull(sendResponse.getError());

        expectedSender = makeAccountBalance(receiver, "10,000,015,136,000,000,995,750,019", "1,989,998,000,000,000,000,000,000", "7,200,001,000,000,000,000,000,000", "9,014,522,525,200,980,550,019", "801,001,613,474,800,015,200,000", XTOType);
        actualSender = xCube.getBalance(null, targetChainId, receiver, XTOType).send();
        System.out.println(actualSender.getBalance());
//        assertEquals(expectedSender.getBalance(), actualSender.getBalance());

        //Account Bonding 확인
        expectedBondingInfo.setResult(new AccountBondInfoResponse.Result(
                CurrencyUtil.generateXTO(CoinType, 91),
                CurrencyUtil.generateXTO(CoinType, 0),
                CurrencyUtil.generateXTO(CoinType, 0),
                null
        ));
        actualBondingInfo = xCube.getBonding(null, targetChainId, receiver, null).send();
        assertEquals(expectedBondingInfo, actualBondingInfo);
    }

    @Test
    public void testorder() throws Exception {
        CommonTx();
        CommonTxOverTxSize();
        CommonTxSameSenderAndReceiver();
        FileTxCheckRegisterValidation();
        FileTxCheckOrigin();
        FileTxOverriding();
        BondingTxCheckValidation();
        BondingTxBonding();
//        UnbondingTxCheckValidation();
//        UnbondingTxUnbonding();
    }

    @Test
    @Order(order = 200)
    public void test() {
        ValidatorListResponse validatorListResponse = xCube.getValidatorList(null, targetChainId).send();
        System.out.println(JsonUtil.generateClassToJson(validatorListResponse.getResult()));
    }
}
