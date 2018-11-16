package com.xbctechnologies.core.apis.asset;

import com.xbctechnologies.core.TestParent;
import com.xbctechnologies.core.apis.XCube;
import com.xbctechnologies.core.apis.dto.ApiEnum;
import com.xbctechnologies.core.apis.dto.TxRequest;
import com.xbctechnologies.core.apis.dto.res.account.AccountBalanceResponse;
import com.xbctechnologies.core.apis.dto.res.account.AccountBondInfoResponse;
import com.xbctechnologies.core.apis.dto.res.data.*;
import com.xbctechnologies.core.apis.dto.res.tx.TxCheckOriginalResponse;
import com.xbctechnologies.core.apis.dto.res.tx.TxSendResponse;
import com.xbctechnologies.core.apis.dto.res.validator.SimpleValidatorResponse;
import com.xbctechnologies.core.apis.dto.res.validator.SimpleValidatorsResponse;
import com.xbctechnologies.core.apis.dto.res.validator.ValidatorListResponse;
import com.xbctechnologies.core.apis.dto.xtypes.*;
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
import java.util.*;

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
    private static long startEndOfVotingBlockNo = startCurrentBlockNo + 2;
    private static long startReflectionBlockNo = startCurrentBlockNo + 3;

    private List<XCube> xCubeList = new ArrayList<>();

    private static String NEW_ACCOUNT = "0xe6a9ccca61be0a46b41808cade23d5160969d52a";
    public final String newAccountPrivKeyJson = "{\n" +
            "  \"result\": {\n" +
            "    \"address\": \"e6a9ccca61be0a46b41808cade23d5160969d52a\",\n" +
            "    \"crypto\": {\n" +
            "      \"cipher\": \"aes-128-ctr\",\n" +
            "      \"ciphertext\": \"315d2704c9145cacf0f31164103722a4ea5990e9ea909c2c1c4944f27b8a016e\",\n" +
            "      \"cipherparams\": {\n" +
            "        \"iv\": \"48d98c80e5886add6f3b14f9d4db5074\"\n" +
            "      },\n" +
            "      \"kdf\": \"scrypt\",\n" +
            "      \"kdfparams\": {\n" +
            "        \"dklen\": 32,\n" +
            "        \"n\": 262144,\n" +
            "        \"p\": 1,\n" +
            "        \"r\": 8,\n" +
            "        \"salt\": \"6946502db0de2d4e066592818e9b76d45f9e065e6ea302509ddaf71b0cdc24ad\"\n" +
            "      },\n" +
            "      \"c\": 0,\n" +
            "      \"prf\": \"\",\n" +
            "      \"mac\": \"293b8d8215e1011f8321406995fc0671aeb90a0b912406e1fa3672ca4a4f6154\"\n" +
            "    },\n" +
            "    \"id\": \"7e9d8855-53a8-44e3-aa48-8ac37bd5cb91\",\n" +
            "    \"version\": 3\n" +
            "  }\n" +
            "}";

    private TxRequest.Builder makeDefaultBuilder() {
        return TxRequest.builder()
                .withIsSync(true)
                .withTargetChainId(targetChainId)
                .withNotCheckValidation(true);
    }

    @Before
    public void init() {
        String etherHost = "106.251.231.226:7420";
//        String etherHost = "localhost:7979";
        xCube = new XCube(new RestHttpClient(
                RestHttpConfig.builder()
                        .withXNodeUrl(String.format("http://%s", etherHost))
                        .withMaxConnection(100)
                        .withDefaultTimeout(60000)
                        .build()
        ));

        String[] hosts = new String[]{"106.251.231.226:7120", "106.251.231.226:7220", "106.251.231.226:7320", "106.251.231.226:7420"};
        for (String host : hosts) {
            xCubeList.add(new XCube(new RestHttpClient(
                    RestHttpConfig.builder()
                            .withXNodeUrl(String.format("http://%s", host))
                            .withMaxConnection(100)
                            .withDefaultTimeout(60000)
                            .build()
            )));
        }
    }

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
    }

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

        AccountBalanceResponse expectedSender = makeAccountBalance(sender, "6,999,989,000,000,000,000,000,000", "6,999,989,000,000,000,000,000,000", "0", "0", "0", XTOType);
        AccountBalanceResponse actualSender = xCube.getBalance(null, targetChainId, sender, XTOType).send();
        assertEquals(expectedSender.getBalance(), actualSender.getBalance());

        AccountBalanceResponse expectedReceiver = makeAccountBalance(receiver, "4,000,010,000,000,000,000,000,000", "3,999,910,000,000,000,000,000,000", "100,000,000,000,000,000,000", "0", "0", XTOType);
        accumulateRewardOfValidator(expectedReceiver, receiver, null);
        AccountBalanceResponse actualReceiver = xCube.getBalance(null, targetChainId, receiver, XTOType).send();
        assertEquals(expectedReceiver.getBalance(), actualReceiver.getBalance());
    }

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

        AccountBalanceResponse expectedReceiver = makeAccountBalance(receiver, "4,000,020,000,000,000,000,000,000", "3,999,920,000,000,000,000,000,000", "100,000,000,000,000,000,000", "0", "0", XTOType);
        accumulateRewardOfValidator(expectedReceiver, receiver, null);
        AccountBalanceResponse actualReceiver = xCube.getBalance(null, targetChainId, receiver, XTOType).send();
        assertEquals(expectedReceiver.getBalance(), actualReceiver.getBalance());
    }

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

        expectedSender = makeAccountBalance(receiver, "4,000,019,000,000,000,000,000,000", "3,999,919,000,000,000,000,000,000", "100,000,000,000,000,000,000", "0", "0", XTOType);
        accumulateRewardOfValidator(expectedSender, receiver, null);
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

    @Test
    @Order(order = 11)
    public void BondingTxBonding() throws Exception {
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
                .withSender(receiver)
                .withReceiver(receiver)
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
                .withSender(sender)
                .withReceiver(sender)
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

    @Test
    @Order(order = 13)
    public void UnbondingTxUnbonding() throws Exception {
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
    @Order(order = 14)
    public void UnbondingTxCheckValidationOfLockBalance() throws Exception {
        //AvailableBalance + LockingBalance를 합친 Fee를 설정한 경우 사용 불가능하도록 되는지 체크
        TxRequest txRequest = makeDefaultBuilder()
                .withSender(receiver)
                .withReceiver(receiver)
                .withPayloadType(ApiEnum.PayloadType.CommonType)
                .withFee(CurrencyUtil.generateXTO(CoinType, 3990008))
                .withAmount(CurrencyUtil.generateXTO(CoinType, 10))
                .withPayloadBody(new TxCommonBody())
                .build();

        TxSendResponse sendResponse = xCube.sendTransaction(txRequest).send();
        assertNotNull(sendResponse.getError());
        Assert.assertEquals(201, sendResponse.getError().getCode());
    }

    @Test
    @Order(order = 15)
    public void UnbondingTxUseLockingBalance() throws Exception {
        AccountBalanceResponse actualSender = xCube.getBalance(null, targetChainId, receiver, XTOType).send();
        actualSender.getBalance().setTotalBalance(actualSender.getBalance().getTotalBalance().subtract(CurrencyUtil.generateXTO(CoinType, 1)));
        actualSender.getBalance().setAvailableBalance(actualSender.getBalance().getAvailableBalance().subtract(CurrencyUtil.generateXTO(CoinType, 1)));

        BigInteger useAmount = actualSender.getBalance().getAvailableBalance().add(findLowestAmount(actualSender.getBalance().getLockingBalance()));
        //아래 트랜잭션이 포함되는 블록No = 18
        //테스트 진행을 위해 Locking BlockNo를 위의 unbonding  blockNo + 1로 주었다. (블록No 증가를 위해 하나의 Tx를 발생)
        TxRequest txRequest = makeDefaultBuilder()
                .withSender(receiver)
                .withReceiver(receiver)
                .withPayloadType(ApiEnum.PayloadType.CommonType)
                .withFee(CurrencyUtil.generateXTO(CoinType, 1))
                .withAmount(CurrencyUtil.generateXTO(CoinType, 0))
                .withPayloadBody(new TxCommonBody())
                .build();

        TxSendResponse sendResponse = xCube.sendTransaction(txRequest).send();
        assertNull(sendResponse.getError());

        //Locking BlockNo에 도달했을때 실제 사용이 가능해지는지 테스트
        txRequest = makeDefaultBuilder()
                .withSender(receiver)
                .withReceiver(receiver)
                .withPayloadType(ApiEnum.PayloadType.CommonType)
                .withFee(useAmount)
                .withAmount(CurrencyUtil.generateXTO(CoinType, 0))
                .withPayloadBody(new TxCommonBody())
                .build();
        sendResponse = xCube.sendTransaction(txRequest).send();
        assertNull(sendResponse.getError());
    }

    @Test
    @Order(order = 16)
    public void DelegatingTxCheckValidation() throws Exception {
        //DelegatingBody의 amount 값을 0보다 작게 설정한 경우.
        TxRequest txRequest = makeDefaultBuilder()
                .withSender(sender)
                .withReceiver(receiver)
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
                .withReceiver(validator)
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
                .withReceiver(receiver)
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
                .withReceiver(receiver)
                .withPayloadType(ApiEnum.PayloadType.DelegatingType)
                .withFee(CurrencyUtil.generateXTO(CoinType, 1))
                .withAmount(CurrencyUtil.generateXTO(XTOType, Long.parseLong("1,000,000,000,000,000,001".replaceAll(",", ""))))
                .withPayloadBody(new TxDelegatingBody())
                .build();
        sendResponse = xCube.bonding(txRequest).send();
        assertNotNull(sendResponse.getError());
        Assert.assertEquals(344, sendResponse.getError().getCode());
    }

    @Test
    @Order(order = 17)
    public void DelegatingTxDelegating() throws Exception {
        //Validator Bonding 확인
        AccountBondInfoResponse expectedBondingInfoOfValidator = new AccountBondInfoResponse();
        expectedBondingInfoOfValidator.setResult(new AccountBondInfoResponse.Result(
                CurrencyUtil.generateXTO(CoinType, 91),
                CurrencyUtil.generateXTO(CoinType, 0),
                CurrencyUtil.generateXTO(CoinType, 0),
                new HashMap<>()
        ));
        AccountBondInfoResponse actualBondingInfoOfValidator = xCube.getBonding(null, targetChainId, receiver, null).send();
        assertEquals(expectedBondingInfoOfValidator.getBonding(), actualBondingInfoOfValidator.getBonding());

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

        BigInteger delegatingAmount = CurrencyUtil.generateXTO(CoinType, 99);
        TxRequest txRequest = makeDefaultBuilder()
                .withSender(sender)
                .withReceiver(receiver)
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
                CurrencyUtil.generateXTO(CoinType, 91),
                CurrencyUtil.generateXTO(CoinType, 99),
                CurrencyUtil.generateXTO(CoinType, 0),
                new HashMap<>()
        ));
        actualBondingInfoOfValidator = xCube.getBonding(null, targetChainId, receiver, null).send();
        assertEquals(expectedBondingInfoOfValidator.getBonding(), actualBondingInfoOfValidator.getBonding());

        //Validator List에 위임한 List 정보가 있는지 확인
        ValidatorListResponse actualValidator = xCube.getValidatorList(null, targetChainId).send();
        boolean checked = false;
        for (ValidatorListResponse.Result result : actualValidator.getResult()) {
            if (result.getValidatorAccountAddr().equals(receiver)) {
                ValidatorListResponse.Result.Delegator delegator = result.getDelegatorMap().get(sender);
                if (delegator != null && delegator.getTotalBondingBalance().compareTo(delegatingAmount) == 0) {
                    checked = true;
                }
            }
        }
        assertEquals(checked, true);

        //Delegator Bonding 확인
        expectedBondingInfoOfDelegator = new AccountBondInfoResponse();
        Map<String, BigInteger> delegatingHistory = new HashMap<>();
        delegatingHistory.put(receiver, CurrencyUtil.generateXTO(CoinType, 99));
        expectedBondingInfoOfDelegator.setResult(new AccountBondInfoResponse.Result(
                CurrencyUtil.generateXTO(CoinType, 0),
                CurrencyUtil.generateXTO(CoinType, 0),
                CurrencyUtil.generateXTO(CoinType, 99),
                delegatingHistory
        ));
        actualBondingInfoOfDelegator = xCube.getBonding(null, targetChainId, sender, null).send();
        assertEquals(expectedBondingInfoOfDelegator.getBonding(), actualBondingInfoOfDelegator.getBonding());
    }

    @Test
    @Order(order = 18)
    public void DelegatingTxDelegatingToSelf() throws Exception {
        TxRequest txRequest = makeDefaultBuilder()
                .withSender(receiver)
                .withReceiver(receiver)
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
                CurrencyUtil.generateXTO(CoinType, 92),
                CurrencyUtil.generateXTO(CoinType, 99),
                CurrencyUtil.generateXTO(CoinType, 0),
                new HashMap<>()
        ));
        AccountBondInfoResponse actualBondingInfoOfValidator = xCube.getBonding(null, targetChainId, receiver, null).send();
        assertEquals(expectedBondingInfoOfValidator.getBonding(), actualBondingInfoOfValidator.getBonding());
    }

    @Test
    @Order(order = 19)
    public void UndelegatingTxCheckValidation() throws Exception {
        //UndelegatingBody의 amount 값을 0보다 작게 설정한 경우.
        TxRequest txRequest = makeDefaultBuilder()
                .withSender(sender)
                .withReceiver(receiver)
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
                .withReceiver(validator)
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
                .withSender(validator)
                .withReceiver(receiver)
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
                .withReceiver(receiver)
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
                .withReceiver(receiver)
                .withPayloadType(ApiEnum.PayloadType.UndelegatingType)
                .withFee(CurrencyUtil.generateXTO(CoinType, 1))
                .withAmount(CurrencyUtil.generateXTO(XTOType, Long.parseLong("1,000,000,000,000,000,001".replaceAll(",", ""))))
                .withPayloadBody(new TxUndelegatingBody())
                .build();
        sendResponse = xCube.bonding(txRequest).send();
        assertNotNull(sendResponse.getError());
        Assert.assertEquals(344, sendResponse.getError().getCode());
    }

    @Test
    @Order(order = 20)
    public void UndelegatingTxUndelegating() throws Exception {
        //Validator List 확인
        TxRequest txRequest = makeDefaultBuilder()
                .withSender(sender)
                .withReceiver(receiver)
                .withPayloadType(ApiEnum.PayloadType.UndelegatingType)
                .withFee(CurrencyUtil.generateXTO(CoinType, 1))
                .withAmount(CurrencyUtil.generateXTO(CoinType, 9))
                .withPayloadBody(new TxUndelegatingBody())
                .build();

        TxSendResponse sendResponse = xCube.undelegating(txRequest).send();
        assertNull(sendResponse.getError());

        //Delegator Bonding 확인
        AccountBondInfoResponse expectedDelegatorBondingInfo = new AccountBondInfoResponse();
        Map<String, BigInteger> delegatingHistory = new HashMap<>();
        delegatingHistory.put(receiver, CurrencyUtil.generateXTO(CoinType, 90));
        expectedDelegatorBondingInfo.setResult(new AccountBondInfoResponse.Result(
                CurrencyUtil.generateXTO(CoinType, 0),
                CurrencyUtil.generateXTO(CoinType, 0),
                CurrencyUtil.generateXTO(CoinType, 90),
                delegatingHistory
        ));
        AccountBondInfoResponse delegatorBondInfoResponse = xCube.getBonding(null, targetChainId, sender, null).send();
        assertEquals(expectedDelegatorBondingInfo.getBonding(), delegatorBondInfoResponse.getBonding());
    }

    @Test
    @Order(order = 21)
    public void UndelegatingTxUndelegatingOfValidator() throws Exception {
        TxRequest txRequest = makeDefaultBuilder()
                .withSender(receiver)
                .withReceiver(receiver)
                .withPayloadType(ApiEnum.PayloadType.UndelegatingType)
                .withFee(CurrencyUtil.generateXTO(CoinType, 1))
                .withAmount(CurrencyUtil.generateXTO(CoinType, 1))
                .withPayloadBody(new TxUndelegatingBody())
                .build();

        TxSendResponse sendResponse = xCube.undelegating(txRequest).send();
        assertNull(sendResponse.getError());

        //Validator Bonding 확인
        AccountBondInfoResponse expectedValidatorBondingInfo = new AccountBondInfoResponse();
        Map<String, BigInteger> delegatingHistory = new HashMap<>();
        expectedValidatorBondingInfo.setResult(new AccountBondInfoResponse.Result(
                CurrencyUtil.generateXTO(CoinType, 91),
                CurrencyUtil.generateXTO(CoinType, 90),
                CurrencyUtil.generateXTO(CoinType, 0),
                delegatingHistory
        ));
        AccountBondInfoResponse validatorBondInfoResponse = xCube.getBonding(null, targetChainId, receiver, null).send();
        assertEquals(expectedValidatorBondingInfo.getBonding(), validatorBondInfoResponse.getBonding());
    }

    @Test
    @Order(order = 22)
    public void GRProposalTxCheckValidation() throws Exception {
        TxGRProposalBody.Builder grpBuilder = TxGRProposalBody.builder();

        //Tx Fee를 100ATX 이하로 설정한 경우.
        TxRequest txRequest = makeDefaultBuilder()
                .withSender(receiver)
                .withReceiver(receiver)
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
                .withSender(receiver)
                .withReceiver(receiver)
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
                .withSender(receiver)
                .withReceiver(validator)
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
                .withSender(receiver)
                .withReceiver(receiver)
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
                .withSender(receiver)
                .withReceiver(receiver)
                .withPayloadType(ApiEnum.PayloadType.GRProposalType)
                .withFee(CurrencyUtil.generateXTO(CoinType, 100))
                .withAmount(CurrencyUtil.generateXTO(CoinType, 0))
                .withPayloadBody(grpBuilder.withCurrentReflection(new TxGRProposalBody.CurrentReflection(0, -1)).build())
                .build();

        sendResponse = xCube.sendTransaction(txRequest).send();
        assertNotNull(sendResponse.getError());
        Assert.assertEquals(1, sendResponse.getError().getCode());

        //Int64로 정의된 필드들의 값이 Int32의 Max 값보다 큰경우. (2147483647)
        txRequest = makeDefaultBuilder()
                .withSender(receiver)
                .withReceiver(receiver)
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
                .withSender(receiver)
                .withReceiver(receiver)
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
                .withSender(receiver)
                .withReceiver(receiver)
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
                                .withCurrentReflection(new TxGRProposalBody.CurrentReflection(1, 3))
                                .build()
                )
                .build();

        sendResponse = xCube.sendTransaction(txRequest).send();
        assertNotNull(sendResponse.getError());
        Assert.assertEquals(401, sendResponse.getError().getCode());

        //GR 적용 최대가능 기간이 투표가능 최대기간 보다 작거나 같은 경우.
        txRequest = makeDefaultBuilder()
                .withSender(receiver)
                .withReceiver(receiver)
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
    }

    @Test
    @Order(order = 23)
    public void GRProposalTxGRProposal() throws Exception {
        //GR 제안을 할 수 있는 제안블록 수 충족을 위한 블록 증가.
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


        String grProposer = null;
        SimpleValidatorsResponse simpleValidatorsResponse = xCube.getSimpleValidators(null, targetChainId).send();
        for (SimpleValidatorResponse.Result result : simpleValidatorsResponse.getResult()) {
            if ((result.getEndBlockNo() - result.getStartBlockNo()) >= 23) {
                grProposer = result.getValidatorAccountAddr();
            }
        }

        //투표 기간 값보다 가결시 적용해야 할 기간이 더 작은 경우.
        txRequest = makeDefaultBuilder()
                .withSender(grProposer)
                .withReceiver(grProposer)
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
        stakeMap.put(grProposer, CurrencyUtil.generateXTO(CurrencyUtil.CurrencyType.CoinType, 100));


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
                .withSender(grProposer)
                .withReceiver(grProposer)
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

        ProgressGovernance actualGR = xCube.getProgressGovernance(null, targetChainId).send();
        expectedGR.setStake(null);
        actualGR.getGR().setStake(null);
        assertEquals(expectedGR, actualGR.getGR());

        //다음 GR을 테스트하기 위해 Tx발생
        for (int i = 0; i < 2; i++) {
            txRequest = makeDefaultBuilder()
                    .withSender(grProposer)
                    .withReceiver(grProposer)
                    .withPayloadType(ApiEnum.PayloadType.GRVoteType)
                    .withFee(CurrencyUtil.generateXTO(CoinType, 0))
                    .withAmount(CurrencyUtil.generateXTO(CoinType, 0))
                    .withPayloadBody(new TxGRVoteBody(false))
                    .build();

            txSendResponse = xCube.sendTransaction(txRequest).send();
            assertNull(txSendResponse.getError());
        }
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
        assertEquals(601, actualGR.getError().getCode());
        assertNull(actualGR.getGR());
    }

    @Test
    @Order(order = 24)
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

        expectedGR.setRewardXtoPerCoin(CurrencyUtil.generateXTO(CurrencyUtil.CurrencyType.GXTOType, 1));
        expectedGR.setCurrentReflection(new TxGRProposalBody.CurrentReflection(2, 3));

        String grProposer = null;
        SimpleValidatorsResponse simpleValidatorsResponse = xCube.getSimpleValidators(null, targetChainId).send();
        for (SimpleValidatorResponse.Result result : simpleValidatorsResponse.getResult()) {
            if ((result.getEndBlockNo() - result.getStartBlockNo()) >= 1) {
                grProposer = result.getValidatorAccountAddr();
            }
        }

        TxRequest txRequest = makeDefaultBuilder()
                .withSender(grProposer)
                .withReceiver(grProposer)
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

        //(1) GR 찬성
        txRequest = makeDefaultBuilder()
                .withSender(grProposer)
                .withReceiver(grProposer)
                .withPayloadType(ApiEnum.PayloadType.GRVoteType)
                .withFee(CurrencyUtil.generateXTO(CoinType, 0))
                .withAmount(CurrencyUtil.generateXTO(CoinType, 0))
                .withPayloadBody(new TxGRVoteBody(true))
                .build();

        txSendResponse = xCube.sendTransaction(txRequest).send();
        assertNull(txSendResponse.getError());

        //(2) GR 반대
        txRequest = makeDefaultBuilder()
                .withSender(grProposer)
                .withReceiver(grProposer)
                .withPayloadType(ApiEnum.PayloadType.GRVoteType)
                .withFee(CurrencyUtil.generateXTO(CoinType, 0))
                .withAmount(CurrencyUtil.generateXTO(CoinType, 0))
                .withPayloadBody(new TxGRVoteBody(false))
                .build();

        txSendResponse = xCube.sendTransaction(txRequest).send();
        assertNull(txSendResponse.getError());

        //(3) 위의 2개 Tx가 발생하게 되면 2개의 블럭이 생성되고 그후 위에서 blockNumsForVoting 값을 2로 주었기 때문에 투표를 하게 되면 에러가 리턴되는지 확인.
        txRequest = makeDefaultBuilder()
                .withSender(grProposer)
                .withReceiver(grProposer)
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

        ProgressGovernance actualGR = xCube.getProgressGovernance(null, targetChainId).send();
        assertNotNull(actualGR.getError());
        Assert.assertEquals(601, actualGR.getError().getCode());

        CurrentGovernance.Result expectedCurrentGovernance = makeCurrentGR();
        CurrentGovernance actualCurrentGovernance = xCube.getCurrentGovernance(null, targetChainId).send();
        assertEquals(expectedCurrentGovernance.getGrVersion(), actualCurrentGovernance.getGR().getGrVersion());
    }

    @Test
    @Order(order = 25)
    public void GRVoteTxGRVoteAgree() throws Exception {
        //아래 테스트를 위하 GR 제안.
        ProgressGovernance.Result expectedGR = makeProgressGR();
        expectedGR.setExpectedGRVersion(2);
        expectedGR.setAgreeRate(0);
        expectedGR.setDisagreeRate(0);
        expectedGR.setPass(false);
        expectedGR.setCurrentBlockNo(startCurrentBlockNo++);
        expectedGR.setEndOfVotingBlockNo(startEndOfVotingBlockNo++);
        expectedGR.setReflectionBlockNo(startReflectionBlockNo++);

        expectedGR.setRewardXtoPerCoin(CurrencyUtil.generateXTO(CurrencyUtil.CurrencyType.XTOType, 10));
        expectedGR.setCurrentReflection(new TxGRProposalBody.CurrentReflection(4, 5));

        String grProposer = null;
        SimpleValidatorsResponse simpleValidatorsResponse = xCube.getSimpleValidators(null, targetChainId).send();
        for (SimpleValidatorResponse.Result result : simpleValidatorsResponse.getResult()) {
            if ((result.getEndBlockNo() - result.getStartBlockNo()) >= 1) {
                grProposer = result.getValidatorAccountAddr();
            }
        }

        TxRequest txRequest = makeDefaultBuilder()
                .withSender(grProposer)
                .withReceiver(grProposer)
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

        // GR 찬성
        txRequest = makeDefaultBuilder()
                .withSender(grProposer)
                .withReceiver(grProposer)
                .withPayloadType(ApiEnum.PayloadType.GRVoteType)
                .withFee(CurrencyUtil.generateXTO(CoinType, 0))
                .withAmount(CurrencyUtil.generateXTO(CoinType, 0))
                .withPayloadBody(new TxGRVoteBody(true))
                .build();

        txSendResponse = xCube.sendTransaction(txRequest).send();
        assertNull(txSendResponse.getError());

        ProgressGovernance actualGR = xCube.getProgressGovernance(null, targetChainId).send();
        Iterator<String> iter = actualGR.getGR().getStake().keySet().iterator();
        int txCnt = 4;
        while (iter.hasNext()) {
            String addr = iter.next();
            if (addr.equals(grProposer)) {
                continue;
            }

            txRequest = makeDefaultBuilder()
                    .withSender(addr)
                    .withReceiver(addr)
                    .withPayloadType(ApiEnum.PayloadType.GRVoteType)
                    .withFee(CurrencyUtil.generateXTO(CoinType, 0))
                    .withAmount(CurrencyUtil.generateXTO(CoinType, 0))
                    .withPayloadBody(new TxGRVoteBody(true))
                    .build();

            txSendResponse = xCube.sendTransaction(txRequest).send();
            assertNull(txSendResponse.getError());
            txCnt--;
        }

        //(4) 새로운 블록을 생성하여 최종적으로 가결 확정되어 새로운 GR로 적용되었는지 확인한다.
        for (int i = 0; i < txCnt; i++) {
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
        }

        actualGR = xCube.getProgressGovernance(null, targetChainId).send();
        assertNotNull(actualGR.getError());
        Assert.assertEquals(601, actualGR.getError().getCode());

        CurrentGovernance actualCurrentGovernance = xCube.getCurrentGovernance(null, targetChainId).send();
        assertEquals(2, actualCurrentGovernance.getGR().getGrVersion());
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
                .withSender(sender)
                .withReceiver(sender)
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
        //1. A계정을 Validator로 등록
        TxRequest txRequest = makeDefaultBuilder()
                .withSender(sender)
                .withReceiver(sender)
                .withPayloadType(ApiEnum.PayloadType.BondingType)
                .withFee(CurrencyUtil.generateXTO(CoinType, 10000))
                .withAmount(CurrencyUtil.generateXTO(CoinType, 1))
                .withPayloadBody(new TxBondingBody())
                .build();
        TxSendResponse sendResponse = xCube.bonding(txRequest).send();
        assertNull(sendResponse.getError());

        for (int i = 0; i < 5; i++) {
            txRequest = makeDefaultBuilder()
                    .withSender(sender)
                    .withReceiver(sender)
                    .withPayloadType(ApiEnum.PayloadType.CommonType)
                    .withFee(CurrencyUtil.generateXTO(CoinType, 1))
                    .withAmount(CurrencyUtil.generateXTO(CoinType, 0))
                    .withPayloadBody(new TxCommonBody())
                    .build();
            sendResponse = xCube.sendTransaction(txRequest).send();
            assertNull(sendResponse.getError());
        }

        boolean isExists = false;
        ValidatorListResponse validatorListResponse = xCube.getValidatorList(null, targetChainId).send();
        for (ValidatorListResponse.Result result : validatorListResponse.getResult()) {
            if (result.getValidatorAccountAddr().equals(sender)) {
                isExists = true;
                assertEquals(true, result.isFreezing());
                Assert.assertEquals(ApiEnum.FreezingType.Disconnected, result.getFreezingReason());
                assertEquals(44, result.getFreezingBlockNo());
                assertEquals(3, result.getDisconnectCnt());
            }
        }
        assertEquals(true, isExists);

        SimpleValidatorResponse simpleValidatorResponse = xCube.getSimpleValidator(null, targetChainId, sender).send();
        Assert.assertEquals(true, simpleValidatorResponse.getResult().isFreezing());
        Assert.assertEquals(ApiEnum.FreezingType.Disconnected, simpleValidatorResponse.getResult().getFreezingReason());
        Assert.assertEquals(44, simpleValidatorResponse.getResult().getFreezingBlockNo());

        txRequest = makeDefaultBuilder()
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
                .withPayloadType(ApiEnum.PayloadType.UndelegatingType)
                .withFee(CurrencyUtil.generateXTO(CoinType, 1))
                .withAmount(CurrencyUtil.generateXTO(CoinType, 1))
                .withPayloadBody(new TxUndelegatingBody())
                .build();
        sendResponse = xCube.undelegating(txRequest).send();
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
        validators.add(new TxMakeXChainBody.Validator(new TxMakeXChainBody.Validator.PubKey("xblock/PubKeySecp256k1", "AhWzVf0OyJqfM7668hiK3K1yJg38uycx9wDwMRAnssP"), CurrencyUtil.generateXTO(CoinType, 1).toString(),
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
        validators.add(new TxMakeXChainBody.Validator(new TxMakeXChainBody.Validator.PubKey("xblock/PubKeySecp256k1", "AhWzVf0OyJqfM7668hiK3K1yJg38uycx9wDwMRAnssPfR"), CurrencyUtil.generateXTO(CoinType, 1).toString(),
                defaultCompanyName,
                defaultCompanyDesc,
                defaultCompanyUrl,
                defaultCompanyLogoUrl,
                defaultCompanyLat,
                defaultCompanyLon));
        validators.add(new TxMakeXChainBody.Validator(new TxMakeXChainBody.Validator.PubKey("xblock/PubKeySecp256k1", "AhWzVf0OyJqfM7668hiK3K1yJg38uycx9wDwMRAnssPfR"), CurrencyUtil.generateXTO(CoinType, 15).toString(),
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
        validators.add(new TxMakeXChainBody.Validator(new TxMakeXChainBody.Validator.PubKey("xblock/PubKeySecp256k1", "AhWzVf0OyJqfM7668hiK3K1yJg38uycx9wDwMRAnssPfR"), CurrencyUtil.generateXTO(CoinType, -20).toString(),
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
        validators.add(new TxMakeXChainBody.Validator(new TxMakeXChainBody.Validator.PubKey("xblock/PubKeySecp256k1", "AhWzVf0OyJqfM7668hiK3K1yJg38uycx9wDwMRAnssPfR"), CurrencyUtil.generateXTO(CoinType, 2).toString(),
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
        validators.add(new TxMakeXChainBody.Validator(new TxMakeXChainBody.Validator.PubKey("xblock/PubKeySecp256k1", "AhWzVf0OyJqfM7668hiK3K1yJg38uycx9wDwMRAnssPfR"), CurrencyUtil.generateXTO(CoinType, 5).toString(),
                defaultCompanyName,
                defaultCompanyDesc,
                defaultCompanyUrl,
                defaultCompanyLogoUrl,
                defaultCompanyLat,
                defaultCompanyLon));
        validators.add(new TxMakeXChainBody.Validator(new TxMakeXChainBody.Validator.PubKey("xblock/PubKeySecp256k1", "AuNGgU3yl4p00bqvNYbx/3CyX0VomKMM6q6dxc9Q4c1u"), CurrencyUtil.generateXTO(CoinType, 20).toString(),
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

    @Test
    @Order(order = 33)
    public void SendNewAccount() throws Exception {
        AccountBalanceResponse receiverBalanceResponse = xCube.getBalance(null, targetChainId, NEW_ACCOUNT, XTOType).send();
        assertNotNull(receiverBalanceResponse.getError());
        assertEquals(200, receiverBalanceResponse.getError().getCode());

        TxRequest txRequest = makeDefaultBuilder()
                .withSender(sender)
                .withReceiver(NEW_ACCOUNT)
                .withPayloadType(ApiEnum.PayloadType.CommonType)
                .withFee(CurrencyUtil.generateXTO(CoinType, 2))
                .withAmount(CurrencyUtil.generateXTO(CoinType, 10))
                .withPayloadBody(new TxCommonBody())
                .build();
        TxSendResponse sendResponse = xCube.sendTransaction(txRequest).send();
        assertNull(sendResponse.getError());

        receiverBalanceResponse = xCube.getBalance(null, targetChainId, NEW_ACCOUNT, XTOType).send();
        assertNull(receiverBalanceResponse.getError());
        assertEquals(CurrencyUtil.generateXTO(CoinType, 10), receiverBalanceResponse.getBalance().getTotalBalance());

        txRequest = makeDefaultBuilder()
                .withSender(NEW_ACCOUNT)
                .withReceiver(sender)
                .withPayloadType(ApiEnum.PayloadType.CommonType)
                .withFee(CurrencyUtil.generateXTO(CoinType, 1))
                .withAmount(CurrencyUtil.generateXTO(CoinType, 2))
                .withPayloadBody(new TxCommonBody())
                .build();
        SignUtil.signTx(txRequest, privKeyPassword, newAccountPrivKeyJson);
        sendResponse = xCube.sendTransaction(txRequest).send();
        assertNull(sendResponse.getError());

        receiverBalanceResponse = xCube.getBalance(null, targetChainId, NEW_ACCOUNT, XTOType).send();
        assertEquals(CurrencyUtil.generateXTO(CoinType, 7), receiverBalanceResponse.getBalance().getTotalBalance());
    }

    @Test
    public void testorder() throws Exception {
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
        GRVoteTxGRVoteDisagree();
        GRVoteTxGRVoteAgree();
        RecoverValidatorTxCheckValidation();
        RecoverValidatorTxRecoverValidator();
        UnstakingTxRevokeAllStake();
        MakeXChainTxCheckValidation();
        SendNewAccount();
    }

    @Test
    @Order(order = 200)
    public void test() {
        ValidatorListResponse validatorListResponse = xCube.getValidatorList(null, targetChainId).send();
        System.out.println(JsonUtil.generateClassToJson(validatorListResponse.getResult()));
//
//        SimpleValidatorsResponse simpleValidatorsResponse = xCube.getSimpleValidators(null, targetChainId).send();
//        System.out.println(JsonUtil.generateClassToJson(simpleValidatorsResponse.getResult()));
//
//        AccountResponse accountDataResponse = xCube.getAccount(null, targetChainId, sender).send();
//        System.out.println(JsonUtil.generateClassToJson(accountDataResponse.getAccount()));

        AccountBalanceResponse actualSender = xCube.getBalance(null, targetChainId, sender, XTOType).send();
        System.out.println(JsonUtil.generateClassToJson(actualSender.getBalance()));
    }
}
