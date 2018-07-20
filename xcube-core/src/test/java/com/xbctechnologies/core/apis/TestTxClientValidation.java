package com.xbctechnologies.core.apis;

import com.xbctechnologies.core.TestParent;
import com.xbctechnologies.core.apis.dto.ApiEnum;
import com.xbctechnologies.core.apis.dto.req.tx.TxRequest;
import com.xbctechnologies.core.apis.dto.res.tx.TxSendResponse;
import com.xbctechnologies.core.apis.dto.xtypes.TxCommonBody;
import com.xbctechnologies.core.apis.dto.xtypes.TxFileBody;
import com.xbctechnologies.core.exception.TransactionException;
import com.xbctechnologies.core.order.Order;
import com.xbctechnologies.core.order.OrderedRunner;
import com.xbctechnologies.core.utils.CurrencyUtil;
import com.xbctechnologies.core.utils.SignUtil;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.xbctechnologies.core.apis.dto.ApiEnum.PayloadType.CommonType;
import static com.xbctechnologies.core.apis.dto.ApiEnum.PayloadType.FileType;
import static com.xbctechnologies.core.utils.CurrencyUtil.CurrencyType.CoinType;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

/**
 * 서버로 Transaction 전송전 Java Client 에서 검증 가능한 데이터들이 정상적으로 검증이 되는지 테스트한다.
 */
@RunWith(OrderedRunner.class)
public class TestTxClientValidation extends TestParent {
    private TxRequest.Builder makeDefaultBuilder() {
        return TxRequest.builder()
                .withIsSync(true)
                .withTargetChainId(targetChainId);
    }

    @Test
    @Order(order = 1)
    public void CheckTxCommonValidation() throws Exception {
        //TargetChainID 설정하지 않음
        TxSendResponse result = null;
        TxRequest.Builder txBuilder = makeDefaultBuilder()
                .withTargetChainId(null)
                .withPayloadBody(new TxCommonBody());

        try {
            result = xCube.sendTransaction(txBuilder.build()).send();
        } catch (TransactionException e) {
            assertEquals("Must be set target chainId", e.getMessage());
        }
        assertNull(result);

        //Sender 또는 Receiver 주소에 0x가 안붙는 경우.
        txBuilder = makeDefaultBuilder()
                .withSender("9ac601f1a9c8385cb1fd794d030898168b0b617a")
                .withReceiver(receiver)
                .withPayloadBody(new TxCommonBody());
        try {
            result = xCube.sendTransaction(txBuilder.build()).send();
        } catch (TransactionException e) {
            assertEquals("The sender and receiver must have the prefix 0x", e.getMessage());
        }
        assertNull(result);

        //Sender 설정하지 않은 경우.
        txBuilder = makeDefaultBuilder()
                .withReceiver(receiver)
                .withPayloadBody(new TxCommonBody());
        try {
            result = xCube.sendTransaction(txBuilder.build()).send();
        } catch (TransactionException e) {
            assertEquals("Must be set sender", e.getMessage());
        }
        assertNull(result);

        //Receiver 설정하지 않은 경우.
        txBuilder = makeDefaultBuilder()
                .withSender(sender)
                .withPayloadBody(new TxCommonBody());
        try {
            result = xCube.sendTransaction(txBuilder.build()).send();
        } catch (TransactionException e) {
            assertEquals("Must be set receiver", e.getMessage());
        }
        assertNull(result);

        //정의되지 않은 PayloadType 설정
        txBuilder = makeDefaultBuilder()
                .withSender(sender)
                .withReceiver(receiver)
                .withPayloadType(null)
                .withPayloadBody(new TxCommonBody());
        try {
            result = xCube.sendTransaction(txBuilder.build()).send();
        } catch (TransactionException e) {
            assertEquals("Must be set payload type", e.getMessage());
        }
        assertNull(result);

        //클라이언트에서 서명한 Account와 Sender가 다른 경우.
        txBuilder = makeDefaultBuilder()
                .withSender(validator)
                .withReceiver(receiver)
                .withFee(CurrencyUtil.generateXTO(CoinType, 1))
                .withAmount(CurrencyUtil.generateXTO(CoinType, 10))
                .withPayloadType(CommonType)
                .withPayloadBody(new TxCommonBody());

        TxRequest txRequest = txBuilder.build();
        SignUtil.signTx(txRequest, privKeyPassword, privKeyJson);
        try {
            result = xCube.sendTransaction(txRequest).send();
        } catch (TransactionException e) {
            assertEquals("The signature is invalid", e.getMessage());
        }
        assertNull(result);

        //PayloadType과 PayloadBody가 맞지 않는경우.
        txBuilder = makeDefaultBuilder()
                .withSender(validator)
                .withReceiver(receiver)
                .withFee(CurrencyUtil.generateXTO(CoinType, 1))
                .withAmount(CurrencyUtil.generateXTO(CoinType, 0))
                .withPayloadType(FileType)
                .withPayloadBody(new TxCommonBody());
        try {
            result = xCube.sendTransaction(txBuilder.build()).send();
        } catch (TransactionException e) {
            assertEquals("If payloadbody is TxCommonBody, then payloadtype must be set to CommonType.", e.getMessage());
        }
        assertNull(result);

        //Amount를 음수로 설정
        txBuilder = makeDefaultBuilder()
                .withSender(validator)
                .withReceiver(receiver)
                .withFee(CurrencyUtil.generateXTO(CoinType, 1))
                .withAmount(CurrencyUtil.generateXTO(CoinType, -1))
                .withPayloadType(CommonType)
                .withPayloadBody(new TxCommonBody());
        try {
            result = xCube.sendTransaction(txBuilder.build()).send();
        } catch (TransactionException e) {
            assertEquals("Amount must be greater than zero", e.getMessage());
        }
        assertNull(result);
    }

    @Test
    @Order(order = 2)
    public void CheckTxFileValidation() throws Exception {
        //Op 필드가 정의되지 않은 값인 경우.
        TxSendResponse result = null;
        TxRequest.Builder txBuilder = makeDefaultBuilder()
                .withSender(sender)
                .withReceiver(sender)
                .withPayloadType(FileType)
                .withPayloadBody(new TxFileBody());
        try {
            result = xCube.sendTransaction(txBuilder.build()).send();
        } catch (TransactionException e) {
            assertEquals("The op field must be set", e.getMessage());
        }
        assertNull(result);

        //DataHash 필드가 정의되지 않은 경우. (파일이 존재하지 않는다는 것. file > base64 변환된 값을 DataHash에 저장)
        result = null;
        txBuilder = makeDefaultBuilder()
                .withSender(sender)
                .withReceiver(sender)
                .withPayloadType(FileType)
                .withPayloadBody(TxFileBody.builder().withAuthors(Arrays.asList(sender)).withOp(ApiEnum.OpType.RegisterType).build());
        try {
            result = xCube.sendTransaction(txBuilder.build()).send();
        } catch (TransactionException e) {
            assertEquals("The file field must be set", e.getMessage());
        }
        assertNull(result);

        //Sender와 Receiver가 다른경우. (최초 원본파일 등록시)
        result = null;
        File file = new File(this.getClass().getResource(testDummyFile).getFile());
        txBuilder = makeDefaultBuilder()
                .withSender(sender)
                .withReceiver(receiver)
                .withPayloadType(FileType)
                .withPayloadBody(TxFileBody.builder().withAuthors(Arrays.asList(sender)).withFile(file).withOp(ApiEnum.OpType.RegisterType).build());
        try {
            result = xCube.sendTransaction(txBuilder.build()).send();
        } catch (TransactionException e) {
            assertEquals("File registration should be the same as sender and receiver", e.getMessage());
        }
        assertNull(result);

        //Sender와 Receiver가 같은경우. (업데이트시)
        result = null;
        txBuilder = makeDefaultBuilder()
                .withSender(sender)
                .withReceiver(sender)
                .withPayloadType(FileType)
                .withPayloadBody(TxFileBody.builder().withAuthors(Arrays.asList(sender)).withOp(ApiEnum.OpType.UpdateType).build());
        try {
            result = xCube.sendTransaction(txBuilder.build()).send();
        } catch (TransactionException e) {
            assertEquals("The receiver field must use the value returned when registering the file.", e.getMessage());
        }
        assertNull(result);

        //Authors 입력하지 않은 경우. (최초 원본파일 등록시)
        result = null;
        txBuilder = makeDefaultBuilder()
                .withSender(sender)
                .withReceiver(sender)
                .withPayloadType(FileType)
                .withPayloadBody(TxFileBody.builder().withOp(ApiEnum.OpType.RegisterType).build());
        try {
            result = xCube.sendTransaction(txBuilder.build()).send();
        } catch (TransactionException e) {
            assertEquals("The authors field in the file transaction must be set.", e.getMessage());
        }
        assertNull(result);

        //모든 사용자 허용으로 한경우. 다른 주소를 추가했을때. (최초 원본파일 또는 업데이트시)
        List<String> authors = new ArrayList<>();
        authors.add(sender);
        authors.add("0xffffffffffffffffffffffffffffffffffffffff");

        result = null;
        txBuilder = makeDefaultBuilder()
                .withSender(sender)
                .withReceiver(sender)
                .withPayloadType(FileType)
                .withPayloadBody(TxFileBody.builder().withOp(ApiEnum.OpType.RegisterType).withAuthors(authors).withFile(file).build());
        try {
            result = xCube.sendTransaction(txBuilder.build()).send();
        } catch (TransactionException e) {
            assertEquals("If Allow and Disallow are set, no other addresses can be set.", e.getMessage());
        }
        assertNull(result);

        //모든 사용자 비허용으로 한경우. 다른 주소를 추가했을때. (최초 원본파일 또는 업데이트시)
        authors = new ArrayList<>();
        authors.add(sender);
        authors.add("0x0000000000000000000000000000000000000000");

        result = null;
        txBuilder = makeDefaultBuilder()
                .withSender(sender)
                .withReceiver(sender)
                .withPayloadType(FileType)
                .withPayloadBody(TxFileBody.builder().withOp(ApiEnum.OpType.RegisterType).withAuthors(authors).withFile(file).build());
        try {
            result = xCube.sendTransaction(txBuilder.build()).send();
        } catch (TransactionException e) {
            assertEquals("If Allow and Disallow are set, no other addresses can be set.", e.getMessage());
        }
        assertNull(result);
    }
}
