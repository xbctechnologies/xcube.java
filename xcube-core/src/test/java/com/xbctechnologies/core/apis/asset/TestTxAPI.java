package com.xbctechnologies.core.apis.asset;

import com.xbctechnologies.core.TestParent;
import com.xbctechnologies.core.apis.dto.ApiEnum;
import com.xbctechnologies.core.apis.dto.req.tx.TxRequest;
import com.xbctechnologies.core.apis.dto.res.BoolResponse;
import com.xbctechnologies.core.apis.dto.res.account.AccountBalanceResponse;
import com.xbctechnologies.core.apis.dto.res.account.AccountBondInfoResponse;
import com.xbctechnologies.core.apis.dto.res.data.*;
import com.xbctechnologies.core.apis.dto.res.tx.TxCheckOriginalResponse;
import com.xbctechnologies.core.apis.dto.res.tx.TxSendResponse;
import com.xbctechnologies.core.apis.dto.xtypes.*;
import com.xbctechnologies.core.order.Order;
import com.xbctechnologies.core.order.OrderedRunner;
import com.xbctechnologies.core.utils.*;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;
import java.math.BigInteger;
import java.util.*;

import static com.xbctechnologies.core.utils.CurrencyUtil.CurrencyType.CoinType;
import static com.xbctechnologies.core.utils.CurrencyUtil.CurrencyType.XTOType;
import static org.junit.Assert.*;

/**
 * 총 10가지 Transaction에 대해 XNode와의 통합 테스트를 진행한다.
 * 각 트랜잭션별 유효성 검사 및 DB에 저장된 값이 올바른지를 검증한다.
 */
@RunWith(OrderedRunner.class)
public class TestTxAPI extends TestParent {
    private static long startCurrentBlockNo = 26;
    private static long startEndOfVotingBlockNo = 28;
    private static long startReflectionBlockNo = 29;

    private TxRequest.Builder makeDefaultBuilder() {
        return TxRequest.builder()
                .withIsSync(true)
                .withTargetChainId(targetChainId)
                .withNotCheckValidation(true);
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
        Assert.assertEquals(304, txSendResponse.getError().getCode());

        //TargetChainID 와 CLI에 설정되 ChainID가 다른경우.
        txRequest = txRequest = makeDefaultBuilder()
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
        Assert.assertEquals(304, txSendResponse.getError().getCode());

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

        AccountBalanceResponse expectedSender = makeAccountBalance(sender, "6,999,989", "6,999,989", "0", "0", "0", CoinType);
        AccountBalanceResponse actualSender = xCube.getBalance(null, targetChainId, sender, CoinType).send();
        assertEquals(expectedSender.getBalance(), actualSender.getBalance());

        AccountBalanceResponse expectedReceiver = makeAccountBalance(receiver, "4,000,010", "4,000,010", "0", "0", "0", CoinType);
        AccountBalanceResponse actualReceiver = xCube.getBalance(null, targetChainId, receiver, CoinType).send();
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

        AccountBalanceResponse expectedSender = makeAccountBalance(sender, "6,999,976", "6,999,976", "0", "0", "0", CoinType);
        AccountBalanceResponse actualSender = xCube.getBalance(null, targetChainId, sender, CoinType).send();
        assertEquals(expectedSender.getBalance(), actualSender.getBalance());

        AccountBalanceResponse expectedReceiver = makeAccountBalance(receiver, "4,000,020", "4,000,020", "0", "0", "0", CoinType);
        AccountBalanceResponse actualReceiver = xCube.getBalance(null, targetChainId, receiver, CoinType).send();
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

        AccountBalanceResponse expectedSender = makeAccountBalance(sender, "6,999,974", "6,999,974", "0", "0", "0", CoinType);
        AccountBalanceResponse actualSender = xCube.getBalance(null, targetChainId, sender, CoinType).send();
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

        AccountBalanceResponse expectedSender = makeAccountBalance(sender, "6,999,973", "6,999,973", "0", "0", "0", CoinType);
        AccountBalanceResponse actualSender = xCube.getBalance(null, targetChainId, sender, CoinType).send();
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

        expectedSender = makeAccountBalance(sender, "6,999,972", "6,999,972", "0", "0", "0", CoinType);
        actualSender = xCube.getBalance(null, targetChainId, sender, CoinType).send();
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

        expectedSender = makeAccountBalance(receiver, "4,000,019", "4,000,019", "0", "0", "0", CoinType);
        actualSender = xCube.getBalance(null, targetChainId, receiver, CoinType).send();
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

        expectedSender = makeAccountBalance(sender, "6,999,971", "6,999,971", "0", "0", "0", CoinType);
        actualSender = xCube.getBalance(null, targetChainId, sender, CoinType).send();
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

        AccountBalanceResponse expectedSender = makeAccountBalance(sender, "6,999,969", "6,999,969", "0", "0", "0", CoinType);
        AccountBalanceResponse actualSender = xCube.getBalance(null, targetChainId, sender, CoinType).send();
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

        expectedSender = makeAccountBalance(sender, "6,999,968", "6,999,968", "0", "0", "0", CoinType);
        actualSender = xCube.getBalance(null, targetChainId, sender, CoinType).send();
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

        expectedSender = makeAccountBalance(sender, "6,999,967", "6,999,967", "0", "0", "0", CoinType);
        actualSender = xCube.getBalance(null, targetChainId, sender, CoinType).send();
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

        expectedSender = makeAccountBalance(sender, "6,999,966", "6,999,966", "0", "0", "0", CoinType);
        actualSender = xCube.getBalance(null, targetChainId, sender, CoinType).send();
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

        expectedSender = makeAccountBalance(sender, "6,999,965", "6,999,965", "0", "0", "0", CoinType);
        actualSender = xCube.getBalance(null, targetChainId, sender, CoinType).send();
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
                .withAmount(CurrencyUtil.generateXTO(CoinType, CurrencyUtil.generateXTO(CoinType, 699000100)))
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
        AccountBalanceResponse expectedSender = makeAccountBalance(validator, "10,000,015,120,000,000,000,000,000", "2,000,000,000,000,000,000,000,000", "8,000,000,000,000,000,000,000,000", "15,120,000,000,000,000,000", "0", XTOType);
        AccountBalanceResponse actualSender = xCube.getBalance(null, targetChainId, validator, XTOType).send();
        assertEquals(expectedSender.getBalance(), actualSender.getBalance());

        //Account Bonding 확인
        AccountBondInfoResponse expectedBondingInfo = new AccountBondInfoResponse();
        expectedBondingInfo.setResult(new AccountBondInfoResponse.Result(
                CurrencyUtil.generateXTO(CoinType, 8000000),
                CurrencyUtil.generateXTO(CoinType, 0),
                CurrencyUtil.generateXTO(CoinType, 0),
                new HashMap<>()
        ));
        AccountBondInfoResponse actualBondingInfo = xCube.getBonding(null, targetChainId, validator, null).send();
        Assert.assertEquals(expectedBondingInfo.getResult(), actualBondingInfo.getResult());

        //Validator 추가 본딩
        TxRequest txRequest = makeDefaultBuilder()
                .withSender(validator)
                .withReceiver(validator)
                .withPayloadType(ApiEnum.PayloadType.BondingType)
                .withFee(CurrencyUtil.generateXTO(CoinType, 10000))
                .withAmount(CurrencyUtil.generateXTO(CoinType, 1))
                .withPayloadBody(new TxBondingBody())
                .build();
        TxSendResponse sendResponse = xCube.bonding(txRequest).send();
        assertNull(sendResponse.getError());

        //추가 본딩 후 Validator Account 잔고 확인
        ValidatorListResponse validatorListResponse = xCube.getValidatorList(null, targetChainId).send();
        expectedSender = makeAccountBalance(validator, "9,990,016,128,000,000,000,000,000", "1,989,999,000,000,000,000,000,000", "8,000,001,000,000,000,000,000,000", "16,128,000,000,000,000,000", "0", XTOType);
        actualSender = xCube.getBalance(null, targetChainId, validator, XTOType).send();
        assertEquals(expectedSender.getBalance(), actualSender.getBalance());

        //추가 본딩 후 Account Bonding 확인
        expectedBondingInfo.setResult(new AccountBondInfoResponse.Result(
                CurrencyUtil.generateXTO(CoinType, 8000001),
                CurrencyUtil.generateXTO(CoinType, 0),
                CurrencyUtil.generateXTO(CoinType, 0),
                new HashMap<>()
        ));
        actualBondingInfo = xCube.getBonding(null, targetChainId, validator, XTOType).send();
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
        AccountBalanceResponse expectedSender = makeAccountBalance(validator, "9,990,016,128,000,000,000,000,000", "1,989,999,000,000,000,000,000,000", "8,000,001,000,000,000,000,000,000", "16,128,000,000,000,000,000", "0", XTOType);
        AccountBalanceResponse actualSender = xCube.getBalance(null, targetChainId, validator, XTOType).send();
        assertEquals(expectedSender.getBalance(), actualSender.getBalance());

        //Account Bonding 확인
        AccountBondInfoResponse expectedBondingInfo = new AccountBondInfoResponse();
        expectedBondingInfo.setResult(new AccountBondInfoResponse.Result(
                CurrencyUtil.generateXTO(CoinType, 8000001),
                CurrencyUtil.generateXTO(CoinType, 0),
                CurrencyUtil.generateXTO(CoinType, 0),
                new HashMap<>()
        ));
        AccountBondInfoResponse actualBondingInfo = xCube.getBonding(null, targetChainId, validator, null).send();
        Assert.assertEquals(expectedBondingInfo.getResult(), actualBondingInfo.getResult());

        TxRequest txRequest = makeDefaultBuilder()
                .withSender(validator)
                .withReceiver(validator)
                .withPayloadType(ApiEnum.PayloadType.UnbondingType)
                .withFee(CurrencyUtil.generateXTO(CoinType, 1))
                .withAmount(CurrencyUtil.generateXTO(CoinType, 800000))
                .withPayloadBody(new TxUnbondingBody())
                .build();
        TxSendResponse sendResponse = xCube.unbonding(txRequest).send();
        assertNull(sendResponse.getError());

        expectedSender = makeAccountBalance(validator, "10,000,015,136,000,000,995,750,019", "1,989,998,000,000,000,000,000,000", "7,200,001,000,000,000,000,000,000", "9,014,522,525,200,980,550,019", "801,001,613,474,800,015,200,000", XTOType);
        actualSender = xCube.getBalance(null, targetChainId, validator, XTOType).send();
        assertEquals(expectedSender.getBalance(), actualSender.getBalance());

        //Account Bonding 확인
        expectedBondingInfo.setResult(new AccountBondInfoResponse.Result(
                CurrencyUtil.generateXTO(CoinType, 7200001),
                CurrencyUtil.generateXTO(CoinType, 0),
                CurrencyUtil.generateXTO(CoinType, 0),
                null
        ));
        actualBondingInfo = xCube.getBonding(null, targetChainId, validator, null).send();
        assertEquals(expectedBondingInfo, actualBondingInfo);
    }

    @Test
    @Order(order = 14)
    public void UnbondingTxCheckValidationOfLockBalance() throws Exception {
        //AvailableBalance + LockingBalance를 합친 Fee를 설정한 경우 사용 불가능하도록 되는지 체크
        TxRequest txRequest = makeDefaultBuilder()
                .withSender(validator)
                .withReceiver(validator)
                .withPayloadType(ApiEnum.PayloadType.CommonType)
                .withFee(CurrencyUtil.generateXTO(CoinType, 1989999))
                .withAmount(CurrencyUtil.generateXTO(CoinType, 10))
                .withPayloadBody(new TxCommonBody())
                .build();

        TxSendResponse sendResponse = xCube.sendTransaction(txRequest).send();
        assertNotNull(sendResponse.getError());
        Assert.assertEquals(201, sendResponse.getError().getCode());
    }

    /**
     * UnbondintTx - common (txCnt : 2, totalTxCnt : 17, fee : 2,000,001, total fee : 2,010,018)
     *
     * @throws Exception
     */
    @Test
    @Order(order = 15)
    public void UnbondingTxUseLockingBalance() throws Exception {
        //아래 트랜잭션이 포함되는 블록No = 18
        //테스트 진행을 위해 Locking BlockNo를 위의 unbonding  blockNo + 1로 주었다. (블록No 증가를 위해 하나의 Tx를 발생)
        TxRequest txRequest = makeDefaultBuilder()
                .withSender(validator)
                .withReceiver(validator)
                .withPayloadType(ApiEnum.PayloadType.CommonType)
                .withFee(CurrencyUtil.generateXTO(CoinType, 1))
                .withAmount(CurrencyUtil.generateXTO(CoinType, 0))
                .withPayloadBody(new TxCommonBody())
                .build();

        TxSendResponse sendResponse = xCube.sendTransaction(txRequest).send();
        assertNull(sendResponse.getError());

        AccountBalanceResponse expectedSender = makeAccountBalance(validator, "100,00,015,143,200,001,990,219,617", "2,790,998,613,474,800,015,200,000", "7,200,001,000,000,000,000,000,000", "9,015,529,725,201,975,019,617", "0", XTOType);
        AccountBalanceResponse actualSender = xCube.getBalance(null, targetChainId, validator, XTOType).send();
        assertEquals(expectedSender.getBalance(), actualSender.getBalance());

        //Locking BlockNo에 도달했을때 실제 사용이 가능해지는지 테스트
        txRequest = makeDefaultBuilder()
                .withSender(validator)
                .withReceiver(validator)
                .withPayloadType(ApiEnum.PayloadType.CommonType)
                .withFee(CurrencyUtil.generateXTO(CoinType, 2000000))
                .withAmount(CurrencyUtil.generateXTO(CoinType, 0))
                .withPayloadBody(new TxCommonBody())
                .build();
        sendResponse = xCube.sendTransaction(txRequest).send();
        assertNull(sendResponse.getError());

        expectedSender = makeAccountBalance(validator, "8,000,016,150,400,002,984,689,215", "790,998,613,474,800,015,200,000", "7,200,001,000,000,000,000,000,000", "9,016,536,925,202,969,489,215", "0", XTOType);
        actualSender = xCube.getBalance(null, targetChainId, validator, XTOType).send();
        assertEquals(expectedSender.getBalance(), actualSender.getBalance());
    }

    @Test
    @Order(order = 16)
    public void DelegatingTxCheckValidation() throws Exception {
        //DelegatingBody의 amount 값을 0보다 작게 설정한 경우.
        TxRequest txRequest = makeDefaultBuilder()
                .withSender(sender)
                .withReceiver(validator)
                .withPayloadType(ApiEnum.PayloadType.DelegatingType)
                .withFee(CurrencyUtil.generateXTO(CoinType, 1))
                .withAmount(new BigInteger("0"))
                .withPayloadBody(new TxDelegatingBody())
                .build();

        TxSendResponse sendResponse = xCube.delegating(txRequest).send();
        assertNotNull(sendResponse.getError());
        Assert.assertEquals(1, sendResponse.getError().getCode());

        //Delegating하고자 하는 Validator가 존재하지 않는 경우.
        txRequest = makeDefaultBuilder()
                .withSender(sender)
                .withReceiver(receiver)
                .withPayloadType(ApiEnum.PayloadType.DelegatingType)
                .withFee(CurrencyUtil.generateXTO(CoinType, 1))
                .withAmount(CurrencyUtil.generateXTO(CoinType, 1))
                .withPayloadBody(new TxDelegatingBody())
                .build();

        sendResponse = xCube.delegating(txRequest).send();
        assertNotNull(sendResponse.getError());
        Assert.assertEquals(401, sendResponse.getError().getCode());

        //Delegating시 Balance보다 크게 위임하고자 하는 경우.
        txRequest = makeDefaultBuilder()
                .withSender(sender)
                .withReceiver(validator)
                .withPayloadType(ApiEnum.PayloadType.DelegatingType)
                .withFee(CurrencyUtil.generateXTO(CoinType, 1))
                .withAmount(CurrencyUtil.generateXTO(CoinType, 6999965))
                .withPayloadBody(new TxDelegatingBody())
                .build();

        sendResponse = xCube.delegating(txRequest).send();
        assertNotNull(sendResponse.getError());
        Assert.assertEquals(201, sendResponse.getError().getCode());

        //ATX 단위로 위임을 하지 않은 경우.
        txRequest = makeDefaultBuilder()
                .withSender(sender)
                .withReceiver(validator)
                .withPayloadType(ApiEnum.PayloadType.DelegatingType)
                .withFee(CurrencyUtil.generateXTO(CoinType, 1))
                .withAmount(CurrencyUtil.generateXTO(XTOType, Long.parseLong("1,000,000,000,000,000,001".replaceAll(",", ""))))
                .withPayloadBody(new TxDelegatingBody())
                .build();
        sendResponse = xCube.bonding(txRequest).send();
        assertNotNull(sendResponse.getError());
        Assert.assertEquals(344, sendResponse.getError().getCode());
    }

    /**
     * DelegatingTx - delegating (txCnt : 1, totalTxCnt : 18, fee : 1, total fee : 2,010,019)
     *
     * @throws Exception
     */
    @Test
    @Order(order = 17)
    public void DelegatingTxDelegating() throws Exception {
        //Validator Bonding 확인
        AccountBondInfoResponse expectedBondingInfoOfValidator = new AccountBondInfoResponse();
        expectedBondingInfoOfValidator.setResult(new AccountBondInfoResponse.Result(
                CurrencyUtil.generateXTO(CoinType, 7200001),
                CurrencyUtil.generateXTO(CoinType, 0),
                CurrencyUtil.generateXTO(CoinType, 0),
                new HashMap<>()
        ));
        AccountBondInfoResponse actualBondingInfoOfValidator = xCube.getBonding(null, targetChainId, validator, null).send();
        assertEquals(expectedBondingInfoOfValidator.getBonding(), actualBondingInfoOfValidator.getBonding());

        //Validator Balance 확인
        AccountBalanceResponse expectedValidatorBalacne = makeAccountBalance(validator, "8,000,016,150,400,002,984,689,215", "790,998,613,474,800,015,200,000", "7,200,001,000,000,000,000,000,000", "9,016,536,925,202,969,489,215", "0", XTOType);
        AccountBalanceResponse actualValidatorBalance = xCube.getBalance(null, targetChainId, validator, XTOType).send();
        assertEquals(expectedValidatorBalacne.getBalance(), actualValidatorBalance.getBalance());

        //Delegator Balance 확인
        AccountBalanceResponse expectedDelegatorBalacne = makeAccountBalance(sender, "6,999,965,000,000,000,000,000,000", "6,999,965,000,000,000,000,000,000", "0", "0", "0", XTOType);
        AccountBalanceResponse actualDelegatorBalance = xCube.getBalance(null, targetChainId, sender, XTOType).send();
        assertEquals(expectedDelegatorBalacne.getBalance(), actualDelegatorBalance.getBalance());

        //Delegator Bonding 확인
        AccountBondInfoResponse expectedBondingInfoOfDelegator = new AccountBondInfoResponse();
        expectedBondingInfoOfDelegator.setResult(new AccountBondInfoResponse.Result(
                CurrencyUtil.generateXTO(CoinType, 0),
                CurrencyUtil.generateXTO(CoinType, 0),
                CurrencyUtil.generateXTO(CoinType, 0),
                new HashMap<>()
        ));
        AccountBondInfoResponse actualBondingInfoOfDelegator = xCube.getBonding(null, targetChainId, sender, null).send();
        assertEquals(expectedBondingInfoOfDelegator.getBonding(), actualBondingInfoOfDelegator.getBonding());

        BigInteger delegatingAmount = CurrencyUtil.generateXTO(CoinType, 99999);
        TxRequest txRequest = makeDefaultBuilder()
                .withSender(sender)
                .withReceiver(validator)
                .withPayloadType(ApiEnum.PayloadType.DelegatingType)
                .withFee(CurrencyUtil.generateXTO(CoinType, 1))
                .withAmount(delegatingAmount)
                .withPayloadBody(new TxDelegatingBody())
                .build();

        TxSendResponse sendResponse = xCube.delegating(txRequest).send();
        assertNull(sendResponse.getError());

        //Validator Bonding 확인
        expectedBondingInfoOfValidator = new AccountBondInfoResponse();
        expectedBondingInfoOfValidator.setResult(new AccountBondInfoResponse.Result(
                CurrencyUtil.generateXTO(CoinType, 7200001),
                CurrencyUtil.generateXTO(CoinType, 99999),
                CurrencyUtil.generateXTO(CoinType, 0),
                new HashMap<>()
        ));
        actualBondingInfoOfValidator = xCube.getBonding(null, targetChainId, validator, null).send();
        assertEquals(expectedBondingInfoOfValidator.getBonding(), actualBondingInfoOfValidator.getBonding());

        //Validator Balance 확인
        expectedValidatorBalacne = makeAccountBalance(validator, "10,000,016,157,600,003,980,625,437", "790,998,613,474,800,015,200,000", "7,200,001,000,000,000,000,000,000", "2,009,016,544,125,203,965,425,437", "0", XTOType);
        actualValidatorBalance = xCube.getBalance(null, targetChainId, validator, XTOType).send();
        assertEquals(expectedValidatorBalacne.getBalance(), actualValidatorBalance.getBalance());

        //Validator List에 위임한 List 정보가 있는지 확인
        ValidatorListResponse actualValidator = xCube.getValidatorList(null, targetChainId).send();
        boolean checked = false;
        for (ValidatorListResponse.Result result : actualValidator.getResult()) {
            if (result.getValidatorAccountAddr().equals(validator)) {
                ValidatorListResponse.Result.Delegator delegator = result.getDelegatorMap().get(sender);
                if (delegator != null && delegator.getTotalBondingBalance().compareTo(delegatingAmount) == 0) {
                    checked = true;
                }
            }
        }
        assertEquals(checked, true);

        //Delegator Balance 확인
        expectedDelegatorBalacne = makeAccountBalance(sender, "6,999,964,000,000,000,000,000,000", "6,899,965,000,000,000,000,000,000", "99,999,000,000,000,000,000,000", "0", "0", XTOType);
        actualDelegatorBalance = xCube.getBalance(null, targetChainId, sender, XTOType).send();
        assertEquals(expectedDelegatorBalacne.getBalance(), actualDelegatorBalance.getBalance());

        //Delegator Bonding 확인
        expectedBondingInfoOfDelegator = new AccountBondInfoResponse();
        Map<String, BigInteger> delegatingHistory = new HashMap<>();
        delegatingHistory.put(validator, CurrencyUtil.generateXTO(CoinType, 99999));
        expectedBondingInfoOfDelegator.setResult(new AccountBondInfoResponse.Result(
                CurrencyUtil.generateXTO(CoinType, 0),
                CurrencyUtil.generateXTO(CoinType, 0),
                CurrencyUtil.generateXTO(CoinType, 99999),
                delegatingHistory
        ));
        actualBondingInfoOfDelegator = xCube.getBonding(null, targetChainId, sender, null).send();
        assertEquals(expectedBondingInfoOfDelegator.getBonding(), actualBondingInfoOfDelegator.getBonding());
    }

    /**
     * DelegatingTx - delegating (txCnt : 1, totalTxCnt : 19, fee : 1, total fee : 2,010,020)
     *
     * @throws Exception
     */
    @Test
    @Order(order = 18)
    public void DelegatingTxDelegatingToSelf() throws Exception {
        TxRequest txRequest = makeDefaultBuilder()
                .withSender(validator)
                .withReceiver(validator)
                .withPayloadType(ApiEnum.PayloadType.DelegatingType)
                .withFee(CurrencyUtil.generateXTO(CoinType, 1))
                .withAmount(CurrencyUtil.generateXTO(CoinType, 1))
                .withPayloadBody(new TxDelegatingBody())
                .build();

        TxSendResponse sendResponse = xCube.delegating(txRequest).send();
        assertNull(sendResponse.getError());

        //Validator Bonding 확인
        AccountBondInfoResponse expectedBondingInfoOfValidator = new AccountBondInfoResponse();
        expectedBondingInfoOfValidator.setResult(new AccountBondInfoResponse.Result(
                CurrencyUtil.generateXTO(CoinType, 7200002),
                CurrencyUtil.generateXTO(CoinType, 99999),
                CurrencyUtil.generateXTO(CoinType, 0),
                new HashMap<>()
        ));
        AccountBondInfoResponse actualBondingInfoOfValidator = xCube.getBonding(null, targetChainId, validator, null).send();
        assertEquals(expectedBondingInfoOfValidator.getBonding(), actualBondingInfoOfValidator.getBonding());

        //여기서는 XTO 단위로 셋팅을 하였는데 이유는 Validator List의 Reward 값을 계산할때 XTO 단위로 더한후 CoinType으로 변환하기 때문에 validator.reward + delegator.reward 역시 XTO 단위로 더한후 ConinType으로 변환한다.

        //Validator Balance 확인
        AccountBalanceResponse expectedValidatorBalacne = makeAccountBalance(validator, "10,000,016,151,101,511,823,726,806", "790,996,613,474,800,015,200,000", "7,200,002,000,000,000,000,000,000", "2,009,017,537,626,711,808,526,806", "0", XTOType);
        AccountBalanceResponse actualValidatorBalance = xCube.getBalance(null, targetChainId, validator, XTOType).send();
        assertEquals(expectedValidatorBalacne.getBalance(), actualValidatorBalance.getBalance());

        //Delegator Balance 확인
        AccountBalanceResponse expectedDelegatorBalacne = makeAccountBalance(sender, "6,999,964,000,099,999,000,000,000", "6,899,965,000,000,000,000,000,000", "99,999,000,000,000,000,000,000", "99,999,000,000,000", "0", XTOType);
        AccountBalanceResponse actualDelegatorBalance = xCube.getBalance(null, targetChainId, sender, XTOType).send();
        assertEquals(expectedDelegatorBalacne.getBalance(), actualDelegatorBalance.getBalance());
    }

    @Test
    @Order(order = 19)
    public void UndelegatingTxCheckValidation() throws Exception {
        //UndelegatingBody의 amount 값을 0보다 작게 설정한 경우.
        TxRequest txRequest = makeDefaultBuilder()
                .withSender(sender)
                .withReceiver(validator)
                .withPayloadType(ApiEnum.PayloadType.UndelegatingType)
                .withFee(CurrencyUtil.generateXTO(CoinType, 1))
                .withAmount(CurrencyUtil.generateXTO(CoinType, 0))
                .withPayloadBody(new TxUndelegatingBody())
                .build();

        TxSendResponse sendResponse = xCube.undelegating(txRequest).send();
        assertNotNull(sendResponse.getError());
        Assert.assertEquals(1, sendResponse.getError().getCode());

        //Undelegating하고자 하는 Validator가 존재하지 않는 경우.
        txRequest = makeDefaultBuilder()
                .withSender(sender)
                .withReceiver(receiver)
                .withPayloadType(ApiEnum.PayloadType.UndelegatingType)
                .withFee(CurrencyUtil.generateXTO(CoinType, 1))
                .withAmount(CurrencyUtil.generateXTO(CoinType, 10))
                .withPayloadBody(new TxUndelegatingBody())
                .build();

        sendResponse = xCube.undelegating(txRequest).send();
        assertNotNull(sendResponse.getError());
        Assert.assertEquals(401, sendResponse.getError().getCode());

        //Undelegating하고자 하는 Validator에 Delegating 이력이 없는 경우.
        txRequest = makeDefaultBuilder()
                .withSender(receiver)
                .withReceiver(validator)
                .withPayloadType(ApiEnum.PayloadType.UndelegatingType)
                .withFee(CurrencyUtil.generateXTO(CoinType, 1))
                .withAmount(CurrencyUtil.generateXTO(CoinType, 10))
                .withPayloadBody(new TxUndelegatingBody())
                .build();

        sendResponse = xCube.undelegating(txRequest).send();
        assertNotNull(sendResponse.getError());
        Assert.assertEquals(402, sendResponse.getError().getCode());

        //Undelegating시 Delegating한 지분 값보다 크게 설정한 경우.
        AccountBondInfoResponse bondInfoResponse = xCube.getBonding(null, targetChainId, sender, null).send();
        BigInteger delegatingAmount = CurrencyUtil.generateCurrencyUnitToCurrencyUnit(CurrencyUtil.CurrencyType.XTOType, CurrencyUtil.CurrencyType.CoinType, bondInfoResponse.getBonding().getDelegating());
        delegatingAmount = delegatingAmount.add(new BigInteger("1"));
        txRequest = makeDefaultBuilder()
                .withSender(sender)
                .withReceiver(validator)
                .withPayloadType(ApiEnum.PayloadType.UndelegatingType)
                .withFee(CurrencyUtil.generateXTO(CoinType, 1))
                .withAmount(CurrencyUtil.generateXTO(CoinType, delegatingAmount))
                .withPayloadBody(new TxUndelegatingBody())
                .build();

        sendResponse = xCube.undelegating(txRequest).send();
        assertNotNull(sendResponse.getError());
        Assert.assertEquals(403, sendResponse.getError().getCode());

        //ATX 단위로 위임 취소를 하지 않은 경우.
        txRequest = makeDefaultBuilder()
                .withSender(sender)
                .withReceiver(validator)
                .withPayloadType(ApiEnum.PayloadType.UndelegatingType)
                .withFee(CurrencyUtil.generateXTO(CoinType, 1))
                .withAmount(CurrencyUtil.generateXTO(XTOType, Long.parseLong("1,000,000,000,000,000,001".replaceAll(",", ""))))
                .withPayloadBody(new TxUndelegatingBody())
                .build();
        sendResponse = xCube.bonding(txRequest).send();
        assertNotNull(sendResponse.getError());
        Assert.assertEquals(344, sendResponse.getError().getCode());
    }

    /**
     * UndelegatingTx - delegating (txCnt : 1, totalTxCnt : 20, fee : 1, total fee : 2,010,021)
     *
     * @throws Exception
     */
    @Test
    @Order(order = 20)
    public void UndelegatingTxUndelegating() throws Exception {
        //Delegator Balance 확인
        AccountBalanceResponse expectedDelegatorBalacne = makeAccountBalance(sender, "6,999,964,000,099,999,000,000,000", "6,899,965,000,000,000,000,000,000", "99,999,000,000,000,000,000,000", "99,999,000,000,000", "0", XTOType);
        AccountBalanceResponse actualDelegatorBalance = xCube.getBalance(null, targetChainId, sender, XTOType).send();
        assertEquals(expectedDelegatorBalacne.getBalance(), actualDelegatorBalance.getBalance());

        //Validator Balance 확인
        AccountBalanceResponse expectedVaidatorBalacne = makeAccountBalance(validator, "10,000,016,151,101,511,823,726,806", "790,996,613,474,800,015,200,000", "7,200,002,000,000,000,000,000,000", "2,009,017,537,626,711,808,526,806", "0", XTOType);
        AccountBalanceResponse actualValidatorBalance = xCube.getBalance(null, targetChainId, validator, XTOType).send();
        assertEquals(expectedVaidatorBalacne.getBalance(), actualValidatorBalance.getBalance());

        //Validator List 확인
        ValidatorListResponse actualValidator = xCube.getValidatorList(null, targetChainId).send();

        TxRequest txRequest = makeDefaultBuilder()
                .withSender(sender)
                .withReceiver(validator)
                .withPayloadType(ApiEnum.PayloadType.UndelegatingType)
                .withFee(CurrencyUtil.generateXTO(CoinType, 1))
                .withAmount(CurrencyUtil.generateXTO(CoinType, 19999))
                .withPayloadBody(new TxUndelegatingBody())
                .build();

        TxSendResponse sendResponse = xCube.undelegating(txRequest).send();
        assertNull(sendResponse.getError());

        //Delegator Bonding 확인
        AccountBondInfoResponse expectedDelegatorBondingInfo = new AccountBondInfoResponse();
        Map<String, BigInteger> delegatingHistory = new HashMap<>();
        delegatingHistory.put(validator, CurrencyUtil.generateXTO(CoinType, 80000));
        expectedDelegatorBondingInfo.setResult(new AccountBondInfoResponse.Result(
                CurrencyUtil.generateXTO(CoinType, 0),
                CurrencyUtil.generateXTO(CoinType, 0),
                CurrencyUtil.generateXTO(CoinType, 80000),
                delegatingHistory
        ));
        AccountBondInfoResponse delegatorBondInfoResponse = xCube.getBonding(null, targetChainId, sender, null).send();
        assertEquals(expectedDelegatorBondingInfo.getBonding(), delegatorBondInfoResponse.getBonding());

        //Delegator Balance 확인
        expectedDelegatorBalacne = makeAccountBalance(sender, "6,999,963,000,359,998,000,000,000", "6,899,964,000,000,000,000,000,000", "80,000,000,000,000,000,000,000", "160,000,000,000,000", "19,999,000,199,998,000,000,000", XTOType);
        actualDelegatorBalance = xCube.getBalance(null, targetChainId, sender, XTOType).send();
        assertEquals(expectedDelegatorBalacne.getBalance(), actualDelegatorBalance.getBalance());

        //Validator Balance 확인
        expectedVaidatorBalacne = makeAccountBalance(validator, "10,000,017,144,603,022,545,092,014", "790,996,613,474,800,015,200,000", "7,200,002,000,000,000,000,000,000", "2,009,018,531,128,222,529,892,014", "0", XTOType);
        actualValidatorBalance = xCube.getBalance(null, targetChainId, validator, XTOType).send();
        assertEquals(expectedVaidatorBalacne.getBalance(), actualValidatorBalance.getBalance());
    }

    /**
     * UndelegatingTx - delegating (txCnt : 1, totalTxCnt : 21, fee : 1, total fee : 2,010,022)
     *
     * @throws Exception
     */
    @Test
    @Order(order = 21)
    public void UndelegatingTxUndelegatingOfValidator() throws Exception {
        //Validator Balance 확인
        AccountBalanceResponse expectedVaidatorBalacne = makeAccountBalance(validator, "10,000,017,144,603,022,545,092,014", "790,996,613,474,800,015,200,000", "7,200,002,000,000,000,000,000,000", "2,009,018,531,128,222,529,892,014", "0", XTOType);
        AccountBalanceResponse actualValidatorBalance = xCube.getBalance(null, targetChainId, validator, XTOType).send();
        assertEquals(expectedVaidatorBalacne.getBalance(), actualValidatorBalance.getBalance());

        TxRequest txRequest = makeDefaultBuilder()
                .withSender(validator)
                .withReceiver(validator)
                .withPayloadType(ApiEnum.PayloadType.UndelegatingType)
                .withFee(CurrencyUtil.generateXTO(CoinType, 1))
                .withAmount(CurrencyUtil.generateXTO(CoinType, 10000))
                .withPayloadBody(new TxUndelegatingBody())
                .build();

        TxSendResponse sendResponse = xCube.undelegating(txRequest).send();
        assertNull(sendResponse.getError());

        //Validator Bonding 확인
        AccountBondInfoResponse expectedValidatorBondingInfo = new AccountBondInfoResponse();
        Map<String, BigInteger> delegatingHistory = new HashMap<>();
        expectedValidatorBondingInfo.setResult(new AccountBondInfoResponse.Result(
                CurrencyUtil.generateXTO(CoinType, 7190002),
                CurrencyUtil.generateXTO(CoinType, 80000),
                CurrencyUtil.generateXTO(CoinType, 0),
                delegatingHistory
        ));
        AccountBondInfoResponse validatorBondInfoResponse = xCube.getBonding(null, targetChainId, validator, null).send();
        assertEquals(expectedValidatorBondingInfo.getBonding(), validatorBondInfoResponse.getBonding());

        //Delegator Balance 확인
        AccountBalanceResponse expectedDelegatorBalacne = makeAccountBalance(sender, "6,999,963,000,439,998,000,000,000", "6,919,963,000,199,998,000,000,000", "80,000,000,000,000,000,000,000", "240,000,000,000,000", "0", XTOType);
        AccountBalanceResponse actualDelegatorBalance = xCube.getBalance(null, targetChainId, sender, XTOType).send();
        assertEquals(expectedDelegatorBalacne.getBalance(), actualDelegatorBalance.getBalance());

        //Validator Balance 확인
        expectedVaidatorBalacne = makeAccountBalance(validator, "10,000,017,140,814,016,570,291,264", "790,995,613,474,800,015,200,000", "7,190,002,000,000,000,000,000,000", "2,006,229,223,215,218,737,381,264", "12,790,304,123,997,817,710,000", XTOType);
        actualValidatorBalance = xCube.getBalance(null, targetChainId, validator, XTOType).send();
        assertEquals(expectedVaidatorBalacne.getBalance(), actualValidatorBalance.getBalance());
    }

    /**
     * GRProposalTx - 유효성 체크
     *
     * @throws Exception
     */
    @Test
    @Order(order = 22)
    public void GRProposalTxCheckValidation() throws Exception {
        TxGRProposalBody.Builder grpBuilder = TxGRProposalBody.builder();

        //Tx Fee를 100ATX 이하로 설정한 경우.
        TxRequest txRequest = makeDefaultBuilder()
                .withSender(validator)
                .withReceiver(validator)
                .withPayloadType(ApiEnum.PayloadType.GRProposalType)
                .withFee(CurrencyUtil.generateXTO(CoinType, 1))
                .withAmount(CurrencyUtil.generateXTO(CoinType, 0))
                .withPayloadBody(grpBuilder.build())
                .build();

        TxSendResponse sendResponse = xCube.sendTransaction(txRequest).send();
        assertNotNull(sendResponse.getError());
        Assert.assertEquals(1, sendResponse.getError().getCode());

        //Tx의 Amount를 0보다 크게 설정한 경우.
        txRequest = makeDefaultBuilder()
                .withSender(validator)
                .withReceiver(validator)
                .withPayloadType(ApiEnum.PayloadType.GRProposalType)
                .withFee(CurrencyUtil.generateXTO(CoinType, 100))
                .withAmount(CurrencyUtil.generateXTO(CoinType, 1))
                .withPayloadBody(grpBuilder.withCurrentReflection(new TxGRProposalBody.CurrentReflection(1, 5)).build())
                .build();

        sendResponse = xCube.sendTransaction(txRequest).send();
        assertNotNull(sendResponse.getError());
        Assert.assertEquals(307, sendResponse.getError().getCode());

        //Sender와 Receiver가 다른 경우.
        txRequest = makeDefaultBuilder()
                .withSender(validator)
                .withReceiver(receiver)
                .withPayloadType(ApiEnum.PayloadType.GRProposalType)
                .withFee(CurrencyUtil.generateXTO(CoinType, 100))
                .withAmount(CurrencyUtil.generateXTO(CoinType, 0))
                .withPayloadBody(grpBuilder.build())
                .build();

        sendResponse = xCube.sendTransaction(txRequest).send();
        assertNotNull(sendResponse.getError());
        Assert.assertEquals(309, sendResponse.getError().getCode());

        //GR 제안시 투표가능한 기간(블록No)이 0보다 작은 경우.
        txRequest = makeDefaultBuilder()
                .withSender(validator)
                .withReceiver(validator)
                .withPayloadType(ApiEnum.PayloadType.GRProposalType)
                .withFee(CurrencyUtil.generateXTO(CoinType, 100))
                .withAmount(CurrencyUtil.generateXTO(CoinType, 0))
                .withPayloadBody(grpBuilder.withCurrentReflection(new TxGRProposalBody.CurrentReflection(-1, 0)).build())
                .build();

        sendResponse = xCube.sendTransaction(txRequest).send();
        assertNotNull(sendResponse.getError());
        Assert.assertEquals(1, sendResponse.getError().getCode());

        //GR이 가결된 경우 적용해야 할 블록No 값이 0보다 작은 경우.
        txRequest = makeDefaultBuilder()
                .withSender(validator)
                .withReceiver(validator)
                .withPayloadType(ApiEnum.PayloadType.GRProposalType)
                .withFee(CurrencyUtil.generateXTO(CoinType, 100))
                .withAmount(CurrencyUtil.generateXTO(CoinType, 0))
                .withPayloadBody(grpBuilder.withCurrentReflection(new TxGRProposalBody.CurrentReflection(0, -1)).build())
                .build();

        sendResponse = xCube.sendTransaction(txRequest).send();
        assertNotNull(sendResponse.getError());
        Assert.assertEquals(1, sendResponse.getError().getCode());

        //Validator와 Delegator의 보상비율을 지정한 경우 합이 100이 안되는 경우.
        txRequest = makeDefaultBuilder()
                .withSender(validator)
                .withReceiver(validator)
                .withPayloadType(ApiEnum.PayloadType.GRProposalType)
                .withFee(CurrencyUtil.generateXTO(CoinType, 100))
                .withAmount(CurrencyUtil.generateXTO(CoinType, 0))
                .withPayloadBody(
                        grpBuilder
                                .withCurrentReflection(new TxGRProposalBody.CurrentReflection(0, 1))
                                .build()
                )
                .build();

        sendResponse = xCube.sendTransaction(txRequest).send();
        assertNotNull(sendResponse.getError());
        Assert.assertEquals(1, sendResponse.getError().getCode());

        //Int64로 정의된 필드들의 값이 Int32의 Max 값보다 큰경우. (2147483647)
        txRequest = makeDefaultBuilder()
                .withSender(validator)
                .withReceiver(validator)
                .withPayloadType(ApiEnum.PayloadType.GRProposalType)
                .withFee(CurrencyUtil.generateXTO(CoinType, 100))
                .withAmount(CurrencyUtil.generateXTO(CoinType, 0))
                .withPayloadBody(
                        grpBuilder
                                .withMinBlockNumsToGRProposal(2147483648l)
                                .withCurrentReflection(new TxGRProposalBody.CurrentReflection(0, 1))
                                .build()
                )
                .build();

        sendResponse = xCube.sendTransaction(txRequest).send();
        assertNotNull(sendResponse.getError());
        Assert.assertEquals(1, sendResponse.getError().getCode());

        //현재 적용중인 GR의 최대 가능한 투표가능 기간 값보다 현재 제안 하려는 GR에 적용하는 투표가능 기간이 큰 경우.
        txRequest = makeDefaultBuilder()
                .withSender(validator)
                .withReceiver(validator)
                .withPayloadType(ApiEnum.PayloadType.GRProposalType)
                .withFee(CurrencyUtil.generateXTO(CoinType, 100))
                .withAmount(CurrencyUtil.generateXTO(CoinType, 0))
                .withPayloadBody(
                        TxGRProposalBody.builder()
                                .withCurrentReflection(new TxGRProposalBody.CurrentReflection(1728001, 1))
                                .build()
                )
                .build();

        sendResponse = xCube.sendTransaction(txRequest).send();
        assertNotNull(sendResponse.getError());
        Assert.assertEquals(1, sendResponse.getError().getCode());

        //제안 하려는 GR이 가결된 후 적용되어야 하는 값이 현재 적용중인 GR의 가결된 후 GR을 적용하고자 하는 최소 및 최대 적용 값 사이로 설정되지 않은 경우.
        txRequest = makeDefaultBuilder()
                .withSender(validator)
                .withReceiver(validator)
                .withPayloadType(ApiEnum.PayloadType.GRProposalType)
                .withFee(CurrencyUtil.generateXTO(CoinType, 100))
                .withAmount(CurrencyUtil.generateXTO(CoinType, 0))
                .withPayloadBody(
                        TxGRProposalBody.builder()
                                .withCurrentReflection(new TxGRProposalBody.CurrentReflection(1728000, 1))
                                .build()
                )
                .build();

        sendResponse = xCube.sendTransaction(txRequest).send();
        assertNotNull(sendResponse.getError());
        Assert.assertEquals(1, sendResponse.getError().getCode());

        //제안자가 Validator가 아닌 경우.
        txRequest = makeDefaultBuilder()
                .withSender(sender)
                .withReceiver(sender)
                .withPayloadType(ApiEnum.PayloadType.GRProposalType)
                .withFee(CurrencyUtil.generateXTO(CoinType, 100))
                .withAmount(CurrencyUtil.generateXTO(CoinType, 0))
                .withPayloadBody(
                        TxGRProposalBody.builder()
                                .withCurrentReflection(new TxGRProposalBody.CurrentReflection(2, 3))
                                .build()
                )
                .build();

        sendResponse = xCube.sendTransaction(txRequest).send();
        assertNotNull(sendResponse.getError());
        Assert.assertEquals(401, sendResponse.getError().getCode());

        //GR 적용 최대가능 기간이 투표가능 최대기간 보다 작거나 같은 경우.
        txRequest = makeDefaultBuilder()
                .withSender(sender)
                .withReceiver(sender)
                .withPayloadType(ApiEnum.PayloadType.GRProposalType)
                .withFee(CurrencyUtil.generateXTO(CoinType, 100))
                .withAmount(CurrencyUtil.generateXTO(CoinType, 0))
                .withPayloadBody(
                        TxGRProposalBody.builder()
                                .withMaxBlockNumsForVoting(5)
                                .withMaxBlockNumsUtilReflection(5)
                                .withCurrentReflection(new TxGRProposalBody.CurrentReflection(2, 3))
                                .build()
                )
                .build();

        sendResponse = xCube.sendTransaction(txRequest).send();
        assertNotNull(sendResponse.getError());
        Assert.assertEquals(1, sendResponse.getError().getCode());

        //제안자(Validator)가 제안조건(제안을 하기 위한 연속된 블록합의 수)을 충족하지 못한경우.
        txRequest = makeDefaultBuilder()
                .withSender(validator)
                .withReceiver(validator)
                .withPayloadType(ApiEnum.PayloadType.GRProposalType)
                .withFee(CurrencyUtil.generateXTO(CoinType, 100))
                .withAmount(CurrencyUtil.generateXTO(CoinType, 0))
                .withPayloadBody(
                        TxGRProposalBody.builder()
                                .withCurrentReflection(new TxGRProposalBody.CurrentReflection(2, 3))
                                .build()
                )
                .build();

        sendResponse = xCube.sendTransaction(txRequest).send();
        assertNotNull(sendResponse.getError());
        Assert.assertEquals(319, sendResponse.getError().getCode());
    }

    /**
     * GRProposalTx - grproposal (txCnt : 2, totalTxCnt : 23, fee : 101, total fee : 2,010,123)
     *
     * @throws Exception
     */
    @Test
    @Order(order = 23)
    public void GRProposalTxGRProposal() throws Exception {
        //GR 제안을 할 수 있는 제안블록 수 충족을 위한 블록 증가.
        AccountBalanceResponse expectedReceiver = makeAccountBalance(receiver, "4,000,019", "4,000,019", "0", "0", "0", CoinType);
        AccountBalanceResponse actualReceiver = xCube.getBalance(null, targetChainId, receiver, CurrencyUtil.CurrencyType.CoinType).send();
        assertEquals(expectedReceiver.getBalance(), actualReceiver.getBalance());

        TxRequest txRequest = makeDefaultBuilder()
                .withSender(receiver)
                .withReceiver(receiver)
                .withPayloadType(ApiEnum.PayloadType.CommonType)
                .withFee(CurrencyUtil.generateXTO(CoinType, 1))
                .withAmount(CurrencyUtil.generateXTO(CoinType, 0))
                .withPayloadBody(new TxCommonBody())
                .build();

        TxSendResponse txSendResponse = xCube.sendTransaction(txRequest).send();
        assertNull(txSendResponse.getError());
        assertNotNull(txSendResponse.getResult());

        expectedReceiver = makeAccountBalance(receiver, "4,000,018", "4,000,018", "0", "0", "0", CoinType);
        actualReceiver = xCube.getBalance(null, targetChainId, receiver, CoinType).send();
        assertEquals(expectedReceiver.getBalance(), actualReceiver.getBalance());

        //투표 기간 값보다 가결시 적용해야 할 기간이 더 작은 경우.
        txRequest = makeDefaultBuilder()
                .withSender(validator)
                .withReceiver(validator)
                .withPayloadType(ApiEnum.PayloadType.GRProposalType)
                .withFee(CurrencyUtil.generateXTO(CoinType, 100))
                .withAmount(CurrencyUtil.generateXTO(CoinType, 0))
                .withPayloadBody(
                        TxGRProposalBody.builder()
                                .withCurrentReflection(new TxGRProposalBody.CurrentReflection(4, 3))
                                .build()
                )
                .build();

        txSendResponse = xCube.sendTransaction(txRequest).send();
        assertNotNull(txSendResponse.getError());
        Assert.assertEquals(1, txSendResponse.getError().getCode());

        //GR 제안 - 전체 필드변경.
        Map<String, BigInteger> stakeMap = new HashMap<>();
        stakeMap.put(validator, CurrencyUtil.generateXTO(CurrencyUtil.CurrencyType.CoinType, 7270002));


        ProgressGovernance.Result expectedGR = new ProgressGovernance.Result();
        expectedGR.setExpectedGRVersion(2);
        expectedGR.setStake(stakeMap);
        expectedGR.setAgreeRate(0);
        expectedGR.setDisagreeRate(0);
        expectedGR.setPass(false);
        expectedGR.setCurrentBlockNo(startCurrentBlockNo++);
        expectedGR.setEndOfVotingBlockNo(startEndOfVotingBlockNo++);
        expectedGR.setReflectionBlockNo(startReflectionBlockNo++);

        expectedGR.setRewardXtoPerCoin(CurrencyUtil.generateXTO(CurrencyUtil.CurrencyType.CoinType, 100));
        expectedGR.setMinCommonTxFee(CurrencyUtil.generateXTO(CurrencyUtil.CurrencyType.CoinType, 9));
        expectedGR.setMinBondingTxFee(CurrencyUtil.generateXTO(CurrencyUtil.CurrencyType.CoinType, 9999));
        expectedGR.setMinGRProposalTxFee(CurrencyUtil.generateXTO(CurrencyUtil.CurrencyType.CoinType, 99));
        expectedGR.setMinGRVoteTxFee(CurrencyUtil.generateXTO(CurrencyUtil.CurrencyType.CoinType, 1));
        expectedGR.setMinXTxFee(CurrencyUtil.generateXTO(CurrencyUtil.CurrencyType.CoinType, 999));
        expectedGR.setMaxBlockNumsForVoting(49);
        expectedGR.setMinBlockNumsToGRProposal(20);
        expectedGR.setMinBlockNumsUtilReflection(1);
        expectedGR.setMaxBlockNumsUtilReflection(50);
        expectedGR.setBlockNumsFreezingValidator(200);
        expectedGR.setBlockNumsUtilUnbonded(10);
        expectedGR.setMaxDelegatableValidatorNums(30);
        expectedGR.setValidatorNums(30);
        expectedGR.setFirstCompatibleVersion("1.0.1-stable");
        expectedGR.setCurrentReflection(new TxGRProposalBody.CurrentReflection(2, 3));

        txRequest = makeDefaultBuilder()
                .withSender(validator)
                .withReceiver(validator)
                .withPayloadType(ApiEnum.PayloadType.GRProposalType)
                .withFee(CurrencyUtil.generateXTO(CoinType, 100))
                .withAmount(CurrencyUtil.generateXTO(CoinType, 0))
                .withPayloadBody(
                        TxGRProposalBody.builder()
                                .withRewardXtoPerCoin(expectedGR.getRewardXtoPerCoin())
                                .withMinCommonTxFee(expectedGR.getMinCommonTxFee())
                                .withMinBondingTxFee(expectedGR.getMinBondingTxFee())
                                .withMinGRProposalTxFee(expectedGR.getMinGRProposalTxFee())
                                .withMinGRVoteTxFee(expectedGR.getMinGRVoteTxFee())
                                .withMinXTxFee(expectedGR.getMinXTxFee())
                                .withMaxBlockNumsForVoting(expectedGR.getMaxBlockNumsForVoting())
                                .withMinBlockNumsToGRProposal(expectedGR.getMinBlockNumsToGRProposal())
                                .withMinBlockNumsUtilReflection(expectedGR.getMinBlockNumsUtilReflection())
                                .withMaxBlockNumsUtilReflection(expectedGR.getMaxBlockNumsUtilReflection())
                                .withBlockNumsFreezingValidator(expectedGR.getBlockNumsFreezingValidator())
                                .withBlockNumsUtilUnbonded(expectedGR.getBlockNumsUtilUnbonded())
                                .withMaxDelegatableValidatorNums(expectedGR.getMaxDelegatableValidatorNums())
                                .withValidatorNums(expectedGR.getValidatorNums())
                                .withFirstCompatibleVersion(expectedGR.getFirstCompatibleVersion())
                                .withCurrentReflection(expectedGR.getCurrentReflection())
                                .build()
                )
                .build();

        txSendResponse = xCube.sendTransaction(txRequest).send();
        assertNull(txSendResponse.getError());

        ValidatorListResponse validatorListResponse = xCube.getValidatorList(null, targetChainId).send();
        ProgressGovernance actualGR = xCube.getProgressGovernance(null, targetChainId).send();
        assertEquals(expectedGR, actualGR.getGR());
        Assert.assertEquals(validatorListResponse.getResult().get(0).getTotalBondingBalance(), actualGR.getGR().getStake().get(validator));

        //다음 GR을 테스트하기 위해 삭제
        BoolResponse boolResponse = xCube.removeNewGR(null, targetChainId).send();
        assertNull(boolResponse.getError());
    }

    /**
     * GRProposalTx - grproposal overriding (txCnt : 16, totalTxCnt : 39, fee : 1,600, total fee : 2,011,723)
     *
     * @throws Exception
     */
    @Test
    @Order(order = 24)
    public void GRProposalTxGRProposalOverriding() throws Exception {
        //RewardAmount
        ProgressGovernance.Result expectedGR = makeProgressGR();
        expectedGR.setRewardXtoPerCoin(CurrencyUtil.generateXTO(CurrencyUtil.CurrencyType.CoinType, 500));
        expectedGR.setCurrentBlockNo(startCurrentBlockNo++);
        expectedGR.setEndOfVotingBlockNo(startEndOfVotingBlockNo++);
        expectedGR.setReflectionBlockNo(startReflectionBlockNo++);

        TxRequest txRequest = makeDefaultBuilder()
                .withSender(validator)
                .withReceiver(validator)
                .withPayloadType(ApiEnum.PayloadType.GRProposalType)
                .withFee(CurrencyUtil.generateXTO(CoinType, 100))
                .withAmount(CurrencyUtil.generateXTO(CoinType, 0))
                .withPayloadBody(
                        TxGRProposalBody.builder()
                                .withRewardXtoPerCoin(expectedGR.getRewardXtoPerCoin())
                                .withCurrentReflection(expectedGR.getCurrentReflection())
                                .build()
                )
                .build();

        TxSendResponse txSendResponse = xCube.sendTransaction(txRequest).send();
        assertNull(txSendResponse.getError());

        ValidatorListResponse validatorListResponse = xCube.getValidatorList(null, targetChainId).send();
        ProgressGovernance actualGR = xCube.getProgressGovernance(null, targetChainId).send();
        assertEquals(expectedGR, actualGR.getGR());
        Assert.assertEquals(validatorListResponse.getResult().get(0).getTotalBondingBalance(), actualGR.getGR().getStake().get(validator));

        BoolResponse boolResponse = xCube.removeNewGR(null, targetChainId).send();
        assertNull(boolResponse.getError());

        //ValidatorRewardRate, DelegatorRewardRate
        expectedGR = makeProgressGR();
        expectedGR.setCurrentBlockNo(startCurrentBlockNo++);
        expectedGR.setEndOfVotingBlockNo(startEndOfVotingBlockNo++);
        expectedGR.setReflectionBlockNo(startReflectionBlockNo++);

        txRequest = makeDefaultBuilder()
                .withSender(validator)
                .withReceiver(validator)
                .withPayloadType(ApiEnum.PayloadType.GRProposalType)
                .withFee(CurrencyUtil.generateXTO(CoinType, 100))
                .withAmount(CurrencyUtil.generateXTO(CoinType, 0))
                .withPayloadBody(
                        TxGRProposalBody.builder()
                                .withCurrentReflection(expectedGR.getCurrentReflection())
                                .build()
                )
                .build();

        txSendResponse = xCube.sendTransaction(txRequest).send();
        assertNull(txSendResponse.getError());

        validatorListResponse = xCube.getValidatorList(null, targetChainId).send();
        actualGR = xCube.getProgressGovernance(null, targetChainId).send();
        assertEquals(expectedGR, actualGR.getGR());
        Assert.assertEquals(validatorListResponse.getResult().get(0).getTotalBondingBalance(), actualGR.getGR().getStake().get(validator));

        boolResponse = xCube.removeNewGR(null, targetChainId).send();
        assertNull(boolResponse.getError());

        //MinCommonTxFee
        expectedGR = makeProgressGR();
        expectedGR.setMinCommonTxFee(CurrencyUtil.generateXTO(CurrencyUtil.CurrencyType.CoinType, 2));
        expectedGR.setCurrentBlockNo(startCurrentBlockNo++);
        expectedGR.setEndOfVotingBlockNo(startEndOfVotingBlockNo++);
        expectedGR.setReflectionBlockNo(startReflectionBlockNo++);

        txRequest = makeDefaultBuilder()
                .withSender(validator)
                .withReceiver(validator)
                .withPayloadType(ApiEnum.PayloadType.GRProposalType)
                .withFee(CurrencyUtil.generateXTO(CoinType, 100))
                .withAmount(CurrencyUtil.generateXTO(CoinType, 0))
                .withPayloadBody(
                        TxGRProposalBody.builder()
                                .withMinCommonTxFee(expectedGR.getMinCommonTxFee())
                                .withCurrentReflection(expectedGR.getCurrentReflection())
                                .build()
                )
                .build();

        txSendResponse = xCube.sendTransaction(txRequest).send();
        assertNull(txSendResponse.getError());

        validatorListResponse = xCube.getValidatorList(null, targetChainId).send();
        actualGR = xCube.getProgressGovernance(null, targetChainId).send();
        assertEquals(expectedGR, actualGR.getGR());
        Assert.assertEquals(validatorListResponse.getResult().get(0).getTotalBondingBalance(), actualGR.getGR().getStake().get(validator));

        boolResponse = xCube.removeNewGR(null, targetChainId).send();
        assertNull(boolResponse.getError());

        //MinBondingTxFee
        expectedGR = makeProgressGR();
        expectedGR.setMinBondingTxFee(CurrencyUtil.generateXTO(CurrencyUtil.CurrencyType.CoinType, 3));
        expectedGR.setCurrentBlockNo(startCurrentBlockNo++);
        expectedGR.setEndOfVotingBlockNo(startEndOfVotingBlockNo++);
        expectedGR.setReflectionBlockNo(startReflectionBlockNo++);

        txRequest = makeDefaultBuilder()
                .withSender(validator)
                .withReceiver(validator)
                .withPayloadType(ApiEnum.PayloadType.GRProposalType)
                .withFee(CurrencyUtil.generateXTO(CoinType, 100))
                .withAmount(CurrencyUtil.generateXTO(CoinType, 0))
                .withPayloadBody(
                        TxGRProposalBody.builder()
                                .withMinBondingTxFee(expectedGR.getMinBondingTxFee())
                                .withCurrentReflection(expectedGR.getCurrentReflection())
                                .build()
                )
                .build();

        txSendResponse = xCube.sendTransaction(txRequest).send();
        assertNull(txSendResponse.getError());

        validatorListResponse = xCube.getValidatorList(null, targetChainId).send();
        actualGR = xCube.getProgressGovernance(null, targetChainId).send();
        assertEquals(expectedGR, actualGR.getGR());
        Assert.assertEquals(validatorListResponse.getResult().get(0).getTotalBondingBalance(), actualGR.getGR().getStake().get(validator));

        boolResponse = xCube.removeNewGR(null, targetChainId).send();
        assertNull(boolResponse.getError());

        //MinGRProposalTxFee
        expectedGR = makeProgressGR();
        expectedGR.setMinGRProposalTxFee(CurrencyUtil.generateXTO(CurrencyUtil.CurrencyType.CoinType, 4));
        expectedGR.setCurrentBlockNo(startCurrentBlockNo++);
        expectedGR.setEndOfVotingBlockNo(startEndOfVotingBlockNo++);
        expectedGR.setReflectionBlockNo(startReflectionBlockNo++);

        txRequest = makeDefaultBuilder()
                .withSender(validator)
                .withReceiver(validator)
                .withPayloadType(ApiEnum.PayloadType.GRProposalType)
                .withFee(CurrencyUtil.generateXTO(CoinType, 100))
                .withAmount(CurrencyUtil.generateXTO(CoinType, 0))
                .withPayloadBody(
                        TxGRProposalBody.builder()
                                .withMinGRProposalTxFee(expectedGR.getMinGRProposalTxFee())
                                .withCurrentReflection(expectedGR.getCurrentReflection())
                                .build()
                )
                .build();

        txSendResponse = xCube.sendTransaction(txRequest).send();
        assertNull(txSendResponse.getError());

        validatorListResponse = xCube.getValidatorList(null, targetChainId).send();
        actualGR = xCube.getProgressGovernance(null, targetChainId).send();
        assertEquals(expectedGR, actualGR.getGR());
        Assert.assertEquals(validatorListResponse.getResult().get(0).getTotalBondingBalance(), actualGR.getGR().getStake().get(validator));

        boolResponse = xCube.removeNewGR(null, targetChainId).send();
        assertNull(boolResponse.getError());

        //MinGRVoteTxFee
        expectedGR = makeProgressGR();
        expectedGR.setMinGRVoteTxFee(CurrencyUtil.generateXTO(CurrencyUtil.CurrencyType.CoinType, 5));
        expectedGR.setCurrentBlockNo(startCurrentBlockNo++);
        expectedGR.setEndOfVotingBlockNo(startEndOfVotingBlockNo++);
        expectedGR.setReflectionBlockNo(startReflectionBlockNo++);

        txRequest = makeDefaultBuilder()
                .withSender(validator)
                .withReceiver(validator)
                .withPayloadType(ApiEnum.PayloadType.GRProposalType)
                .withFee(CurrencyUtil.generateXTO(CoinType, 100))
                .withAmount(CurrencyUtil.generateXTO(CoinType, 0))
                .withPayloadBody(
                        TxGRProposalBody.builder()
                                .withMinGRVoteTxFee(expectedGR.getMinGRVoteTxFee())
                                .withCurrentReflection(expectedGR.getCurrentReflection())
                                .build()
                )
                .build();

        txSendResponse = xCube.sendTransaction(txRequest).send();
        assertNull(txSendResponse.getError());

        validatorListResponse = xCube.getValidatorList(null, targetChainId).send();
        actualGR = xCube.getProgressGovernance(null, targetChainId).send();
        assertEquals(expectedGR, actualGR.getGR());
        Assert.assertEquals(validatorListResponse.getResult().get(0).getTotalBondingBalance(), actualGR.getGR().getStake().get(validator));

        boolResponse = xCube.removeNewGR(null, targetChainId).send();
        assertNull(boolResponse.getError());

        //MinXTxFee
        expectedGR = makeProgressGR();
        expectedGR.setMinXTxFee(CurrencyUtil.generateXTO(CurrencyUtil.CurrencyType.CoinType, 600));
        expectedGR.setCurrentBlockNo(startCurrentBlockNo++);
        expectedGR.setEndOfVotingBlockNo(startEndOfVotingBlockNo++);
        expectedGR.setReflectionBlockNo(startReflectionBlockNo++);

        txRequest = makeDefaultBuilder()
                .withSender(validator)
                .withReceiver(validator)
                .withPayloadType(ApiEnum.PayloadType.GRProposalType)
                .withFee(CurrencyUtil.generateXTO(CoinType, 100))
                .withAmount(CurrencyUtil.generateXTO(CoinType, 0))
                .withPayloadBody(
                        TxGRProposalBody.builder()
                                .withMinXTxFee(expectedGR.getMinXTxFee())
                                .withCurrentReflection(expectedGR.getCurrentReflection())
                                .build()
                )
                .build();

        txSendResponse = xCube.sendTransaction(txRequest).send();
        assertNull(txSendResponse.getError());

        validatorListResponse = xCube.getValidatorList(null, targetChainId).send();
        actualGR = xCube.getProgressGovernance(null, targetChainId).send();
        assertEquals(expectedGR, actualGR.getGR());
        Assert.assertEquals(validatorListResponse.getResult().get(0).getTotalBondingBalance(), actualGR.getGR().getStake().get(validator));

        boolResponse = xCube.removeNewGR(null, targetChainId).send();
        assertNull(boolResponse.getError());

        //MaxBlockNumsForVoting
        expectedGR = makeProgressGR();
        expectedGR.setMaxBlockNumsForVoting(3);
        expectedGR.setCurrentBlockNo(startCurrentBlockNo++);
        expectedGR.setEndOfVotingBlockNo(startEndOfVotingBlockNo++);
        expectedGR.setReflectionBlockNo(startReflectionBlockNo++);

        txRequest = makeDefaultBuilder()
                .withSender(validator)
                .withReceiver(validator)
                .withPayloadType(ApiEnum.PayloadType.GRProposalType)
                .withFee(CurrencyUtil.generateXTO(CoinType, 100))
                .withAmount(CurrencyUtil.generateXTO(CoinType, 0))
                .withPayloadBody(
                        TxGRProposalBody.builder()
                                .withMaxBlockNumsForVoting(expectedGR.getMaxBlockNumsForVoting())
                                .withCurrentReflection(expectedGR.getCurrentReflection())
                                .build()
                )
                .build();

        txSendResponse = xCube.sendTransaction(txRequest).send();
        assertNull(txSendResponse.getError());

        validatorListResponse = xCube.getValidatorList(null, targetChainId).send();
        actualGR = xCube.getProgressGovernance(null, targetChainId).send();
        assertEquals(expectedGR, actualGR.getGR());
        Assert.assertEquals(validatorListResponse.getResult().get(0).getTotalBondingBalance(), actualGR.getGR().getStake().get(validator));

        boolResponse = xCube.removeNewGR(null, targetChainId).send();
        assertNull(boolResponse.getError());

        //MinBlockNumsToGRProposal
        expectedGR = makeProgressGR();
        expectedGR.setMinBlockNumsToGRProposal(55);
        expectedGR.setCurrentBlockNo(startCurrentBlockNo++);
        expectedGR.setEndOfVotingBlockNo(startEndOfVotingBlockNo++);
        expectedGR.setReflectionBlockNo(startReflectionBlockNo++);

        txRequest = makeDefaultBuilder()
                .withSender(validator)
                .withReceiver(validator)
                .withPayloadType(ApiEnum.PayloadType.GRProposalType)
                .withFee(CurrencyUtil.generateXTO(CoinType, 100))
                .withAmount(CurrencyUtil.generateXTO(CoinType, 0))
                .withPayloadBody(
                        TxGRProposalBody.builder()
                                .withMinBlockNumsToGRProposal(expectedGR.getMinBlockNumsToGRProposal())
                                .withCurrentReflection(expectedGR.getCurrentReflection())
                                .build()
                )
                .build();

        txSendResponse = xCube.sendTransaction(txRequest).send();
        assertNull(txSendResponse.getError());

        validatorListResponse = xCube.getValidatorList(null, targetChainId).send();
        actualGR = xCube.getProgressGovernance(null, targetChainId).send();
        assertEquals(expectedGR, actualGR.getGR());
        Assert.assertEquals(validatorListResponse.getResult().get(0).getTotalBondingBalance(), actualGR.getGR().getStake().get(validator));

        boolResponse = xCube.removeNewGR(null, targetChainId).send();
        assertNull(boolResponse.getError());

        //MinBlockNumsUtilReflection
        expectedGR = makeProgressGR();
        expectedGR.setMinBlockNumsUtilReflection(3);
        expectedGR.setCurrentBlockNo(startCurrentBlockNo++);
        expectedGR.setEndOfVotingBlockNo(startEndOfVotingBlockNo++);
        expectedGR.setReflectionBlockNo(startReflectionBlockNo++);

        txRequest = makeDefaultBuilder()
                .withSender(validator)
                .withReceiver(validator)
                .withPayloadType(ApiEnum.PayloadType.GRProposalType)
                .withFee(CurrencyUtil.generateXTO(CoinType, 100))
                .withAmount(CurrencyUtil.generateXTO(CoinType, 0))
                .withPayloadBody(
                        TxGRProposalBody.builder()
                                .withMinBlockNumsUtilReflection(expectedGR.getMinBlockNumsUtilReflection())
                                .withCurrentReflection(expectedGR.getCurrentReflection())
                                .build()
                )
                .build();

        txSendResponse = xCube.sendTransaction(txRequest).send();
        assertNull(txSendResponse.getError());

        validatorListResponse = xCube.getValidatorList(null, targetChainId).send();
        actualGR = xCube.getProgressGovernance(null, targetChainId).send();
        assertEquals(expectedGR, actualGR.getGR());
        Assert.assertEquals(validatorListResponse.getResult().get(0).getTotalBondingBalance(), actualGR.getGR().getStake().get(validator));

        boolResponse = xCube.removeNewGR(null, targetChainId).send();
        assertNull(boolResponse.getError());

        //MaxBlockNumsUtilReflection
        expectedGR = makeProgressGR();
        expectedGR.setMaxBlockNumsUtilReflection(10);
        expectedGR.setCurrentBlockNo(startCurrentBlockNo++);
        expectedGR.setEndOfVotingBlockNo(startEndOfVotingBlockNo++);
        expectedGR.setReflectionBlockNo(startReflectionBlockNo++);

        txRequest = makeDefaultBuilder()
                .withSender(validator)
                .withReceiver(validator)
                .withPayloadType(ApiEnum.PayloadType.GRProposalType)
                .withFee(CurrencyUtil.generateXTO(CoinType, 100))
                .withAmount(CurrencyUtil.generateXTO(CoinType, 0))
                .withPayloadBody(
                        TxGRProposalBody.builder()
                                .withMaxBlockNumsUtilReflection(expectedGR.getMaxBlockNumsUtilReflection())
                                .withCurrentReflection(expectedGR.getCurrentReflection())
                                .build()
                )
                .build();

        txSendResponse = xCube.sendTransaction(txRequest).send();
        assertNull(txSendResponse.getError());

        validatorListResponse = xCube.getValidatorList(null, targetChainId).send();
        actualGR = xCube.getProgressGovernance(null, targetChainId).send();
        assertEquals(expectedGR, actualGR.getGR());
        Assert.assertEquals(validatorListResponse.getResult().get(0).getTotalBondingBalance(), actualGR.getGR().getStake().get(validator));

        boolResponse = xCube.removeNewGR(null, targetChainId).send();
        assertNull(boolResponse.getError());

        //BlockNumsFreezingValidator
        expectedGR = makeProgressGR();
        expectedGR.setBlockNumsFreezingValidator(250);
        expectedGR.setCurrentBlockNo(startCurrentBlockNo++);
        expectedGR.setEndOfVotingBlockNo(startEndOfVotingBlockNo++);
        expectedGR.setReflectionBlockNo(startReflectionBlockNo++);

        txRequest = makeDefaultBuilder()
                .withSender(validator)
                .withReceiver(validator)
                .withPayloadType(ApiEnum.PayloadType.GRProposalType)
                .withFee(CurrencyUtil.generateXTO(CoinType, 100))
                .withAmount(CurrencyUtil.generateXTO(CoinType, 0))
                .withPayloadBody(
                        TxGRProposalBody.builder()
                                .withBlockNumsFreezingValidator(expectedGR.getBlockNumsFreezingValidator())
                                .withCurrentReflection(expectedGR.getCurrentReflection())
                                .build()
                )
                .build();

        txSendResponse = xCube.sendTransaction(txRequest).send();
        assertNull(txSendResponse.getError());

        validatorListResponse = xCube.getValidatorList(null, targetChainId).send();
        actualGR = xCube.getProgressGovernance(null, targetChainId).send();
        assertEquals(expectedGR, actualGR.getGR());
        Assert.assertEquals(validatorListResponse.getResult().get(0).getTotalBondingBalance(), actualGR.getGR().getStake().get(validator));

        boolResponse = xCube.removeNewGR(null, targetChainId).send();
        assertNull(boolResponse.getError());

        //BlockNumsUtilUnbonded
        expectedGR = makeProgressGR();
        expectedGR.setBlockNumsUtilUnbonded(2);
        expectedGR.setCurrentBlockNo(startCurrentBlockNo++);
        expectedGR.setEndOfVotingBlockNo(startEndOfVotingBlockNo++);
        expectedGR.setReflectionBlockNo(startReflectionBlockNo++);

        txRequest = makeDefaultBuilder()
                .withSender(validator)
                .withReceiver(validator)
                .withPayloadType(ApiEnum.PayloadType.GRProposalType)
                .withFee(CurrencyUtil.generateXTO(CoinType, 100))
                .withAmount(CurrencyUtil.generateXTO(CoinType, 0))
                .withPayloadBody(
                        TxGRProposalBody.builder()
                                .withBlockNumsUtilUnbonded(expectedGR.getBlockNumsUtilUnbonded())
                                .withCurrentReflection(expectedGR.getCurrentReflection())
                                .build()
                )
                .build();

        txSendResponse = xCube.sendTransaction(txRequest).send();
        assertNull(txSendResponse.getError());

        validatorListResponse = xCube.getValidatorList(null, targetChainId).send();
        actualGR = xCube.getProgressGovernance(null, targetChainId).send();
        assertEquals(expectedGR, actualGR.getGR());
        Assert.assertEquals(validatorListResponse.getResult().get(0).getTotalBondingBalance(), actualGR.getGR().getStake().get(validator));

        boolResponse = xCube.removeNewGR(null, targetChainId).send();
        assertNull(boolResponse.getError());

        //MaxDelegatableValidatorNums
        expectedGR = makeProgressGR();
        expectedGR.setMaxDelegatableValidatorNums(7);
        expectedGR.setCurrentBlockNo(startCurrentBlockNo++);
        expectedGR.setEndOfVotingBlockNo(startEndOfVotingBlockNo++);
        expectedGR.setReflectionBlockNo(startReflectionBlockNo++);

        txRequest = makeDefaultBuilder()
                .withSender(validator)
                .withReceiver(validator)
                .withPayloadType(ApiEnum.PayloadType.GRProposalType)
                .withFee(CurrencyUtil.generateXTO(CoinType, 100))
                .withAmount(CurrencyUtil.generateXTO(CoinType, 0))
                .withPayloadBody(
                        TxGRProposalBody.builder()
                                .withMaxDelegatableValidatorNums(expectedGR.getMaxDelegatableValidatorNums())
                                .withCurrentReflection(expectedGR.getCurrentReflection())
                                .build()
                )
                .build();

        txSendResponse = xCube.sendTransaction(txRequest).send();
        assertNull(txSendResponse.getError());

        validatorListResponse = xCube.getValidatorList(null, targetChainId).send();
        actualGR = xCube.getProgressGovernance(null, targetChainId).send();
        assertEquals(expectedGR, actualGR.getGR());
        Assert.assertEquals(validatorListResponse.getResult().get(0).getTotalBondingBalance(), actualGR.getGR().getStake().get(validator));

        boolResponse = xCube.removeNewGR(null, targetChainId).send();
        assertNull(boolResponse.getError());

        //ValidatorNums
        expectedGR = makeProgressGR();
        expectedGR.setValidatorNums(7);
        expectedGR.setCurrentBlockNo(startCurrentBlockNo++);
        expectedGR.setEndOfVotingBlockNo(startEndOfVotingBlockNo++);
        expectedGR.setReflectionBlockNo(startReflectionBlockNo++);

        txRequest = makeDefaultBuilder()
                .withSender(validator)
                .withReceiver(validator)
                .withPayloadType(ApiEnum.PayloadType.GRProposalType)
                .withFee(CurrencyUtil.generateXTO(CoinType, 100))
                .withAmount(CurrencyUtil.generateXTO(CoinType, 0))
                .withPayloadBody(
                        TxGRProposalBody.builder()
                                .withValidatorNums(expectedGR.getValidatorNums())
                                .withCurrentReflection(expectedGR.getCurrentReflection())
                                .build()
                )
                .build();

        txSendResponse = xCube.sendTransaction(txRequest).send();
        assertNull(txSendResponse.getError());

        validatorListResponse = xCube.getValidatorList(null, targetChainId).send();
        actualGR = xCube.getProgressGovernance(null, targetChainId).send();
        assertEquals(expectedGR, actualGR.getGR());
        Assert.assertEquals(validatorListResponse.getResult().get(0).getTotalBondingBalance(), actualGR.getGR().getStake().get(validator));

        boolResponse = xCube.removeNewGR(null, targetChainId).send();
        assertNull(boolResponse.getError());

        //FirstCompatibleVersion
        expectedGR = makeProgressGR();
        expectedGR.setFirstCompatibleVersion("2.0.0-stable");
        expectedGR.setCurrentBlockNo(startCurrentBlockNo++);
        expectedGR.setEndOfVotingBlockNo(startEndOfVotingBlockNo++);
        expectedGR.setReflectionBlockNo(startReflectionBlockNo++);

        txRequest = makeDefaultBuilder()
                .withSender(validator)
                .withReceiver(validator)
                .withPayloadType(ApiEnum.PayloadType.GRProposalType)
                .withFee(CurrencyUtil.generateXTO(CoinType, 100))
                .withAmount(CurrencyUtil.generateXTO(CoinType, 0))
                .withPayloadBody(
                        TxGRProposalBody.builder()
                                .withFirstCompatibleVersion(expectedGR.getFirstCompatibleVersion())
                                .withCurrentReflection(expectedGR.getCurrentReflection())
                                .build()
                )
                .build();

        txSendResponse = xCube.sendTransaction(txRequest).send();
        assertNull(txSendResponse.getError());

        validatorListResponse = xCube.getValidatorList(null, targetChainId).send();
        actualGR = xCube.getProgressGovernance(null, targetChainId).send();
        assertEquals(expectedGR, actualGR.getGR());
        Assert.assertEquals(validatorListResponse.getResult().get(0).getTotalBondingBalance(), actualGR.getGR().getStake().get(validator));

        //이미 제안된 GR이 존재하는 경우 새로운 GR을 제안하면 불가능 한가.
        txRequest = makeDefaultBuilder()
                .withSender(validator)
                .withReceiver(validator)
                .withPayloadType(ApiEnum.PayloadType.GRProposalType)
                .withFee(CurrencyUtil.generateXTO(CoinType, 100))
                .withAmount(CurrencyUtil.generateXTO(CoinType, 0))
                .withPayloadBody(
                        TxGRProposalBody.builder()
                                .withFirstCompatibleVersion(expectedGR.getFirstCompatibleVersion())
                                .withCurrentReflection(expectedGR.getCurrentReflection())
                                .build()
                )
                .build();

        txSendResponse = xCube.sendTransaction(txRequest).send();
        assertNotNull(txSendResponse.getError());
        Assert.assertEquals(600, txSendResponse.getError().getCode());

        boolResponse = xCube.removeNewGR(null, targetChainId).send();
        assertNull(boolResponse.getError());
    }

    /**
     * GRVoteTx - grVote validation (txCnt : 2, totalTxCnt : 41, fee : 10,100, total fee : 2,021,823)
     *
     * @throws Exception
     */
    @Test
    @Order(order = 25)
    public void GRVoteTxCheckValidation() throws Exception {
        //Tx의 Amount를 0보다 크게 설정한 경우.
        TxRequest txRequest = makeDefaultBuilder()
                .withSender(validator)
                .withReceiver(validator)
                .withPayloadType(ApiEnum.PayloadType.GRVoteType)
                .withFee(CurrencyUtil.generateXTO(CoinType, 0))
                .withAmount(CurrencyUtil.generateXTO(CoinType, 1))
                .withPayloadBody(new TxGRVoteBody(true))
                .build();

        TxSendResponse sendResponse = xCube.sendTransaction(txRequest).send();
        assertNotNull(sendResponse.getError());
        Assert.assertEquals(307, sendResponse.getError().getCode());

        //Sender와 Receiver를 다르게 설정한 경우.
        txRequest = makeDefaultBuilder()
                .withSender(validator)
                .withReceiver(receiver)
                .withPayloadType(ApiEnum.PayloadType.GRVoteType)
                .withFee(CurrencyUtil.generateXTO(CoinType, 0))
                .withAmount(CurrencyUtil.generateXTO(CoinType, 0))
                .withPayloadBody(new TxGRVoteBody(true))
                .build();

        sendResponse = xCube.sendTransaction(txRequest).send();
        assertNotNull(sendResponse.getError());
        Assert.assertEquals(309, sendResponse.getError().getCode());

        //제안된 GR이 존재하지 않는겨우.
        txRequest = makeDefaultBuilder()
                .withSender(validator)
                .withReceiver(validator)
                .withPayloadType(ApiEnum.PayloadType.GRVoteType)
                .withFee(CurrencyUtil.generateXTO(CoinType, 0))
                .withAmount(CurrencyUtil.generateXTO(CoinType, 0))
                .withPayloadBody(new TxGRVoteBody(true))
                .build();

        sendResponse = xCube.sendTransaction(txRequest).send();
        assertNotNull(sendResponse.getError());
        Assert.assertEquals(601, sendResponse.getError().getCode());


        //아래 테스트를 위하 GR 제안.
        Map<String, BigInteger> stakeMap = new HashMap<>();
        stakeMap.put(validator, CurrencyUtil.generateXTO(CurrencyUtil.CurrencyType.CoinType, 7270002));


        ProgressGovernance.Result expectedGR = new ProgressGovernance.Result();
        expectedGR.setExpectedGRVersion(2);
        expectedGR.setStake(stakeMap);
        expectedGR.setAgreeRate(0);
        expectedGR.setDisagreeRate(0);
        expectedGR.setPass(false);
        expectedGR.setCurrentBlockNo(startCurrentBlockNo++);
        expectedGR.setEndOfVotingBlockNo(startEndOfVotingBlockNo++);
        expectedGR.setReflectionBlockNo((startReflectionBlockNo++) + 2);

        expectedGR.setRewardXtoPerCoin(CurrencyUtil.generateXTO(CurrencyUtil.CurrencyType.CoinType, 10));
        expectedGR.setMinCommonTxFee(CurrencyUtil.generateXTO(CurrencyUtil.CurrencyType.CoinType, 9));
        expectedGR.setMinBondingTxFee(CurrencyUtil.generateXTO(CurrencyUtil.CurrencyType.CoinType, 9999));
        expectedGR.setMinGRProposalTxFee(CurrencyUtil.generateXTO(CurrencyUtil.CurrencyType.CoinType, 99));
        expectedGR.setMinGRVoteTxFee(CurrencyUtil.generateXTO(CurrencyUtil.CurrencyType.CoinType, 1));
        expectedGR.setMinXTxFee(CurrencyUtil.generateXTO(CurrencyUtil.CurrencyType.CoinType, 999));
        expectedGR.setMaxBlockNumsForVoting(49);
        expectedGR.setMinBlockNumsToGRProposal(20);
        expectedGR.setMinBlockNumsUtilReflection(1);
        expectedGR.setMaxBlockNumsUtilReflection(50);
        expectedGR.setBlockNumsFreezingValidator(200);
        expectedGR.setBlockNumsUtilUnbonded(10);
        expectedGR.setMaxDelegatableValidatorNums(30);
        expectedGR.setValidatorNums(30);
        expectedGR.setFirstCompatibleVersion("1.0.0-stable");
        expectedGR.setCurrentReflection(new TxGRProposalBody.CurrentReflection(2, 5));

        txRequest = makeDefaultBuilder()
                .withSender(validator)
                .withReceiver(validator)
                .withPayloadType(ApiEnum.PayloadType.GRProposalType)
                .withFee(CurrencyUtil.generateXTO(CoinType, 100))
                .withAmount(CurrencyUtil.generateXTO(CoinType, 0))
                .withPayloadBody(
                        TxGRProposalBody.builder()
                                .withRewardXtoPerCoin(expectedGR.getRewardXtoPerCoin())
                                .withMinCommonTxFee(expectedGR.getMinCommonTxFee())
                                .withMinBondingTxFee(expectedGR.getMinBondingTxFee())
                                .withMinGRProposalTxFee(expectedGR.getMinGRProposalTxFee())
                                .withMinGRVoteTxFee(expectedGR.getMinGRVoteTxFee())
                                .withMinXTxFee(expectedGR.getMinXTxFee())
                                .withMaxBlockNumsForVoting(expectedGR.getMaxBlockNumsForVoting())
                                .withMinBlockNumsToGRProposal(expectedGR.getMinBlockNumsToGRProposal())
                                .withMinBlockNumsUtilReflection(expectedGR.getMinBlockNumsUtilReflection())
                                .withMaxBlockNumsUtilReflection(expectedGR.getMaxBlockNumsUtilReflection())
                                .withBlockNumsFreezingValidator(expectedGR.getBlockNumsFreezingValidator())
                                .withBlockNumsUtilUnbonded(expectedGR.getBlockNumsUtilUnbonded())
                                .withMaxDelegatableValidatorNums(expectedGR.getMaxDelegatableValidatorNums())
                                .withValidatorNums(expectedGR.getValidatorNums())
                                .withCurrentReflection(expectedGR.getCurrentReflection())
                                .build()
                )
                .build();

        sendResponse = xCube.sendTransaction(txRequest).send();
        assertNull(sendResponse.getError());

        ValidatorListResponse validatorListResponse = xCube.getValidatorList(null, targetChainId).send();
        ProgressGovernance actualGR = xCube.getProgressGovernance(null, targetChainId).send();
        assertEquals(expectedGR, actualGR.getGR());
        Assert.assertEquals(validatorListResponse.getResult().get(0).getTotalBondingBalance(), actualGR.getGR().getStake().get(validator));

        //GR 제안시점의 Validator 였는지 체크 (A계정을 Validator로 등록)
        AccountBalanceResponse expectedSenderBalacne = makeAccountBalance(sender, "6,999,963,001,959,998,000,000,000", "6,919,963,000,199,998,000,000,000", "80,000,000,000,000,000,000,000", "1,760,000,000,000,000", "0", XTOType);
        AccountBalanceResponse actualSenderBalance = xCube.getBalance(null, targetChainId, sender, XTOType).send();
        assertEquals(expectedSenderBalacne.getBalance(), actualSenderBalance.getBalance());

        //1. A계정을 Validator로 등록
        txRequest = makeDefaultBuilder()
                .withSender(sender)
                .withReceiver(sender)
                .withPayloadType(ApiEnum.PayloadType.BondingType)
                .withFee(CurrencyUtil.generateXTO(CoinType, 10000))
                .withAmount(CurrencyUtil.generateXTO(CoinType, 1))
                .withPayloadBody(new TxBondingBody())
                .build();
        sendResponse = xCube.bonding(txRequest).send();
        assertNull(sendResponse.getError());

        expectedSenderBalacne = makeAccountBalance(sender, "6,989,963,002,039,998,000,000,000", "6,909,962,000,199,998,000,000,000", "80,001,000,000,000,000,000,000", "1,840,000,000,000,000", "0", XTOType);
        actualSenderBalance = xCube.getBalance(null, targetChainId, sender, XTOType).send();
        assertEquals(expectedSenderBalacne.getBalance(), actualSenderBalance.getBalance());

        //2. A계정으로 GR 투표
        txRequest = makeDefaultBuilder()
                .withSender(sender)
                .withReceiver(sender)
                .withPayloadType(ApiEnum.PayloadType.GRVoteType)
                .withFee(CurrencyUtil.generateXTO(CoinType, 1))
                .withAmount(CurrencyUtil.generateXTO(CoinType, 0))
                .withPayloadBody(new TxGRVoteBody(true))
                .build();
        sendResponse = xCube.bonding(txRequest).send();
        assertNotNull(sendResponse.getError());
        Assert.assertEquals(318, sendResponse.getError().getCode());

        txRequest = makeDefaultBuilder()
                .withSender(receiver)
                .withReceiver(receiver)
                .withPayloadType(ApiEnum.PayloadType.GRVoteType)
                .withFee(CurrencyUtil.generateXTO(CoinType, 1))
                .withAmount(CurrencyUtil.generateXTO(CoinType, 0))
                .withPayloadBody(new TxGRVoteBody(true))
                .build();
        sendResponse = xCube.bonding(txRequest).send();
        assertNotNull(sendResponse.getError());
        Assert.assertEquals(318, sendResponse.getError().getCode());

        //다음 GR을 테스트하기 위해 삭제
        BoolResponse boolResponse = xCube.removeNewGR(null, targetChainId).send();
        assertNull(boolResponse.getError());
    }

    /**
     * GRVoteTx - grVote disagree (txCnt : 4, totalTxCnt : 45, fee : 101, total fee : 2,021,924)
     *
     * @throws Exception
     */
    @Test
    @Order(order = 26)
    public void GRVoteTxGRVoteDisagree() throws Exception {
        //아래 테스트를 위하 GR 제안.
        ProgressGovernance.Result expectedGR = makeProgressGR();
        expectedGR.setExpectedGRVersion(2);
        expectedGR.setAgreeRate(0);
        expectedGR.setDisagreeRate(0);
        expectedGR.setPass(false);
        startCurrentBlockNo++;
        startEndOfVotingBlockNo++;
        startReflectionBlockNo++;
        expectedGR.setCurrentBlockNo(startCurrentBlockNo++);
        expectedGR.setEndOfVotingBlockNo(startEndOfVotingBlockNo++);
        expectedGR.setReflectionBlockNo(startReflectionBlockNo++);

        expectedGR.setRewardXtoPerCoin(CurrencyUtil.generateXTO(CurrencyUtil.CurrencyType.CoinType, 10));
        expectedGR.setCurrentReflection(new TxGRProposalBody.CurrentReflection(2, 3));

        TxRequest txRequest = makeDefaultBuilder()
                .withSender(validator)
                .withReceiver(validator)
                .withPayloadType(ApiEnum.PayloadType.GRProposalType)
                .withFee(CurrencyUtil.generateXTO(CoinType, 100))
                .withAmount(CurrencyUtil.generateXTO(CoinType, 0))
                .withPayloadBody(
                        TxGRProposalBody.builder()
                                .withRewardXtoPerCoin(expectedGR.getRewardXtoPerCoin())
                                .withCurrentReflection(expectedGR.getCurrentReflection())
                                .build()
                )
                .build();

        TxSendResponse txSendResponse = xCube.sendTransaction(txRequest).send();
        assertNull(txSendResponse.getError());

        ValidatorListResponse validatorListResponse = xCube.getValidatorList(null, targetChainId).send();
        ProgressGovernance actualGR = xCube.getProgressGovernance(null, targetChainId).send();
        assertEquals(expectedGR, actualGR.getGR());
        Assert.assertEquals(validatorListResponse.getResult().get(0).getTotalBondingBalance(), actualGR.getGR().getStake().get(validator));

        //(1) GR 찬성
        txRequest = makeDefaultBuilder()
                .withSender(validator)
                .withReceiver(validator)
                .withPayloadType(ApiEnum.PayloadType.GRVoteType)
                .withFee(CurrencyUtil.generateXTO(CoinType, 0))
                .withAmount(CurrencyUtil.generateXTO(CoinType, 0))
                .withPayloadBody(new TxGRVoteBody(true))
                .build();

        txSendResponse = xCube.sendTransaction(txRequest).send();
        assertNull(txSendResponse.getError());

        actualGR = xCube.getProgressGovernance(null, targetChainId).send();
        assertEquals(true, actualGR.getGR().getVotingResult().get(validator));

        //(2) GR 반대
        txRequest = makeDefaultBuilder()
                .withSender(validator)
                .withReceiver(validator)
                .withPayloadType(ApiEnum.PayloadType.GRVoteType)
                .withFee(CurrencyUtil.generateXTO(CoinType, 0))
                .withAmount(CurrencyUtil.generateXTO(CoinType, 0))
                .withPayloadBody(new TxGRVoteBody(false))
                .build();

        txSendResponse = xCube.sendTransaction(txRequest).send();
        assertNull(txSendResponse.getError());

        actualGR = xCube.getProgressGovernance(null, targetChainId).send();
        assertEquals(false, actualGR.getGR().getVotingResult().get(validator));

        //(3) 위의 2개 Tx가 발생하게 되면 2개의 블럭이 생성되고 그후 위에서 blockNumsForVoting 값을 2로 주었기 때문에 투표를 하게 되면 에러가 리턴되는지 확인.
        txRequest = makeDefaultBuilder()
                .withSender(validator)
                .withReceiver(validator)
                .withPayloadType(ApiEnum.PayloadType.GRVoteType)
                .withFee(CurrencyUtil.generateXTO(CoinType, 0))
                .withAmount(CurrencyUtil.generateXTO(CoinType, 0))
                .withPayloadBody(new TxGRVoteBody(false))
                .build();

        txSendResponse = xCube.sendTransaction(txRequest).send();
        assertNotNull(txSendResponse.getError());
        Assert.assertEquals(321, txSendResponse.getError().getCode());

        //(4) 새로운 블록을 생성하여 최종적으로 반대가 확정되어 기존 GR이 그대로 반영되는지 확인
        txRequest = makeDefaultBuilder()
                .withSender(sender)
                .withReceiver(sender)
                .withPayloadType(ApiEnum.PayloadType.CommonType)
                .withFee(CurrencyUtil.generateXTO(CoinType, 1))
                .withAmount(CurrencyUtil.generateXTO(CoinType, 0))
                .withPayloadBody(new TxCommonBody())
                .build();

        txSendResponse = xCube.sendTransaction(txRequest).send();
        assertNull(txSendResponse.getError());

        actualGR = xCube.getProgressGovernance(null, targetChainId).send();
        assertNotNull(actualGR.getError());
        Assert.assertEquals(601, actualGR.getError().getCode());

        CurrentGovernance.Result expectedCurrentGovernance = makeCurrentGR();
        CurrentGovernance actualCurrentGovernance = xCube.getCurrentGovernance(null, targetChainId).send();
        assertEquals(expectedCurrentGovernance, actualCurrentGovernance.getGR());
    }

    /**
     * GRVoteTx - grVote agree (txCnt : 4, totalTxCnt : 49, fee : 101, total fee : 2,022,025)
     *
     * @throws Exception
     */
    @Test
    @Order(order = 27)
    public void GRVoteTxGRVoteAgree() throws Exception {
        //아래 테스트를 위하 GR 제안.
        ProgressGovernance.Result expectedGR = makeProgressGR();
        expectedGR.setExpectedGRVersion(2);
        expectedGR.setAgreeRate(0);
        expectedGR.setDisagreeRate(0);
        expectedGR.setPass(false);
        expectedGR.setCurrentBlockNo(49);
        expectedGR.setEndOfVotingBlockNo(51);
        expectedGR.setReflectionBlockNo(52);

        expectedGR.setRewardXtoPerCoin(CurrencyUtil.generateXTO(CurrencyUtil.CurrencyType.CoinType, 10));
        expectedGR.setCurrentReflection(new TxGRProposalBody.CurrentReflection(2, 3));

        TxRequest txRequest = makeDefaultBuilder()
                .withSender(validator)
                .withReceiver(validator)
                .withPayloadType(ApiEnum.PayloadType.GRProposalType)
                .withFee(CurrencyUtil.generateXTO(CoinType, 100))
                .withAmount(CurrencyUtil.generateXTO(CoinType, 0))
                .withPayloadBody(
                        TxGRProposalBody.builder()
                                .withRewardXtoPerCoin(expectedGR.getRewardXtoPerCoin())
                                .withCurrentReflection(expectedGR.getCurrentReflection())
                                .build()
                )
                .build();

        TxSendResponse txSendResponse = xCube.sendTransaction(txRequest).send();
        assertNull(txSendResponse.getError());

        ValidatorListResponse validatorListResponse = xCube.getValidatorList(null, targetChainId).send();
        ProgressGovernance actualGR = xCube.getProgressGovernance(null, targetChainId).send();
        assertEquals(expectedGR, actualGR.getGR());
        Assert.assertEquals(validatorListResponse.getResult().get(0).getTotalBondingBalance(), actualGR.getGR().getStake().get(validator));

        //(1) GR 반대
        txRequest = makeDefaultBuilder()
                .withSender(validator)
                .withReceiver(validator)
                .withPayloadType(ApiEnum.PayloadType.GRVoteType)
                .withFee(CurrencyUtil.generateXTO(CoinType, 0))
                .withAmount(CurrencyUtil.generateXTO(CoinType, 0))
                .withPayloadBody(new TxGRVoteBody(false))
                .build();

        txSendResponse = xCube.sendTransaction(txRequest).send();
        assertNull(txSendResponse.getError());

        actualGR = xCube.getProgressGovernance(null, targetChainId).send();
        assertEquals(false, actualGR.getGR().getVotingResult().get(validator));

        //(2) GR 찬성
        txRequest = makeDefaultBuilder()
                .withSender(validator)
                .withReceiver(validator)
                .withPayloadType(ApiEnum.PayloadType.GRVoteType)
                .withFee(CurrencyUtil.generateXTO(CoinType, 0))
                .withAmount(CurrencyUtil.generateXTO(CoinType, 0))
                .withPayloadBody(new TxGRVoteBody(true))
                .build();

        txSendResponse = xCube.sendTransaction(txRequest).send();
        assertNull(txSendResponse.getError());

        actualGR = xCube.getProgressGovernance(null, targetChainId).send();
        assertEquals(true, actualGR.getGR().getVotingResult().get(validator));

        //(3) 위의 2개 Tx가 발생하게 되면 2개의 블럭이 생성되고 그후 위에서 blockNumsForVoting 값을 2로 주었기 때문에 투표를 하게 되면 에러가 리턴되는지 확인.
        txRequest = makeDefaultBuilder()
                .withSender(validator)
                .withReceiver(validator)
                .withPayloadType(ApiEnum.PayloadType.GRVoteType)
                .withFee(CurrencyUtil.generateXTO(CoinType, 0))
                .withAmount(CurrencyUtil.generateXTO(CoinType, 0))
                .withPayloadBody(new TxGRVoteBody(false))
                .build();

        txSendResponse = xCube.sendTransaction(txRequest).send();
        assertNotNull(txSendResponse.getError());
        Assert.assertEquals(321, txSendResponse.getError().getCode());

        //(4) 새로운 블록을 생성하여 최종적으로 가결 확정되어 새로운 GR로 적용되었는지 확인한다.
        txRequest = makeDefaultBuilder()
                .withSender(sender)
                .withReceiver(sender)
                .withPayloadType(ApiEnum.PayloadType.CommonType)
                .withFee(CurrencyUtil.generateXTO(CoinType, 1))
                .withAmount(CurrencyUtil.generateXTO(CoinType, 0))
                .withPayloadBody(new TxCommonBody())
                .build();

        txSendResponse = xCube.sendTransaction(txRequest).send();
        assertNull(txSendResponse.getError());

        actualGR = xCube.getProgressGovernance(null, targetChainId).send();
        assertNotNull(actualGR.getError());
        Assert.assertEquals(601, actualGR.getError().getCode());

        CurrentGovernance.Result expectedCurrentGovernance = makeCurrentGR();
        expectedCurrentGovernance.setRewardXtoPerCoin(expectedGR.getRewardXtoPerCoin());
        expectedCurrentGovernance.setCurrentReflection(expectedGR.getCurrentReflection());
        expectedCurrentGovernance.setGrVersion(2);
        expectedCurrentGovernance.setProposalBlockNo(expectedGR.getCurrentBlockNo());
        expectedCurrentGovernance.setEndOfVotingBlockNo(expectedGR.getEndOfVotingBlockNo());
        expectedCurrentGovernance.setReflectionBlockNo(expectedGR.getReflectionBlockNo());
        expectedCurrentGovernance.setEligibleToVoteMap(expectedGR.getStake());

        Map<String, Boolean> voteHistory = new HashMap<>();
        voteHistory.put(validator, true);
        expectedCurrentGovernance.setVoteHistory(voteHistory);

        CurrentGovernance actualCurrentGovernance = xCube.getCurrentGovernance(null, targetChainId).send();
        assertEquals(expectedCurrentGovernance, actualCurrentGovernance.getGR());
    }

    /**
     * RecoverValidatorTx - 유효성 체크
     *
     * @throws Exception
     */
    @Test
    @Order(order = 28)
    public void RecoverValidatorTxCheckValidation() throws Exception {
        //Sender와 Receiver를 다르게 설정한 경우.
        TxRequest txRequest = makeDefaultBuilder()
                .withSender(validator)
                .withReceiver(receiver)
                .withPayloadType(ApiEnum.PayloadType.RecoverValidatorType)
                .withFee(CurrencyUtil.generateXTO(CoinType, 1))
                .withAmount(CurrencyUtil.generateXTO(CoinType, 0))
                .withPayloadBody(new TxRecoverBody())
                .build();

        TxSendResponse txSendResponse = xCube.sendTransaction(txRequest).send();
        assertNotNull(txSendResponse.getError());
        Assert.assertEquals(309, txSendResponse.getError().getCode());

        //Validator로 등록되지 않은 경우.
        txRequest = makeDefaultBuilder()
                .withSender(receiver)
                .withReceiver(receiver)
                .withPayloadType(ApiEnum.PayloadType.RecoverValidatorType)
                .withFee(CurrencyUtil.generateXTO(CoinType, 1))
                .withAmount(CurrencyUtil.generateXTO(CoinType, 0))
                .withPayloadBody(new TxRecoverBody())
                .build();

        txSendResponse = xCube.sendTransaction(txRequest).send();
        assertNotNull(txSendResponse.getError());
        Assert.assertEquals(401, txSendResponse.getError().getCode());
    }

    /**
     * RecoverValidatorTx - recoverValidator  (txCnt : 1, totalTxCnt : 50, fee : 1, total fee : 2,022,026)
     *
     * @throws Exception
     */
    @Test
    @Order(order = 29)
    public void RecoverValidatorTxRecoverValidator() throws Exception {
        boolean isExists = false;
        ValidatorListResponse validatorListResponse = xCube.getValidatorList(null, targetChainId).send();
        for (ValidatorListResponse.Result result : validatorListResponse.getResult()) {
            if (result.getValidatorAccountAddr().equals(sender)) {
                isExists = true;
                assertEquals(true, result.isFreezing());
                Assert.assertEquals(ApiEnum.FreezingType.Disconnected, result.getFreezingReason());
                assertEquals(48, result.getFreezingBlockNo());
                assertEquals(3, result.getDisconnectCnt());
            }
        }
        assertEquals(true, isExists);

        SimpleValidatorResponse simpleValidatorResponse = xCube.getSimpleValidator(null, targetChainId, sender).send();
        Assert.assertEquals(true, simpleValidatorResponse.getResult().isFreezing());
        Assert.assertEquals(ApiEnum.FreezingType.Disconnected, simpleValidatorResponse.getResult().getFreezingReason());
        Assert.assertEquals(48, simpleValidatorResponse.getResult().getFreezingBlockNo());

        TxRequest txRequest = makeDefaultBuilder()
                .withSender(sender)
                .withReceiver(sender)
                .withPayloadType(ApiEnum.PayloadType.RecoverValidatorType)
                .withFee(CurrencyUtil.generateXTO(CoinType, 1))
                .withAmount(CurrencyUtil.generateXTO(CoinType, 0))
                .withPayloadBody(new TxRecoverBody())
                .build();

        TxSendResponse txSendResponse = xCube.sendTransaction(txRequest).send();
        assertNull(txSendResponse.getError());

        isExists = false;
        validatorListResponse = xCube.getValidatorList(null, targetChainId).send();
        for (ValidatorListResponse.Result result : validatorListResponse.getResult()) {
            if (result.getValidatorAccountAddr().equals(sender)) {
                isExists = true;
                assertEquals(false, result.isFreezing());
                Assert.assertEquals(ApiEnum.FreezingType.NONE, result.getFreezingReason());
                assertEquals(0, result.getFreezingBlockNo());
                assertEquals(0, result.getDisconnectCnt());
            }
        }
        assertEquals(true, isExists);

        simpleValidatorResponse = xCube.getSimpleValidator(null, targetChainId, sender).send();
        Assert.assertEquals(false, simpleValidatorResponse.getResult().isFreezing());
        Assert.assertEquals(ApiEnum.FreezingType.NONE, simpleValidatorResponse.getResult().getFreezingReason());
        Assert.assertEquals(0, simpleValidatorResponse.getResult().getFreezingBlockNo());
    }

    /**
     * UnstakingTx - revoke all stake  (txCnt : 3, totalTxCnt : 53, fee : 10002, total fee : 2,032,028)
     *
     * @throws Exception
     */
    @Test
    @Order(order = 30)
    public void UnstakingTxRevokeAllStake() throws Exception {
        //UnbondingTx - Validator Set 제거
        TxRequest txRequest = makeDefaultBuilder()
                .withSender(sender)
                .withReceiver(sender)
                .withPayloadType(ApiEnum.PayloadType.UnbondingType)
                .withFee(CurrencyUtil.generateXTO(CoinType, 1))
                .withAmount(CurrencyUtil.generateXTO(CoinType, 1))
                .withPayloadBody(new TxUnbondingBody())
                .build();
        TxSendResponse sendResponse = xCube.unbonding(txRequest).send();
        assertNull(sendResponse.getError());

        boolean isExists = false;
        ValidatorListResponse validatorListResponse = xCube.getValidatorList(null, targetChainId).send();
        for (ValidatorListResponse.Result result : validatorListResponse.getResult()) {
            if (result.getValidatorAccountAddr().equals(sender)) {
                isExists = true;
            }
        }
        assertEquals(false, isExists);

        SimpleValidatorResponse simpleValidatorResponse = xCube.getSimpleValidator(null, targetChainId, sender).send();
        assertNull(simpleValidatorResponse.getResult());

        //Undelegating 테스트를 위해 bonding
        txRequest = makeDefaultBuilder()
                .withSender(sender)
                .withReceiver(sender)
                .withPayloadType(ApiEnum.PayloadType.BondingType)
                .withFee(CurrencyUtil.generateXTO(CoinType, 10000))
                .withAmount(CurrencyUtil.generateXTO(CoinType, 1))
                .withPayloadBody(new TxBondingBody())
                .build();
        sendResponse = xCube.bonding(txRequest).send();
        assertNull(sendResponse.getError());

        isExists = false;
        validatorListResponse = xCube.getValidatorList(null, targetChainId).send();
        for (ValidatorListResponse.Result result : validatorListResponse.getResult()) {
            if (result.getValidatorAccountAddr().equals(sender)) {
                isExists = true;
            }
        }
        assertEquals(true, isExists);

        simpleValidatorResponse = xCube.getSimpleValidator(null, targetChainId, sender).send();
        assertNotNull(simpleValidatorResponse.getResult());

        //UndelegatingTx - Validator Set 제거
        txRequest = makeDefaultBuilder()
                .withSender(sender)
                .withReceiver(sender)
                .withPayloadType(ApiEnum.PayloadType.UnbondingType)
                .withFee(CurrencyUtil.generateXTO(CoinType, 1))
                .withAmount(CurrencyUtil.generateXTO(CoinType, 1))
                .withPayloadBody(new TxUnbondingBody())
                .build();
        sendResponse = xCube.unbonding(txRequest).send();
        assertNull(sendResponse.getError());

        isExists = false;
        validatorListResponse = xCube.getValidatorList(null, targetChainId).send();
        for (ValidatorListResponse.Result result : validatorListResponse.getResult()) {
            if (result.getValidatorAccountAddr().equals(sender)) {
                isExists = true;
            }
        }
        assertEquals(false, isExists);

        simpleValidatorResponse = xCube.getSimpleValidator(null, targetChainId, sender).send();
        assertNull(simpleValidatorResponse.getResult());
    }

    @Test
    @Order(order = 31)
    public void CheckATXBalance() throws Exception {
        //초기에 각 계정에 할당한 ATX 값 셋팅
        BigInteger initTotalBalance = new BigInteger("0");
        for (int i = 1; i <= 10; i++) {
            initTotalBalance = initTotalBalance.add(new BigInteger(String.valueOf(i) + "000000000000000000000000"));
        }
        //각 계정에 할당한 총 ATX 값과 블록 생성에 의해 받은 보상값을 더함.
        //초기 GR의 블록당 Reward = 1,000 (48개 블록 생성됨.)
        //변경된 GR의 블록당 Reward = 10 (4개 블록 생성됨.)
        //Validator의 보상된 블록이력의 마지막 BlockNo = 55 (30번째 메소드까지 총 55개의 블록을 생성했지만 마지막 블록은 아직 보상이 안됨. 다음블록이 생성되면 이전블록에 대한 보상이 이루어짐.)
        //해서 55 - 3 (genesis block + proof block + proof block(File Data 체크를 위한 블록)) = 52 + 1(아직 보상되지 않음) == totalTxCnt : 53 값과 같음
        initTotalBalance = initTotalBalance.add(new BigInteger("48040000000000000000000"));

        //모든 계정들의 합과 전체 ATX의 합이 같은지를 비교.
        int totalAccount = 10;
        BigInteger totalBalance = new BigInteger("0");
        BigInteger availableBalance = new BigInteger("0");
        BigInteger stakingBalance = new BigInteger("0");
        BigInteger predictionRewardBalance = new BigInteger("0");
        BigInteger lockingBalance = new BigInteger("0");
        String[] accounts = new String[]{
                "0x392531466fe4f4a4368fb48c33bf13dec2b518a9",
                "0x0224ec920928f97ce2c41fc0e3bbadba70fbd940",
                "0xd52ff6084b6dec53b74b2ac9133fe3541709fa7f",
                "0xeba3f7d6983141ecbca0319aed800458d9a3f2d5",
                "0x2dbcfcd26e47dbe403194caad0cee168f432cf3a",
                "0x7826d36525a285072fd8fe7cbe1597013d8d9761",
                "0x999702f5dccec1af21201f18c25ff04aa4ed413c",
                "0x9ac601f1a9c8385cb1fd794d030898168b0b617a",
                "0x4b86dfdf5b061dbc5c168e54467c4363a3060101",
                "0xfb3deb14ccad367142cf4ea78b2d969b0d100513",
        };
        for (String account : accounts) {
            AccountBalanceResponse actualSender = xCube.getBalance(null, targetChainId, account, CurrencyUtil.CurrencyType.XTOType).send();
            totalBalance = totalBalance.add(actualSender.getBalance().getTotalBalance());
            availableBalance = availableBalance.add(actualSender.getBalance().getAvailableBalance());
            stakingBalance = stakingBalance.add(actualSender.getBalance().getStakingBalance());
            predictionRewardBalance = predictionRewardBalance.add(actualSender.getBalance().getPredictionRewardBalance());
            lockingBalance = lockingBalance.add(actualSender.getBalance().getLockingBalance());
        }

        TotalAtxResponse totalAtxResponse = xCube.getTotalATX(null, targetChainId, CurrencyUtil.CurrencyType.XTOType).send();
        BigInteger tempTotalBalance = new BigInteger("0");
        tempTotalBalance = tempTotalBalance.add(totalAtxResponse.getResult().getAvailableBalance()).add(totalAtxResponse.getResult().getStakingBalance()).add(totalAtxResponse.getResult().getPredictionRewardBalance()).add(totalAtxResponse.getResult().getLockingBalance());
        Assert.assertEquals(totalAccount, totalAtxResponse.getResult().getTotalAccount());
        Assert.assertEquals(totalBalance, totalAtxResponse.getResult().getTotalBalance());
        Assert.assertEquals(availableBalance, totalAtxResponse.getResult().getAvailableBalance());
        Assert.assertEquals(stakingBalance, totalAtxResponse.getResult().getStakingBalance());
        Assert.assertEquals(predictionRewardBalance, totalAtxResponse.getResult().getPredictionRewardBalance());
        Assert.assertEquals(lockingBalance, totalAtxResponse.getResult().getLockingBalance());
        Assert.assertEquals(tempTotalBalance, totalAtxResponse.getResult().getTotalBalance());

        //총 보상값보다 1.5ATX 만큼의 보상이 덜 이루어 지는 것을 감안. (보상값 계산시 보상값 / 지분양 = 블록 보상마다 소수점이 생겨서 차이가 발생함.)
        //또한 여기서 1ATX는 실제로 이전 트랜잭션의 Fee 값이지만 이 메소드를 수행하는 시점에는 해당 Fee에 대한 보상이 이루어지지 않았기 대문에 1의 차이가 발생한다.
        BigInteger allowMinDifferenceAmount = new BigInteger("500,000,000".replaceAll(",", ""));
        BigInteger prevFee = new BigInteger("1,000,000,000,000,000,000".replaceAll(",", ""));
        totalBalance = totalBalance.add(prevFee);
        System.out.println(
                String.format("expectedTotalAmount:%s, actualTotalAmount:%s, diff:%s",
                        NumberUtil.comma(initTotalBalance),
                        NumberUtil.comma(totalBalance),
                        CurrencyUtil.generateStringCurrencyUnitToCurrencyUnit(CurrencyUtil.CurrencyType.XTOType, CurrencyUtil.CurrencyType.CoinType, initTotalBalance.subtract(totalBalance))
                )
        );
        assertTrue(initTotalBalance.equals(totalBalance) || initTotalBalance.subtract(totalBalance).compareTo(allowMinDifferenceAmount) < 0);
    }

    /**
     * MakeXChainTx - 유효성 체크
     *
     * @throws Exception
     */
    @Test
    @Order(order = 32)
    public void MakeXChainTxCheckValidation() throws Exception {
        //(Tx로 검증) Tx의 Amount가 0보다 크게 설정한 경우.
        TxMakeXChainBody.Builder makeXChainBody = TxMakeXChainBody.builder();
        TxRequest txRequest = makeDefaultBuilder()
                .withSender(sender)
                .withReceiver(sender)
                .withPayloadType(ApiEnum.PayloadType.MakeXChainType)
                .withFee(CurrencyUtil.generateXTO(CoinType, 1000))
                .withAmount(CurrencyUtil.generateXTO(CoinType, 1))
                .withPayloadBody(makeXChainBody.build())
                .build();
        TxSendResponse sendResponse = xCube.unbonding(txRequest).send();
        assertNotNull(sendResponse.getError());
        Assert.assertEquals(307, sendResponse.getError().getCode());

        //(Tx로 검증) Sender와 Receiver가 다르게 설정된 경우.
        makeXChainBody = TxMakeXChainBody.builder();
        txRequest = makeDefaultBuilder()
                .withSender(sender)
                .withReceiver(receiver)
                .withPayloadType(ApiEnum.PayloadType.MakeXChainType)
                .withFee(CurrencyUtil.generateXTO(CoinType, 1000))
                .withAmount(CurrencyUtil.generateXTO(CoinType, 0))
                .withPayloadBody(makeXChainBody.build())
                .build();
        sendResponse = xCube.unbonding(txRequest).send();
        assertNotNull(sendResponse.getError());
        Assert.assertEquals(309, sendResponse.getError().getCode());

        //(Tx로 검증) depth가 1보다 작거나 int32의 최대 값보다 크게 설정된 경우.
        makeXChainBody = TxMakeXChainBody.builder()
                .withDepth(2147483648l);
        txRequest = makeDefaultBuilder()
                .withSender(sender)
                .withReceiver(sender)
                .withPayloadType(ApiEnum.PayloadType.MakeXChainType)
                .withFee(CurrencyUtil.generateXTO(CoinType, 1000))
                .withAmount(CurrencyUtil.generateXTO(CoinType, 0))
                .withPayloadBody(makeXChainBody.build())
                .build();
        sendResponse = xCube.unbonding(txRequest).send();
        assertNotNull(sendResponse.getError());
        Assert.assertEquals(1, sendResponse.getError().getCode());

        //(Tx로 검증) AirdropRate 값이 0보다 작거나 100보다 크게 설정된 경우.
        makeXChainBody = TxMakeXChainBody.builder()
                .withDepth(1)
                .withAirdropRate(101);
        txRequest = makeDefaultBuilder()
                .withSender(sender)
                .withReceiver(sender)
                .withPayloadType(ApiEnum.PayloadType.MakeXChainType)
                .withFee(CurrencyUtil.generateXTO(CoinType, 1000))
                .withAmount(CurrencyUtil.generateXTO(CoinType, 0))
                .withPayloadBody(makeXChainBody.build())
                .build();
        sendResponse = xCube.unbonding(txRequest).send();
        assertNotNull(sendResponse.getError());
        Assert.assertEquals(1, sendResponse.getError().getCode());

        //(Tx로 검증) 자산을 갖지 않도록 설정 했는데 자신의 하위 체인이 자산을 갖을 수 있도록 설정된 경우.
        makeXChainBody = TxMakeXChainBody.builder()
                .withDepth(1)
                .withHasAsset(false)
                .withEnableSubAsset(true);
        txRequest = makeDefaultBuilder()
                .withSender(sender)
                .withReceiver(sender)
                .withPayloadType(ApiEnum.PayloadType.MakeXChainType)
                .withFee(CurrencyUtil.generateXTO(CoinType, 1000))
                .withAmount(CurrencyUtil.generateXTO(CoinType, 0))
                .withPayloadBody(makeXChainBody.build())
                .build();
        sendResponse = xCube.unbonding(txRequest).send();
        assertNotNull(sendResponse.getError());
        Assert.assertEquals(1, sendResponse.getError().getCode());

        //(Tx로 검증) 자산을 갖지 않도록 설정 했는데 다른 체인과 자산을 교환할 수 있도록 설정된 경우.
        makeXChainBody = TxMakeXChainBody.builder()
                .withDepth(1)
                .withHasAsset(false)
                .withNonExchangeChain(false);
        txRequest = makeDefaultBuilder()
                .withSender(sender)
                .withReceiver(sender)
                .withPayloadType(ApiEnum.PayloadType.MakeXChainType)
                .withFee(CurrencyUtil.generateXTO(CoinType, 1000))
                .withAmount(CurrencyUtil.generateXTO(CoinType, 0))
                .withPayloadBody(makeXChainBody.build())
                .build();
        sendResponse = xCube.unbonding(txRequest).send();
        assertNotNull(sendResponse.getError());
        Assert.assertEquals(1, sendResponse.getError().getCode());

        //(Tx로 검증) 자산을 갖도록 설정 했는데 AssetHolders, Seeds, Validators가 설정되지 않은 경우.
        makeXChainBody = TxMakeXChainBody.builder()
                .withDepth(1)
                .withHasAsset(true);
        txRequest = makeDefaultBuilder()
                .withSender(sender)
                .withReceiver(sender)
                .withPayloadType(ApiEnum.PayloadType.MakeXChainType)
                .withFee(CurrencyUtil.generateXTO(CoinType, 1000))
                .withAmount(CurrencyUtil.generateXTO(CoinType, 0))
                .withPayloadBody(makeXChainBody.build())
                .build();
        sendResponse = xCube.unbonding(txRequest).send();
        assertNotNull(sendResponse.getError());
        Assert.assertEquals(332, sendResponse.getError().getCode());

        //(Tx로 검증) 자산을 갖지 않도록 설정 했는데 AirdropRate, AssetHolders, Seeds, Validators가 설정된 경우.
        makeXChainBody = TxMakeXChainBody.builder()
                .withDepth(1)
                .withHasAsset(false)
                .withNonExchangeChain(true)
                .withAirdropRate(100);
        txRequest = makeDefaultBuilder()
                .withSender(sender)
                .withReceiver(sender)
                .withPayloadType(ApiEnum.PayloadType.MakeXChainType)
                .withFee(CurrencyUtil.generateXTO(CoinType, 1000))
                .withAmount(CurrencyUtil.generateXTO(CoinType, 0))
                .withPayloadBody(makeXChainBody.build())
                .build();
        sendResponse = xCube.unbonding(txRequest).send();
        assertNotNull(sendResponse.getError());
        Assert.assertEquals(1, sendResponse.getError().getCode());

        //(Tx로 검증) Validators의 주소값이 올바르지 않은 경우.
        List<TxMakeXChainBody.AssetHolder> assetHolders = new ArrayList<>();
        assetHolders.add(new TxMakeXChainBody.AssetHolder(validator, new BigInteger("100")));

        List<TxMakeXChainBody.Seed> seeds = new ArrayList<>();
        seeds.add(new TxMakeXChainBody.Seed("seedId", "192.168.0.1", 7979));

        List<TxMakeXChainBody.Validator> validators = new ArrayList<>();
        validators.add(new TxMakeXChainBody.Validator(new TxMakeXChainBody.Validator.PubKey("xblock/PubKeySecp256k1", "Ak02Nx/qaOCMn0iox4XZaA1qKsSWcl2C2Em8/USAhrP"), CurrencyUtil.generateXTO(CoinType, 1).toString(),
                defaultCompanyName,
                defaultCompanyDesc,
                defaultCompanyUrl,
                defaultCompanyLogoUrl,
                defaultCompanyLat,
                defaultCompanyLon));
        makeXChainBody = TxMakeXChainBody.builder()
                .withDepth(1)
                .withHasAsset(true)
                .withAssetHolders(assetHolders)
                .withSeeds(seeds)
                .withValidators(validators);
        txRequest = makeDefaultBuilder()
                .withSender(sender)
                .withReceiver(sender)
                .withPayloadType(ApiEnum.PayloadType.MakeXChainType)
                .withFee(CurrencyUtil.generateXTO(CoinType, 1000))
                .withAmount(CurrencyUtil.generateXTO(CoinType, 0))
                .withPayloadBody(makeXChainBody.build())
                .build();
        sendResponse = xCube.unbonding(txRequest).send();
        assertNotNull(sendResponse.getError());
        Assert.assertEquals(909, sendResponse.getError().getCode());

        //(Tx로 검증) Validators이 중복으로 설정된 경우.
        assetHolders = new ArrayList<>();
        assetHolders.add(new TxMakeXChainBody.AssetHolder(validator, new BigInteger("100")));

        seeds = new ArrayList<>();
        seeds.add(new TxMakeXChainBody.Seed("seedId", "192.168.0.1", 7979));

        validators = new ArrayList<>();
        validators.add(new TxMakeXChainBody.Validator(new TxMakeXChainBody.Validator.PubKey("xblock/PubKeySecp256k1", "Ak02Nx/qaOCMn0iox4XZaA1qKsSWcl2C2Em8/USAhrPR"), CurrencyUtil.generateXTO(CoinType, 1).toString(),
                defaultCompanyName,
                defaultCompanyDesc,
                defaultCompanyUrl,
                defaultCompanyLogoUrl,
                defaultCompanyLat,
                defaultCompanyLon));
        validators.add(new TxMakeXChainBody.Validator(new TxMakeXChainBody.Validator.PubKey("xblock/PubKeySecp256k1", "Ak02Nx/qaOCMn0iox4XZaA1qKsSWcl2C2Em8/USAhrPR"), CurrencyUtil.generateXTO(CoinType, 15).toString(),
                defaultCompanyName,
                defaultCompanyDesc,
                defaultCompanyUrl,
                defaultCompanyLogoUrl,
                defaultCompanyLat,
                defaultCompanyLon));
        makeXChainBody = TxMakeXChainBody.builder()
                .withDepth(1)
                .withHasAsset(true)
                .withAssetHolders(assetHolders)
                .withSeeds(seeds)
                .withValidators(validators);
        txRequest = makeDefaultBuilder()
                .withSender(sender)
                .withReceiver(sender)
                .withPayloadType(ApiEnum.PayloadType.MakeXChainType)
                .withFee(CurrencyUtil.generateXTO(CoinType, 1000))
                .withAmount(CurrencyUtil.generateXTO(CoinType, 0))
                .withPayloadBody(makeXChainBody.build())
                .build();
        sendResponse = xCube.unbonding(txRequest).send();
        assertNotNull(sendResponse.getError());
        Assert.assertEquals(104, sendResponse.getError().getCode());

        //(Tx로 검증) Validators의 Power가 0과 같거나 작게 설정된 경우.
        assetHolders = new ArrayList<>();
        assetHolders.add(new TxMakeXChainBody.AssetHolder(validator, new BigInteger("100")));

        seeds = new ArrayList<>();
        seeds.add(new TxMakeXChainBody.Seed("seedId", "192.168.0.1", 7979));

        validators = new ArrayList<>();
        validators.add(new TxMakeXChainBody.Validator(new TxMakeXChainBody.Validator.PubKey("xblock/PubKeySecp256k1", "Ak02Nx/qaOCMn0iox4XZaA1qKsSWcl2C2Em8/USAhrPR"), CurrencyUtil.generateXTO(CoinType, -20).toString(),
                defaultCompanyName,
                defaultCompanyDesc,
                defaultCompanyUrl,
                defaultCompanyLogoUrl,
                defaultCompanyLat,
                defaultCompanyLon));
        makeXChainBody = TxMakeXChainBody.builder()
                .withDepth(1)
                .withHasAsset(true)
                .withAssetHolders(assetHolders)
                .withSeeds(seeds)
                .withValidators(validators);
        txRequest = makeDefaultBuilder()
                .withSender(sender)
                .withReceiver(sender)
                .withPayloadType(ApiEnum.PayloadType.MakeXChainType)
                .withFee(CurrencyUtil.generateXTO(CoinType, 1000))
                .withAmount(CurrencyUtil.generateXTO(CoinType, 0))
                .withPayloadBody(makeXChainBody.build())
                .build();
        sendResponse = xCube.unbonding(txRequest).send();
        assertNotNull(sendResponse.getError());
        Assert.assertEquals(107, sendResponse.getError().getCode());

        //(Tx로 검증) Validators의 Validator의 Power값이 AssetHolders에 설정된 자산보다 큰경우.
        assetHolders = new ArrayList<>();
        assetHolders.add(new TxMakeXChainBody.AssetHolder(validator, new BigInteger("100")));

        seeds = new ArrayList<>();
        seeds.add(new TxMakeXChainBody.Seed("seedId", "192.168.0.1", 7979));

        validators = new ArrayList<>();
        validators.add(new TxMakeXChainBody.Validator(new TxMakeXChainBody.Validator.PubKey("xblock/PubKeySecp256k1", "Ak02Nx/qaOCMn0iox4XZaA1qKsSWcl2C2Em8/USAhrPR"), CurrencyUtil.generateXTO(CoinType, 2).toString(),
                defaultCompanyName,
                defaultCompanyDesc,
                defaultCompanyUrl,
                defaultCompanyLogoUrl,
                defaultCompanyLat,
                defaultCompanyLon));
        makeXChainBody = TxMakeXChainBody.builder()
                .withDepth(1)
                .withHasAsset(true)
                .withAssetHolders(assetHolders)
                .withSeeds(seeds)
                .withValidators(validators);
        txRequest = makeDefaultBuilder()
                .withSender(sender)
                .withReceiver(sender)
                .withPayloadType(ApiEnum.PayloadType.MakeXChainType)
                .withFee(CurrencyUtil.generateXTO(CoinType, 1000))
                .withAmount(CurrencyUtil.generateXTO(CoinType, 0))
                .withPayloadBody(makeXChainBody.build())
                .build();
        sendResponse = xCube.unbonding(txRequest).send();
        assertNotNull(sendResponse.getError());
        Assert.assertEquals(106, sendResponse.getError().getCode());

        //(Tx로 검증) Validators에 존재하는데 AssetHolders에 존재하지 않는 경우.
        assetHolders = new ArrayList<>();
        assetHolders.add(new TxMakeXChainBody.AssetHolder(validator, CurrencyUtil.generateXTO(CoinType, 500)));

        seeds = new ArrayList<>();
        seeds.add(new TxMakeXChainBody.Seed("seedId", "192.168.0.1", 7979));

        validators = new ArrayList<>();
        validators.add(new TxMakeXChainBody.Validator(new TxMakeXChainBody.Validator.PubKey("xblock/PubKeySecp256k1", "Ak02Nx/qaOCMn0iox4XZaA1qKsSWcl2C2Em8/USAhrPR"), CurrencyUtil.generateXTO(CoinType, 5).toString(),
                defaultCompanyName,
                defaultCompanyDesc,
                defaultCompanyUrl,
                defaultCompanyLogoUrl,
                defaultCompanyLat,
                defaultCompanyLon));
        validators.add(new TxMakeXChainBody.Validator(new TxMakeXChainBody.Validator.PubKey("xblock/PubKeySecp256k1", "A2110Eau20O5mq6tgQh8crX9tCeLA4ezwhKlCCeC5/Mq"), CurrencyUtil.generateXTO(CoinType, 20).toString(),
                defaultCompanyName,
                defaultCompanyDesc,
                defaultCompanyUrl,
                defaultCompanyLogoUrl,
                defaultCompanyLat,
                defaultCompanyLon));
        makeXChainBody = TxMakeXChainBody.builder()
                .withDepth(1)
                .withHasAsset(true)
                .withAssetHolders(assetHolders)
                .withSeeds(seeds)
                .withValidators(validators);
        txRequest = makeDefaultBuilder()
                .withSender(sender)
                .withReceiver(sender)
                .withPayloadType(ApiEnum.PayloadType.MakeXChainType)
                .withFee(CurrencyUtil.generateXTO(CoinType, 1000))
                .withAmount(CurrencyUtil.generateXTO(CoinType, 0))
                .withPayloadBody(makeXChainBody.build())
                .build();
        sendResponse = xCube.unbonding(txRequest).send();
        assertNotNull(sendResponse.getError());
        Assert.assertEquals(105, sendResponse.getError().getCode());

        //(상위 체인과 비교) Depth값이 상위체인의 Depth와 같거나 큰경우.
        makeXChainBody = TxMakeXChainBody.builder()
                .withDepth(4096)
                .withHasAsset(false)
                .withNonExchangeChain(true);
        txRequest = makeDefaultBuilder()
                .withSender(sender)
                .withReceiver(sender)
                .withPayloadType(ApiEnum.PayloadType.MakeXChainType)
                .withFee(CurrencyUtil.generateXTO(CoinType, 1000))
                .withAmount(CurrencyUtil.generateXTO(CoinType, 0))
                .withPayloadBody(makeXChainBody.build())
                .build();
        sendResponse = xCube.unbonding(txRequest).send();
        assertNotNull(sendResponse.getError());
        Assert.assertEquals(312, sendResponse.getError().getCode());
    }

    /**
     * todo MakeXChainTx - 유효성 체크 (상위체인 생성 후 확인해야 함.)
     *
     * @throws Exception
     */
    @Test
    @Order(order = 33)
    public void MakeXChainTxCheckValidationCompareParentChain() throws Exception {
        //todo(alex) (상위 체인과 비교) 상위 체인이 자산을 갖지 않는데 하위 체인이 자산을 갖도록 설정된 경우.
        //todo(alex) (상위 체인과 비교) 상위 체인이 자산을 갖지 않는데 하위 체인이 자신의 하위 체인 생성시 자산을 갖을 수 있도록 설정된 경우.
        //todo(alex) (상위 체인과 비교) 상위 체인이 자산을 갖지 않는데 하위 체인이 다른 체인과 자산을 교환할 수 있도록 설정된 경우.
        //todo(alex) (상위 체인과 비교) 상위 체인이 하위 체인생성시 자산을 갖을 수 없도록 설정했는데 자산을 갖을 수 있도록 생성을 하는 경우.
    }

    /**
     * MakeXChainTx - make child chain  (txCnt : 3, totalTxCnt : 53, fee : 1002, total fee : 2,815,415)
     *
     * @throws Exception
     */
    @Test
    @Order(order = 34)
    public void MakeXChainTxMakeXChain() throws Exception {

    }

    @Test
    public void testTr() {
        TotalAtxResponse totalAtxResponse = xCube.getTotalATX(null, targetChainId, CurrencyUtil.CurrencyType.XTOType).send();
        System.out.println(JsonUtil.generateClassToJson(totalAtxResponse.getResult()));
    }

    @Test
    public void TestAmount() throws Exception {
        List<Unstaking> unstakingList = new ArrayList<>();

        BigInteger totalStakingOfValidator = CurrencyUtil.generateXTO(CoinType, 8000000);
        BigInteger totalStakingOfDelegator = new BigInteger("0");
        ExpectedRewardResult expectedRewardResult = new ExpectedRewardResult();
        expectedRewardResult.setTotalBalance(getInitBalance());

//        CheckValidationCommonFields();
        CommonTxCheckValidation();
        CommonTx(); //3번째 블록 (1번 = genesis block, 2번 = proof block)
        CommonTxOverTxSize();    //4번째 블록 (3block 보상)
        calculateExpectedReward(expectedRewardResult, makeExpectedReward(1, totalStakingOfValidator, totalStakingOfDelegator, null));
        calculateExpectedReward(expectedRewardResult, makeExpectedReward(2, totalStakingOfValidator, totalStakingOfDelegator, null));
        calculateExpectedReward(expectedRewardResult, makeExpectedReward(3, totalStakingOfValidator, totalStakingOfDelegator, CurrencyUtil.generateXTO(CoinType, 1)));
        assertEqualTotalBalance(expectedRewardResult, CurrencyUtil.generateXTO(CoinType, 3));


//        calculateRewardWithExpectedAndActual(3, totalStaking, totalStakingOfValidator, totalExpectedRewardAmount, null);
//        xCube.getBlockLatestBlock(null, targetChainId).send().getBlock().getBlockNo();
//        argsForCalculate.put(3l, putRewardArgs("1,000", "1"));

//        CommonTxSameSenderAndReceiver(); //5번째 블록 (4block 보상)

//        FileTxCheckValidation();
//        FileTxCheckRegisterValidation(); //6 ~ 9번째 블록 (5 ~ 8 block 보상)

//        FileTxCheckOrigin();    //10번째 블록 (9 ~ 10 Block 보상 - sleep이 있어서 proof block(11번째 블록)으로 인하여 보상이루어 짐.)

//        FileTxOverriding(); //12 ~ 16번째 블록 (12 ~ 15 block 보상)

//        BondingTxCheckValidation();
//        BondingTxBonding(); //17번째 블록 (16 block 보상)
//        argsForCalculate.put(16l, putRewardArgs("1,000", "1"));
//        totalAmount = calculateRewardWithExpectedAndActual(argsForCalculate, null, getInitBalance(), null);
//        //예상값에 10,000을 차감한 이유는 BondingTx에서 Fee를 10,000으로 주었는데 해당 블록 보상은 다음블록에서 이루어 지기 때문이다.
//        BigInteger subReward = printRewardMap(false).add(CurrencyUtil.generateXTO(CurrencyUtil.CurrencyType.CoinType, 10000));
//        TotalAtxResponse totalAtxResponse = xCube.getTotalATX(null, targetChainId, CurrencyUtil.CurrencyType.XTOType).send();
//        Assert.assertEquals(totalAmount.subtract(subReward), totalAtxResponse.getResult().getTotalBalance());
//
//        UnbondingTxCheckValidation();
//        UnbondingTxUnbonding(); //18번째 블록 (17 block 보상)
//        UnbondingTxCheckValidationOfLockBalance();
//        argsForCalculate.put(17l, putRewardArgs("1,000", "10,000"));
//        unstakingList.add(new Unstaking(false, 3l, 17l, CurrencyUtil.generateXTO(CurrencyUtil.CurrencyType.CoinType, NumberUtil.generateStringToBigInteger("800,000"))));
//        totalAmount = calculateRewardWithExpectedAndActual(argsForCalculate, null, getInitBalance(), unstakingList);
//        //예상값에 1을 차감한 이유는 UnbondingTx에서 Fee를 1으로 주었는데 해당 블록 보상은 다음블록에서 이루어 지기 때문이다.
//        subReward = printRewardMap(false).add(CurrencyUtil.generateXTO(CurrencyUtil.CurrencyType.CoinType, 1));
//        totalAtxResponse = xCube.getTotalATX(null, targetChainId, CurrencyUtil.CurrencyType.XTOType).send();
//        Assert.assertEquals(totalAmount.subtract(subReward), totalAtxResponse.getResult().getTotalBalance());
//
//
//        UnbondingTxUseLockingBalance(); //20번째 블록 (18 ~ 19 block 보상)
//        argsForCalculate.put(18l, putRewardArgs("1,000", "1"));
//        argsForCalculate.put(19l, putRewardArgs("1,000", "1"));
//        totalAmount = calculateRewardWithExpectedAndActual(argsForCalculate, null, getInitBalance(), unstakingList);
//        subReward = printRewardMap(false).add(CurrencyUtil.generateXTO(CurrencyUtil.CurrencyType.CoinType, 2792388));
//        totalAtxResponse = xCube.getTotalATX(null, targetChainId, CurrencyUtil.CurrencyType.XTOType).send();
//        Assert.assertEquals(totalAmount.subtract(subReward), totalAtxResponse.getResult().getTotalBalance());
//
//        DelegatingTxCheckValidation();
//        DelegatingTxDelegating(); //21번째 블록 (20 block 보상)
//        argsForCalculate.put(20l, putRewardArgs("1,000", "2,792,388"));
//        totalAmount = calculateRewardWithExpectedAndActual(argsForCalculate, null, getInitBalance(), unstakingList);
//        subReward = printRewardMap(false).add(CurrencyUtil.generateXTO(CurrencyUtil.CurrencyType.CoinType, 1));
//        totalAtxResponse = xCube.getTotalATX(null, targetChainId, CurrencyUtil.CurrencyType.XTOType).send();
//        Assert.assertEquals(totalAmount.subtract(subReward), totalAtxResponse.getResult().getTotalBalance());
//
//        DelegatingTxDelegatingToSelf();  //22번째 블록 (21 block 보상)
//        argsForCalculate.put(21l, putRewardArgs("1,000", "1"));
//        totalAmount = calculateRewardWithExpectedAndActual(argsForCalculate, null, getInitBalance(), unstakingList);
//        subReward = printRewardMap(false).add(CurrencyUtil.generateXTO(CurrencyUtil.CurrencyType.CoinType, 1));
//        totalAtxResponse = xCube.getTotalATX(null, targetChainId, CurrencyUtil.CurrencyType.XTOType).send();
//        Assert.assertEquals(totalAmount.subtract(subReward), totalAtxResponse.getResult().getTotalBalance());
//
//        UndelegatingTxCheckValidation();
//        UndelegatingTxUndelegating();  //23번째 블록 (22 block 보상)
//        argsForCalculate.put(22l, putRewardArgs("1,000", "1"));
//        unstakingList.add(new Unstaking(true, 21l, 22l, CurrencyUtil.generateXTO(CurrencyUtil.CurrencyType.CoinType, NumberUtil.generateStringToBigInteger("19,999"))));
//        totalAmount = calculateRewardWithExpectedAndActual(argsForCalculate, null, getInitBalance(), unstakingList);
//        subReward = printRewardMap(false).add(CurrencyUtil.generateXTO(CurrencyUtil.CurrencyType.CoinType, 1));
//        totalAtxResponse = xCube.getTotalATX(null, targetChainId, CurrencyUtil.CurrencyType.XTOType).send();
//        Assert.assertEquals(totalAmount.subtract(subReward), totalAtxResponse.getResult().getTotalBalance());
//
//        UndelegatingTxUndelegatingOfValidator();  //24번째 블록 (23 block 보상)
//        argsForCalculate.put(23l, putRewardArgs("1,000", "1"));
//        //start가 3인 이유는 undelegating 이지만 validator가 undelegating을 하는데 해당 validator는 3번째 블록부터 bonding을하였기 때문이다.
//        unstakingList.add(new Unstaking(false, 3l, 23l, CurrencyUtil.generateXTO(CurrencyUtil.CurrencyType.CoinType, NumberUtil.generateStringToBigInteger("10,000"))));
//        totalAmount = calculateRewardWithExpectedAndActual(argsForCalculate, null, getInitBalance(), unstakingList);
//        subReward = printRewardMap(false).add(CurrencyUtil.generateXTO(CurrencyUtil.CurrencyType.CoinType, 1));
//        totalAtxResponse = xCube.getTotalATX(null, targetChainId, CurrencyUtil.CurrencyType.XTOType).send();
//        Assert.assertEquals(totalAmount.subtract(subReward), totalAtxResponse.getResult().getTotalBalance());
//
//        GRProposalTxCheckValidation();  //26번째 블록 (24 ~ 25 block 보상)
//        GRProposalTxGRProposal();
//        argsForCalculate.put(24l, putRewardArgs("1,000", "1"));
//        argsForCalculate.put(25l, putRewardArgs("1,000", "1"));
//        totalAmount = calculateRewardWithExpectedAndActual(argsForCalculate, null, getInitBalance(), unstakingList);
//        subReward = printRewardMap(false).add(CurrencyUtil.generateXTO(CurrencyUtil.CurrencyType.CoinType, 100));
//        totalAtxResponse = xCube.getTotalATX(null, targetChainId, CurrencyUtil.CurrencyType.XTOType).send();
//        Assert.assertEquals(totalAmount.subtract(subReward), totalAtxResponse.getResult().getTotalBalance());
//
//        GRProposalTxGRProposalOverriding(); //42번째 블록 (26 ~ 41 block 보상)
//        argsForCalculate.put(26l, putRewardArgs("1,000", "100"));
//        argsForCalculate.put(27l, putRewardArgs("1,000", "100"));
//        argsForCalculate.put(28l, putRewardArgs("1,000", "100"));
//        argsForCalculate.put(29l, putRewardArgs("1,000", "100"));
//        argsForCalculate.put(30l, putRewardArgs("1,000", "100"));
//        argsForCalculate.put(31l, putRewardArgs("1,000", "100"));
//        argsForCalculate.put(32l, putRewardArgs("1,000", "100"));
//        argsForCalculate.put(33l, putRewardArgs("1,000", "100"));
//        argsForCalculate.put(34l, putRewardArgs("1,000", "100"));
//        argsForCalculate.put(35l, putRewardArgs("1,000", "100"));
//        argsForCalculate.put(36l, putRewardArgs("1,000", "100"));
//        argsForCalculate.put(37l, putRewardArgs("1,000", "100"));
//        argsForCalculate.put(38l, putRewardArgs("1,000", "100"));
//        argsForCalculate.put(39l, putRewardArgs("1,000", "100"));
//        argsForCalculate.put(40l, putRewardArgs("1,000", "100"));
//        argsForCalculate.put(41l, putRewardArgs("1,000", "100"));
//        totalAmount = calculateRewardWithExpectedAndActual(argsForCalculate, null, getInitBalance(), unstakingList);
//        subReward = printRewardMap(false).add(CurrencyUtil.generateXTO(CurrencyUtil.CurrencyType.CoinType, 100));
//        totalAtxResponse = xCube.getTotalATX(null, targetChainId, CurrencyUtil.CurrencyType.XTOType).send();
//        Assert.assertEquals(totalAmount.subtract(subReward), totalAtxResponse.getResult().getTotalBalance());
//
//        GRVoteTxCheckValidation(); //44번째 블록 (42 ~ 43 block 보상)
//        argsForCalculate.put(42l, putRewardArgs("1,000", "100"));
//        argsForCalculate.put(43l, putRewardArgs("1,000", "100"));
//        totalAmount = calculateRewardWithExpectedAndActual(argsForCalculate, null, getInitBalance(), unstakingList);
//        subReward = printRewardMap(false).add(CurrencyUtil.generateXTO(CurrencyUtil.CurrencyType.CoinType, 10000));
//        totalAtxResponse = xCube.getTotalATX(null, targetChainId, CurrencyUtil.CurrencyType.XTOType).send();
//        Assert.assertEquals(totalAmount.subtract(subReward), totalAtxResponse.getResult().getTotalBalance());
//
//        GRVoteTxGRVoteDisagree(); //48번째 블록 (44 ~ 47 block 보상)
//        argsForCalculate.put(44l, putRewardArgs("1,000", "10000"));
//        argsForCalculate.put(45l, putRewardArgs("1,000", "100"));
//        argsForCalculate.put(46l, putRewardArgs("1,000", "0"));
//        argsForCalculate.put(47l, putRewardArgs("1,000", "0"));
//        totalAmount = calculateRewardWithExpectedAndActual(argsForCalculate, null, getInitBalance(), unstakingList);
//        subReward = printRewardMap(false).add(CurrencyUtil.generateXTO(CurrencyUtil.CurrencyType.CoinType, 1));
//        totalAtxResponse = xCube.getTotalATX(null, targetChainId, CurrencyUtil.CurrencyType.XTOType).send();
//        Assert.assertEquals(totalAmount.subtract(subReward), totalAtxResponse.getResult().getTotalBalance());
//
//        GRVoteTxGRVoteAgree(); //52번째 블록 (48 ~ 51 block 보상)
//        argsForCalculate.put(48l, putRewardArgs("1,000", "1"));
//        argsForCalculate.put(49l, putRewardArgs("1,000", "100"));
//        argsForCalculate.put(50l, putRewardArgs("1,000", "0"));
//        argsForCalculate.put(51l, putRewardArgs("1,000", "0"));
//        totalAmount = calculateRewardWithExpectedAndActual(argsForCalculate, null, getInitBalance(), unstakingList);
//        subReward = printRewardMap(false).add(CurrencyUtil.generateXTO(CurrencyUtil.CurrencyType.CoinType, 1));
//        totalAtxResponse = xCube.getTotalATX(null, targetChainId, CurrencyUtil.CurrencyType.XTOType).send();
//        Assert.assertEquals(totalAmount.subtract(subReward), totalAtxResponse.getResult().getTotalBalance());
//
//        RecoverValidatorTxCheckValidation();
//        RecoverValidatorTxRecoverValidator(); //53번째 블록 (52 block 보상)
//        argsForCalculate.put(52l, putRewardArgs("10", "1"));
//        totalAmount = calculateRewardWithExpectedAndActual(argsForCalculate, null, getInitBalance(), unstakingList);
//        subReward = printRewardMap(false).add(CurrencyUtil.generateXTO(CurrencyUtil.CurrencyType.CoinType, 1));
//        totalAtxResponse = xCube.getTotalATX(null, targetChainId, CurrencyUtil.CurrencyType.XTOType).send();
//        Assert.assertEquals(totalAmount.subtract(subReward), totalAtxResponse.getResult().getTotalBalance());
//
//        UnstakingTxRevokeAllStake(); //56번째 블록 (53 ~ 55 block 보상)
//        argsForCalculate.put(53l, putRewardArgs("10", "1"));
//        argsForCalculate.put(54l, putRewardArgs("10", "1"));
//        argsForCalculate.put(55l, putRewardArgs("10", "10000"));
//        totalAmount = calculateRewardWithExpectedAndActual(argsForCalculate, null, getInitBalance(), unstakingList);
//        subReward = printRewardMap(false).add(CurrencyUtil.generateXTO(CurrencyUtil.CurrencyType.CoinType, 1));
//        totalAtxResponse = xCube.getTotalATX(null, targetChainId, CurrencyUtil.CurrencyType.XTOType).send();
//        Assert.assertEquals(totalAmount.subtract(subReward), totalAtxResponse.getResult().getTotalBalance());
//
//        CheckATXBalance();
//        MakeXChainTxCheckValidation();
//        MakeXChainTxCheckValidationCompareParentChain();

//        TotalAtxResponse totalAtxResponse = xCube.getTotalATX(null, targetChainId, CurrencyUtil.CurrencyType.CoinType).send();
//        System.out.println(JsonUtil.generateClassToJson(totalAtxResponse.getResult()));
//        AccountBalanceResponse actualSender = xCube.getBalance(null, targetChainId, receiver, CurrencyUtil.CurrencyType.CoinType).send();
//        System.out.println(JsonUtil.generateClassToJson(actualSender.getBalance()));
//        ValidatorListResponse validatorListResponse = xCube.getValidatorList(null, targetChainId).send();
//        System.out.println(JsonUtil.generateClassToJson(validatorListResponse.getResult()));
//        SimpleValidatorResponse simpleValidatorResponse = xCube.getSimpleValidator(null, targetChainId, sender).send();
//        System.out.println(JsonUtil.generateClassToJson(simpleValidatorResponse.getResult()));
//        AccountDataResponse accountDataResponse = xCube.getAccount(null, targetChainId, validator).send();
//        System.out.println(JsonUtil.generateClassToJson(accountDataResponse.getAccount()));
//        AccountBondInfoResponse bondInfoResponse = xCube.getBonding(null, targetChainId, sender, null).send();
//        System.out.println(bondInfoResponse.getBonding());
//        ProgressGovernance txProgressGovernance = xCube.getProgressGovernance(null, targetChainId).send();
//        System.out.println(txProgressGovernance.getError());
//        System.out.println(txProgressGovernance.getGR());
//        CurrentGovernance currentGovernance = xCube.getCurrentGovernance(null, targetChainId).send();
//        System.out.println(JsonUtil.generateClassToJson(currentGovernance.getGR()));
    }

    @Test
    public void test() throws Exception {
        CheckValidationCommonFields();
        CommonTxCheckValidation();
        CommonTx();
        CommonTxOverTxSize();
        CommonTxSameSenderAndReceiver();
        FileTxCheckValidation();
        FileTxCheckRegisterValidation();
        FileTxCheckOrigin();
        FileTxOverriding();
        BondingTxCheckValidation();
        BondingTxBonding();
        UnbondingTxCheckValidation();
        UnbondingTxUnbonding();
        UnbondingTxCheckValidationOfLockBalance();
        UnbondingTxUseLockingBalance();
        DelegatingTxCheckValidation();
        DelegatingTxDelegating();
        DelegatingTxDelegatingToSelf();
        UndelegatingTxCheckValidation();
        UndelegatingTxUndelegating();
        UndelegatingTxUndelegatingOfValidator();
        GRProposalTxCheckValidation();
        GRProposalTxGRProposal();
        GRProposalTxGRProposalOverriding();
        GRVoteTxCheckValidation();
        GRVoteTxGRVoteDisagree();
        GRVoteTxGRVoteAgree();
        RecoverValidatorTxCheckValidation();
        RecoverValidatorTxRecoverValidator();
        UnstakingTxRevokeAllStake();
        MakeXChainTxCheckValidation();

        ValidatorListResponse validatorListResponse = xCube.getValidatorList(null, targetChainId).send();
        System.out.println(JsonUtil.generateClassToJson(validatorListResponse.getResult()));
//        CheckATXBalance();
    }
}
