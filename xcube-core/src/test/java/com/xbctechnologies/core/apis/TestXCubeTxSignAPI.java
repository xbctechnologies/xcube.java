package com.xbctechnologies.core.apis;

import com.xbctechnologies.core.TestParent;
import com.xbctechnologies.core.apis.dto.ApiEnum;
import com.xbctechnologies.core.apis.dto.req.tx.TxRequest;
import com.xbctechnologies.core.apis.dto.res.account.AccountBalanceResponse;
import com.xbctechnologies.core.apis.dto.res.account.AccountExportResponse;
import com.xbctechnologies.core.apis.dto.res.tx.TxCheckOriginalResponse;
import com.xbctechnologies.core.apis.dto.res.tx.TxResponse;
import com.xbctechnologies.core.apis.dto.res.tx.TxSendResponse;
import com.xbctechnologies.core.apis.dto.res.tx.TxSignResponse;
import com.xbctechnologies.core.apis.dto.xtypes.*;
import com.xbctechnologies.core.component.rest.RestHttpClient;
import com.xbctechnologies.core.order.Order;
import com.xbctechnologies.core.apis.dto.xtypes.*;
import com.xbctechnologies.core.component.rest.RestHttpConfig;
import com.xbctechnologies.core.utils.CurrencyUtil;
import com.xbctechnologies.core.utils.DateUtil;
import com.xbctechnologies.xcrypto.api.XbCipherUtil;
import com.xbctechnologies.xcrypto.api.XbSignUtil;
import com.xbctechnologies.xcrypto.ec.ECKey;
import com.xbctechnologies.xcrypto.util.ByteUtil;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.spongycastle.crypto.InvalidCipherTextException;
import org.spongycastle.util.encoders.Hex;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.security.KeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.xbctechnologies.core.utils.CurrencyUtil.CurrencyType.CoinType;
import static org.junit.Assert.*;

public class TestXCubeTxSignAPI extends TestParent {
    private XCube xCube;
    private TestXCube testXCube;
    private String targetChainId = "1T";
    private String sender = "0x9ac601f1a9c8385cb1fd794d030898168b0b617a";
    private String receiver = "0x7826d36525a285072fd8fe7cbe1597013d8d9761";
    private String testFile = "/testFile";
    private String validator = "0xd52ff6084b6dec53b74b2ac9133fe3541709fa7f";
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

        testXCube = new TestXCube(new RestHttpClient(
                RestHttpConfig.builder()
                        .withXNodeUrl(String.format("http://%s", localhost))
                        .withMaxConnection(100)
                        .withDefaultTimeout(60000)
                        .build()
        ));
    }

    private TxRequest.Builder makeDefaultBuilder() {
        return TxRequest.builder()
                .withIsSync(true)
                .withTargetChainId(targetChainId);
    }

    private AccountBalanceResponse makeAccountBalance(String totalBalance, String availableBalance, String stakingBalance, String predictionRewardBalance, String lockingBalance, CurrencyUtil.CurrencyType currencyType) {
        AccountBalanceResponse.Result accountBalanceResult = new AccountBalanceResponse.Result();
        accountBalanceResult.setTotalBalance(new BigInteger(totalBalance.replaceAll(",", "")));
        accountBalanceResult.setAvailableBalance(new BigInteger(availableBalance.replaceAll(",", "")));
        accountBalanceResult.setStakingBalance(new BigInteger(stakingBalance.replaceAll(",", "")));
        accountBalanceResult.setPredictionRewardBalance(new BigInteger(predictionRewardBalance.replaceAll(",", "")));
        accountBalanceResult.setLockingBalance(new BigInteger(lockingBalance.replaceAll(",", "")));
        accountBalanceResult.setCurrencyType(currencyType);


        AccountBalanceResponse accountBalance = new AccountBalanceResponse();
        accountBalance.setResult(accountBalanceResult);

        return accountBalance;
    }

    private String getExportKey(String passwd, AccountExportResponse exportResponse) throws UnsupportedEncodingException, NoSuchAlgorithmException, InvalidCipherTextException, KeyException {
        XbCipherUtil cipherUtil = new XbCipherUtil();
        String priKeyHexstr = cipherUtil.pbkdfDecyrptKey(passwd.getBytes("UTF-8"),
                Hex.decode(exportResponse.getResult().getCrypto().getSalt()),
                exportResponse.getResult().getCrypto().getN(), exportResponse.getResult().getCrypto().getR(),
                exportResponse.getResult().getCrypto().getP(), exportResponse.getResult().getCrypto().getDklen(),
                exportResponse.getResult().getCrypto().getCiphertext(), exportResponse.getResult().getCrypto().getMac(),
                exportResponse.getResult().getCrypto().getCipherparams().getIv());
        return priKeyHexstr;
    }

    private void sign(String priKeyHexstr, TxRequest txRequest) throws NoSuchAlgorithmException, SignatureException {
        XbSignUtil xbSignUtil = new XbSignUtil();
        // temp ECC Key Gen
        //ECKey key = xbSignUtil.ecKeyGen();
        ECKey.ECDSASignature signature = null;
        try {
            //System.out.println( Base64.toBase64String(txRequest.marshalProto(false).toByteArray()) );
            //System.out.println( JsonUtil.generateClassToJson(txRequest.marshalProto(false)) );
            try {
                if (txRequest.marshalProto(false) == null) {
                    throw new SignatureException("TxRequest.marshalProto(false) is null");
                }
            } catch (Exception e) {
                throw new SignatureException("TxRequest.marshalProto(false) is null");
            }
            signature = xbSignUtil.sign(priKeyHexstr, txRequest.marshalProto(false).toByteArray());
            txRequest.setR(signature.r);
            txRequest.setS(signature.s);
            txRequest.setV(signature.getVInt());
            /*System.out.println("r: " + signature.r);
            System.out.println("s: " + signature.s);
            System.out.println("v: " +signature.getVInt());*/
            /*Byte b = new Byte(  txRequest.getV()+"" );
            ECKey.ECDSASignature verifysign = ECKey.ECDSASignature.fromComponents(txRequest.getR().toByteArray(), txRequest.getS().toByteArray(), b.byteValue());
            ECKey key = xbSignUtil.getRecoveryKey(verifysign, txRequest.marshalProto(false).toByteArray());
            System.out.println("key addr " + Hex.toHexString(key.getAddress()) );*/
        } catch (NoSuchAlgorithmException e) {
            throw e;
        } catch (SignatureException e) {
            throw e;
        }
    }

    @Test
    @Order(order = 1)
    public void commonSignTx() throws Exception {
        /*
            CommonType(1),
         */
        // 1. client time setting
        //BigInteger time = new BigInteger( "1539223260544001");
        BigInteger time = new BigInteger(String.valueOf(DateUtil.getMicroSecond()));
        //System.out.println("currenttime: " + time);

        // 2. Common Type Tx Create
        String local_sender = sender;
        TxRequest.Builder builder = makeDefaultBuilder();
        builder.withSender(local_sender);
        builder.withReceiver(receiver);
        builder.withPayloadType(ApiEnum.PayloadType.CommonType);
        builder.withFee(CurrencyUtil.generateXTO(CurrencyUtil.CurrencyType.CoinType, 1));
        builder.withAmount(CurrencyUtil.generateXTO(CurrencyUtil.CurrencyType.CoinType, 10));
        builder.withTime(time);
        builder.withPayloadBody(new TxCommonBody("test네트웍1"));
        TxRequest txRequest = builder.build();

        // 3. Account Key Export ( temp test ) and Account exist check
        String passphrase = "1111";
        AccountExportResponse exportResponse = testXCube.exportAccount(null, local_sender, passphrase).send();
        assertNull(exportResponse.getError());
        assertNotNull(exportResponse.getResult());
        // sender Account Address, export Key Account Address same check.
        Assert.assertEquals(ByteUtil.toNoPriFixHexString(local_sender.toLowerCase()), exportResponse.getResult().getAddress().toLowerCase());

        // private Key export
        String priKeyHexstr = getExportKey(passphrase, exportResponse);
        // 4. Tx Sign
        try {
            sign(priKeyHexstr, txRequest);
        } catch (NoSuchAlgorithmException e) {
            throw e;
        } catch (SignatureException e) {
            throw e;
        }
        assertNotNull(txRequest.getR());
        assertNotNull(txRequest.getS());
        assertNotNull(txRequest.getV());

        // 5. CommonType Tx Send
        TxSendResponse txSendResponse = xCube.sendTransaction(txRequest).send();
        // server response sign check result error case
        if (txSendResponse.getError() != null && txSendResponse.getError().getCode() == 323) {
            fail();
        }
        assertNotNull(txSendResponse.getResult());

        // 6. getTransaction tx exist check
        TxResponse txRes = xCube.getTransaction(null, targetChainId, ByteUtil.toNoPriFixHexString(txSendResponse.getResult().getTxHash())).send();
        assertNull(txRes.getError());
        assertNotNull(txRes.getResult());

        // 7. Type check
        Assert.assertEquals(txRes.getResult().getPayloadType(), ApiEnum.PayloadType.CommonType);

        // 8. pubkey and message same check
        Byte b = new Byte(txRes.getResult().getV());
        ECKey.ECDSASignature verifysign = ECKey.ECDSASignature.fromComponents(txRes.getResult().getR().toByteArray(), txRes.getResult().getS().toByteArray(), b.byteValue());
        byte[] resMsg = txRes.generateTxRequest(true).marshalProto(false).toByteArray();
        XbSignUtil xbSignUtil = new XbSignUtil();
        ECKey recoverkey = xbSignUtil.getRecoveryKey(verifysign, resMsg);
        assertEquals(ByteUtil.toNoPriFixHexString(txRes.getResult().getSender()), Hex.toHexString(recoverkey.getAddress()));

        // 9. ecdsa/sha256 sign verify
        try {
            boolean isVerify = xbSignUtil.verify(verifysign, resMsg);
            assertTrue(isVerify);
        } catch (NoSuchAlgorithmException e) {
            throw e;

        }
        // 10. tx Hash same Check
        Assert.assertEquals(txSendResponse.getResult().getTxHash(), txRes.getResult().getTxHash());
    }

    @Test
    @Order(order = 2)
    public void fileSignTx() throws Exception {
         /*
            FileType(2),
        */
        // 1. client time setting
        //BigInteger time = new BigInteger( "1539223260544001");
        BigInteger time = new BigInteger(String.valueOf(DateUtil.getMicroSecond()));
        // 2. File Type Tx Create
        File file = new File(this.getClass().getResource(testFile).getFile());
        TxFileBody.Builder txFileBuilder = new TxFileBody().builder()
                .withOp(ApiEnum.OpType.RegisterType)
                .withFile(file)
                .withInfo("xchain auth data");

        String local_sender = sender;
        TxRequest txRequest = makeDefaultBuilder()
                .withSender(local_sender)
                .withReceiver(local_sender)
                .withPayloadType(ApiEnum.PayloadType.FileType)
                .withFee(CurrencyUtil.generateXTO(CurrencyUtil.CurrencyType.CoinType, 1))
                .withAmount(CurrencyUtil.generateXTO(CurrencyUtil.CurrencyType.CoinType, 0))
                .withTime(time)
                .withPayloadBody(txFileBuilder.withAuthors(Arrays.asList(sender)).build())
                .build();

        // 3. Account Key Export ( temp test ) and Account exist check
        String passphrase = "1111";
        AccountExportResponse exportResponse = testXCube.exportAccount(null, local_sender, passphrase).send();
        assertNull(exportResponse.getError());
        assertNotNull(exportResponse.getResult());
        // sender Account Address, export Key Account Address same check.
        Assert.assertEquals(ByteUtil.toNoPriFixHexString(local_sender.toLowerCase()), exportResponse.getResult().getAddress().toLowerCase());

        // private Key export
        String priKeyHexstr = getExportKey(passphrase, exportResponse);
        // 4. Tx Sign
        try {
            sign(priKeyHexstr, txRequest);
        } catch (NoSuchAlgorithmException e) {
            throw e;
        } catch (SignatureException e) {
            throw e;
        }
        // R, S, V null check
        assertNotNull(txRequest.getR());
        assertNotNull(txRequest.getS());
        assertNotNull(txRequest.getV());

        // 5. FileType Tx Send
        TxSendResponse txSendResponse = xCube.sendTransaction(txRequest).send();
        // server response sign check result error case
        if (txSendResponse.getError() != null && txSendResponse.getError().getCode() == 323) {
            fail();
        }
        assertNull(txSendResponse.getError());
        assertNotNull(txSendResponse.getResult());

        // 6. getTransaction tx exist check
        TxResponse txRes = xCube.getTransaction(null, targetChainId, ByteUtil.toNoPriFixHexString(txSendResponse.getResult().getTxHash())).send();
        assertNull(txRes.getError());
        assertNotNull(txRes.getResult());

        // 7. Type check
        Assert.assertEquals(txRes.getResult().getPayloadType(), ApiEnum.PayloadType.FileType);

        // 8. sender Account Address, response (recover public Key) Account Address same check.
        Byte b = new Byte(txRes.getResult().getV());
        ECKey.ECDSASignature verifysign = ECKey.ECDSASignature.fromComponents(txRes.getResult().getR().toByteArray(), txRes.getResult().getS().toByteArray(), b.byteValue());
        byte[] resMsg = txRes.generateTxRequest(true).marshalProto(true).toByteArray();
        XbSignUtil xbSignUtil = new XbSignUtil();
        ECKey recoverkey = xbSignUtil.getRecoveryKey(verifysign, resMsg);
        assertEquals(ByteUtil.toNoPriFixHexString(txRes.getResult().getSender()), Hex.toHexString(recoverkey.getAddress()));

        // 9. ecdsa/sha256 sender sign verify
        try {
            boolean isVerify = xbSignUtil.verify(verifysign, resMsg);
            assertTrue(isVerify);
        } catch (NoSuchAlgorithmException e) {
            throw e;
        }
        // 10. tx Hash same Check
        Assert.assertEquals(txSendResponse.getResult().getTxHash(), txRes.getResult().getTxHash());

        txRequest = makeDefaultBuilder()
                .withSender(local_sender)
                .withReceiver(receiver)
                .withPayloadType(ApiEnum.PayloadType.CommonType)
                .withFee(CurrencyUtil.generateXTO(CurrencyUtil.CurrencyType.CoinType, 1))
                .withAmount(CurrencyUtil.generateXTO(CurrencyUtil.CurrencyType.CoinType, 10))
                .withPayloadBody(new TxCommonBody("test"))
                .withTime(new BigInteger(String.valueOf(DateUtil.getMicroSecond())))
                .build();

        txSendResponse = xCube.sendTransaction(txRequest).send();
        assertNull(txSendResponse.getError());
        assertNotNull(txSendResponse.getResult());

        System.out.println("txRes.getResult().getDataAccount(): " + txRes.getResult().getDataAccount());
        //TxCheckOriginalResponse txcheckRes = xCube.checkOriginal(null, targetChainId, "0x99dbb8e77785e25f943320037466acd5b8b538f3", file).send();
        TxCheckOriginalResponse txcheckRes = xCube.checkOriginal(null, targetChainId, txRes.getResult().getDataAccount(), file).send();
        assertNull(txcheckRes.getError());
        assertNotNull(txcheckRes.getResult());
    }

    @Test
    @Order(order = 3)
    public void bondingSignTx() throws Exception {
         /*
            BondingType(3),
        */
        // 1. client time setting
        BigInteger time = new BigInteger(String.valueOf(DateUtil.getMicroSecond()));

        // 2. Bonding Type Tx Create
        String local_sender = sender;
        TxRequest txRequest = makeDefaultBuilder()
                .withSender(local_sender)
                .withReceiver(local_sender)
                .withPayloadType(ApiEnum.PayloadType.BondingType)
                .withFee(CurrencyUtil.generateXTO(CoinType, 10000))
                .withAmount(CurrencyUtil.generateXTO(CoinType, 1))
                .withTime(time)
                .withPayloadBody(new TxBondingBody())
                .build();

        // 3. Account Key Export ( temp test ) and Account exist check
        String passphrase = "1111";
        AccountExportResponse exportResponse = testXCube.exportAccount(null, local_sender, passphrase).send();
        assertNull(exportResponse.getError());
        assertNotNull(exportResponse.getResult());
        // sender Account Address, export Key Account Address same check.
        Assert.assertEquals(ByteUtil.toNoPriFixHexString(local_sender).toLowerCase(), exportResponse.getResult().getAddress().toLowerCase());
        // private Key export
        String priKeyHexstr = getExportKey(passphrase, exportResponse);
        // 4. Tx Sign
        try {
            sign(priKeyHexstr, txRequest);
        } catch (NoSuchAlgorithmException e) {
            throw e;
        } catch (SignatureException e) {
            throw e;
        }
        // R, S, V null check
        assertNotNull(txRequest.getR());
        assertNotNull(txRequest.getS());
        assertNotNull(txRequest.getV());

        // 5. BondingType Tx Send
        TxSendResponse sendResponse = xCube.bonding(txRequest).send();
        // server response sign check result error case
        if (sendResponse.getError() != null && sendResponse.getError().getCode() == 323) {
            fail();
        }
        assertNull(sendResponse.getError());
        assertNotNull(sendResponse.getResult());

        // 6. getTransaction tx exist check
        TxResponse txRes = xCube.getTransaction(null, targetChainId, ByteUtil.toNoPriFixHexString(sendResponse.getResult().getTxHash())).send();
        assertNull(txRes.getError());
        assertNotNull(txRes.getResult());

        // 7. Type check
        Assert.assertEquals(txRes.getResult().getPayloadType(), ApiEnum.PayloadType.BondingType);

        // 8. sender Account Address, response (recover public Key) Account Address same check.
        Byte b = new Byte(txRes.getResult().getV());
        ECKey.ECDSASignature verifysign = ECKey.ECDSASignature.fromComponents(txRes.getResult().getR().toByteArray(), txRes.getResult().getS().toByteArray(), b.byteValue());
        byte[] resMsg = txRes.generateTxRequest(true).marshalProto(false).toByteArray();
        XbSignUtil xbSignUtil = new XbSignUtil();
        ECKey recoverkey = xbSignUtil.getRecoveryKey(verifysign, resMsg);
        assertEquals(ByteUtil.toNoPriFixHexString(txRes.getResult().getSender()), Hex.toHexString(recoverkey.getAddress()));

        // 9. ecdsa/sha256 sender sign verify
        try {
            boolean isVerify = xbSignUtil.verify(verifysign, resMsg);
            assertTrue(isVerify);
        } catch (NoSuchAlgorithmException e) {
            throw e;
        }
        // 10. tx Hash same Check
        Assert.assertEquals(sendResponse.getResult().getTxHash(), txRes.getResult().getTxHash());
    }

    @Test
    @Order(order = 4)
    public void unbondingSignTx() throws Exception {
        /*
         UnbondingType(4),
         */
        // 1. client time setting
        BigInteger time = new BigInteger(String.valueOf(DateUtil.getMicroSecond()));
        // 2. Unbonding Type Tx Create
        String local_sender = validator;
        TxRequest txRequest = makeDefaultBuilder()
                .withSender(local_sender)
                .withReceiver(local_sender)
                .withPayloadType(ApiEnum.PayloadType.UnbondingType)
                .withFee(CurrencyUtil.generateXTO(CurrencyUtil.CurrencyType.CoinType, 1))
                .withAmount(CurrencyUtil.generateXTO(CoinType, 1))
                .withTime(time)
                .withPayloadBody(new TxUnbondingBody())
                .build();

        // 3. Account Key Export ( temp test ) and Account exist check
        String passwd = "1111";
        AccountExportResponse exportResponse = testXCube.exportAccount(null, local_sender, passwd).send();
        assertNull(exportResponse.getError());
        assertNotNull(exportResponse.getResult());
        // sender Account Address, export Key Account Address same check.
        Assert.assertEquals(ByteUtil.toNoPriFixHexString(local_sender).toLowerCase(), exportResponse.getResult().getAddress().toLowerCase());
        // private Key export
        String priKeyHexstr = getExportKey(passwd, exportResponse);
        // 4. Tx Sign
        try {
            sign(priKeyHexstr, txRequest);
        } catch (NoSuchAlgorithmException e) {
            throw e;
        } catch (SignatureException e) {

            throw e;
        }
        // R, S, V null check
        assertNotNull(txRequest.getR());
        assertNotNull(txRequest.getS());
        assertNotNull(txRequest.getV());

        // 5. UnbondingType Tx Send
        TxSendResponse sendResponse = xCube.bonding(txRequest).send();
        // server response sign check result error case
        if (sendResponse.getError() != null && sendResponse.getError().getCode() == 323) {
            fail();
        }
        assertNull(sendResponse.getError());
        assertNotNull(sendResponse.getResult());

        // 6. getTransaction tx exist check
        TxResponse txRes = xCube.getTransaction(null, targetChainId, ByteUtil.toNoPriFixHexString(sendResponse.getResult().getTxHash())).send();
        assertNull(txRes.getError());
        assertNotNull(txRes.getResult());

        // 7. Type check
        Assert.assertEquals(txRes.getResult().getPayloadType(), ApiEnum.PayloadType.UnbondingType);

        // 8. sender Account Address, response (recover public Key) Account Address same check.
        Byte b = new Byte(txRes.getResult().getV());
        ECKey.ECDSASignature verifysign = ECKey.ECDSASignature.fromComponents(txRes.getResult().getR().toByteArray(), txRes.getResult().getS().toByteArray(), b.byteValue());
        byte[] resMsg = txRes.generateTxRequest(true).marshalProto(false).toByteArray();
        XbSignUtil xbSignUtil = new XbSignUtil();
        ECKey recoverkey = xbSignUtil.getRecoveryKey(verifysign, resMsg);
        assertEquals(ByteUtil.toNoPriFixHexString(txRes.getResult().getSender()), Hex.toHexString(recoverkey.getAddress()));

        // 9. ecdsa/sha256 sender sign verify
        try {
            boolean isVerify = xbSignUtil.verify(verifysign, resMsg);
            assertTrue(isVerify);
        } catch (NoSuchAlgorithmException e) {
            throw e;
        }
        // 10. tx Hash same Check
        Assert.assertEquals(sendResponse.getResult().getTxHash(), txRes.getResult().getTxHash());
    }

    @Test
    @Order(order = 5)
    public void delegatingSignTx() throws Exception {
        /*
        DelegatingType(5),
         */
        // 1. client time setting
        BigInteger time = new BigInteger(String.valueOf(DateUtil.getMicroSecond()));
        // 2. Delegating Type Tx Create
        String local_sender = validator;
        TxRequest txRequest = makeDefaultBuilder()
                .withSender(local_sender)
                .withReceiver(local_sender)
                .withPayloadType(ApiEnum.PayloadType.DelegatingType)
                .withFee(CurrencyUtil.generateXTO(CoinType, 1))
                .withAmount(CurrencyUtil.generateXTO(CoinType, 10))
                .withTime(time)
                .withPayloadBody(new TxDelegatingBody())
                .build();

        // 3. Account Key Export ( temp test ) and Account exist check
        String passwd = "1111";
        AccountExportResponse exportResponse = testXCube.exportAccount(null, local_sender, passwd).send();
        assertNull(exportResponse.getError());
        assertNotNull(exportResponse.getResult());
        // sender Account Address, export Key Account Address same check.
        Assert.assertEquals(ByteUtil.toNoPriFixHexString(local_sender).toLowerCase(), exportResponse.getResult().getAddress().toLowerCase());
        // private Key export
        String priKeyHexstr = getExportKey(passwd, exportResponse);
        // 4. Tx Sign
        try {
            sign(priKeyHexstr, txRequest);
        } catch (NoSuchAlgorithmException e) {
            throw e;
        } catch (SignatureException e) {
            throw e;
        }
        // R, S, V null check
        assertNotNull(txRequest.getR());
        assertNotNull(txRequest.getS());
        assertNotNull(txRequest.getV());

        // 5. DelegatingType Tx Send
        TxSendResponse sendResponse = xCube.delegating(txRequest).send();
        // server response sign check result error case
        if (sendResponse.getError() != null && sendResponse.getError().getCode() == 323) {
            fail();
        }
        assertNull(sendResponse.getError());
        assertNotNull(sendResponse.getResult());

        // 6. getTransaction tx exist check
        TxResponse txRes = xCube.getTransaction(null, targetChainId, ByteUtil.toNoPriFixHexString(sendResponse.getResult().getTxHash())).send();
        assertNull(txRes.getError());
        assertNotNull(txRes.getResult());

        // 7. Type check
        Assert.assertEquals(txRes.getResult().getPayloadType(), ApiEnum.PayloadType.DelegatingType);

        // 8. sender Account Address, response (recover public Key) Account Address same check.
        Byte b = new Byte(txRes.getResult().getV());
        ECKey.ECDSASignature verifysign = ECKey.ECDSASignature.fromComponents(txRes.getResult().getR().toByteArray(), txRes.getResult().getS().toByteArray(), b.byteValue());
        byte[] resMsg = txRes.generateTxRequest(true).marshalProto(false).toByteArray();
        XbSignUtil xbSignUtil = new XbSignUtil();
        ECKey recoverkey = xbSignUtil.getRecoveryKey(verifysign, resMsg);
        assertEquals(ByteUtil.toNoPriFixHexString(txRes.getResult().getSender()), Hex.toHexString(recoverkey.getAddress()));

        // 9. ecdsa/sha256 sender sign verify
        try {
            boolean isVerify = xbSignUtil.verify(verifysign, resMsg);
            assertTrue(isVerify);
        } catch (NoSuchAlgorithmException e) {
            throw e;
        }
        // 10. tx Hash same Check
        Assert.assertEquals(sendResponse.getResult().getTxHash(), txRes.getResult().getTxHash());
    }

    @Test
    @Order(order = 6)
    public void undelegatingSignTx() throws Exception {
        /*
        UndelegatingType(6),
         */
        // 1. client time setting
        BigInteger time = new BigInteger(String.valueOf(DateUtil.getMicroSecond()));
        // 2. Undelegating Type Tx Create
        String local_sender = validator;
        TxRequest txRequest = makeDefaultBuilder()
                .withSender(local_sender)
                .withReceiver(local_sender)
                .withPayloadType(ApiEnum.PayloadType.UndelegatingType)
                .withFee(CurrencyUtil.generateXTO(CoinType, 1))
                .withAmount(CurrencyUtil.generateXTO(CoinType, 10))
                .withTime(time)
                .withPayloadBody(new TxUndelegatingBody())
                .build();

        // 3. Account Key Export ( temp test ) and Account exist check
        String passwd = "1111";
        AccountExportResponse exportResponse = testXCube.exportAccount(null, local_sender, passwd).send();
        assertNull(exportResponse.getError());
        assertNotNull(exportResponse.getResult());
        // sender Account Address, export Key Account Address same check.
        Assert.assertEquals(ByteUtil.toNoPriFixHexString(local_sender).toLowerCase(), exportResponse.getResult().getAddress().toLowerCase());
        // private Key export
        String priKeyHexstr = getExportKey(passwd, exportResponse);
        // 4. Tx Sign
        try {
            sign(priKeyHexstr, txRequest);
        } catch (NoSuchAlgorithmException e) {
            throw e;
        } catch (SignatureException e) {
            throw e;
        }
        // R, S, V null check
        assertNotNull(txRequest.getR());
        assertNotNull(txRequest.getS());
        assertNotNull(txRequest.getV());

        // 5. UndelegatingType Tx Send
        TxSendResponse sendResponse = xCube.undelegating(txRequest).send();
        // server response sign check result error case
        if (sendResponse.getError() != null && sendResponse.getError().getCode() == 323) {
            fail();
        }
        assertNull(sendResponse.getError());
        assertNotNull(sendResponse.getResult());

        // 6. getTransaction tx exist check
        TxResponse txRes = xCube.getTransaction(null, targetChainId, ByteUtil.toNoPriFixHexString(sendResponse.getResult().getTxHash())).send();
        assertNull(txRes.getError());
        assertNotNull(txRes.getResult());

        // 7. Type check
        Assert.assertEquals(txRes.getResult().getPayloadType(), ApiEnum.PayloadType.UndelegatingType);

        // 8. pubkey and message same check
        Byte b = new Byte(txRes.getResult().getV());
        ECKey.ECDSASignature verifysign = ECKey.ECDSASignature.fromComponents(txRes.getResult().getR().toByteArray(), txRes.getResult().getS().toByteArray(), b.byteValue());
        byte[] resMsg = txRes.generateTxRequest(true).marshalProto(false).toByteArray();
        XbSignUtil xbSignUtil = new XbSignUtil();
        ECKey recoverkey = xbSignUtil.getRecoveryKey(verifysign, resMsg);
        assertEquals(ByteUtil.toNoPriFixHexString(txRes.getResult().getSender()), Hex.toHexString(recoverkey.getAddress()));

        // 9. ecdsa/sha256 sender sign verify
        try {
            boolean isVerify = xbSignUtil.verify(verifysign, resMsg);
            assertTrue(isVerify);
        } catch (NoSuchAlgorithmException e) {
            throw e;
        }
        // 10. tx Hash same Check
        Assert.assertEquals(sendResponse.getResult().getTxHash(), txRes.getResult().getTxHash());
    }

    @Test
    @Order(order = 7)
    public void grProposalSignTx() throws Exception {
        /*
        GRProposalType(7),
         */
        // 1. client time setting
        BigInteger time = new BigInteger(String.valueOf(DateUtil.getMicroSecond()));
        // 2. GRProposalType Type Tx Create
        String local_sender = validator;
        TxRequest txRequest = makeDefaultBuilder()
                .withSender(local_sender)
                .withReceiver(local_sender)
                .withPayloadType(ApiEnum.PayloadType.GRProposalType)
                .withFee(CurrencyUtil.generateXTO(CoinType, 100))
                .withAmount(CurrencyUtil.generateXTO(CoinType, 0))
                .withTime(time)
                .withPayloadBody(
                        TxGRProposalBody.builder()
                                .withCurrentReflection(new TxGRProposalBody.CurrentReflection(1, 2))
                                .build()
                )
                .build();

        // 3. Account Key Export ( temp test ) and Account exist check
        String passwd = "1111";
        AccountExportResponse exportResponse = testXCube.exportAccount(null, local_sender, passwd).send();
        assertNull(exportResponse.getError());
        assertNotNull(exportResponse.getResult());
        // sender Account Address, export Key Account Address same check.
        Assert.assertEquals(ByteUtil.toNoPriFixHexString(local_sender).toLowerCase(), exportResponse.getResult().getAddress().toLowerCase());
        // private Key export
        String priKeyHexstr = getExportKey(passwd, exportResponse);
        // 4. Tx Sign
        try {
            sign(priKeyHexstr, txRequest);
        } catch (NoSuchAlgorithmException e) {
            throw e;
        } catch (SignatureException e) {
            throw e;
        }
        // R, S, V null check
        assertNotNull(txRequest.getR());
        assertNotNull(txRequest.getS());
        assertNotNull(txRequest.getV());

        // 5. GRProposalType Tx Send
        TxSendResponse sendResponse = xCube.sendTransaction(txRequest).send();
        // server response sign check result error case
        if (sendResponse.getError() != null && sendResponse.getError().getCode() == 323) {
            fail();
        }
        assertNull(sendResponse.getError());
        assertNotNull(sendResponse.getResult());

        // 6. getTransaction tx exist check
        TxResponse txRes = xCube.getTransaction(null, targetChainId, ByteUtil.toNoPriFixHexString(sendResponse.getResult().getTxHash())).send();
        assertNull(txRes.getError());
        assertNotNull(txRes.getResult());

        // 7. Type check
        Assert.assertEquals(txRes.getResult().getPayloadType(), ApiEnum.PayloadType.GRProposalType);

        // 8. pubkey and message same check
        Byte b = new Byte(txRes.getResult().getV());
        ECKey.ECDSASignature verifysign = ECKey.ECDSASignature.fromComponents(txRes.getResult().getR().toByteArray(), txRes.getResult().getS().toByteArray(), b.byteValue());
        byte[] resMsg = txRes.generateTxRequest(true).marshalProto(false).toByteArray();
        XbSignUtil xbSignUtil = new XbSignUtil();
        ECKey recoverkey = xbSignUtil.getRecoveryKey(verifysign, resMsg);
        assertEquals(ByteUtil.toNoPriFixHexString(txRes.getResult().getSender()), Hex.toHexString(recoverkey.getAddress()));

        // 9. ecdsa/sha256 sender sign verify
        try {
            boolean isVerify = xbSignUtil.verify(verifysign, resMsg);
            assertTrue(isVerify);
        } catch (NoSuchAlgorithmException e) {
            throw e;
        }
        // 10. tx Hash same Check
        Assert.assertEquals(sendResponse.getResult().getTxHash(), txRes.getResult().getTxHash());
    }

    public void txCommonRequest() throws Exception {
        //https://developers.google.com/protocol-buffers/docs/javatutorial
        TxRequest txRequest = makeDefaultBuilder()
                .withSender(validator)
                .withReceiver(receiver)
                .withPayloadType(ApiEnum.PayloadType.CommonType)
                .withFee(fee)
                .withAmount(amount)
                .withPayloadBody(new TxCommonBody("test"))
                .build();

        TxSendResponse txSendResponse = xCube.sendTransaction(txRequest).send();
        assertNull(txSendResponse.getError());
        assertNotNull(txSendResponse.getResult());
    }

    public void txGRProposalRequest() throws Exception {
        //Proposal 할수있는 조건을 충족하기 위해
        for (int i = 0; i < 25; i++) {
            txCommonRequest();
        }

        TxGRProposalBody txGRProposalBody = TxGRProposalBody.builder()
                .withCurrentReflection(new TxGRProposalBody.CurrentReflection(1, 2))
                .build();

        TxRequest txRequest = makeDefaultBuilder()
                .withSender(validator)
                .withReceiver(validator)
                .withFee(fee)
                .withAmount(null)
                .withPayloadType(ApiEnum.PayloadType.GRProposalType)
                .withPayloadBody(txGRProposalBody)
                .build();

        TxSendResponse txSendResponse = xCube.sendTransaction(txRequest).send();
        assertNull(txSendResponse.getError());
        assertNotNull(txSendResponse.getResult());
    }

    @Test
    @Order(order = 8)
    public void grVoteTypeSignTx() throws Exception {
        txGRProposalRequest();
        /*
        GRVoteType(8),
         */
        // 1. client time setting
        BigInteger time = new BigInteger(String.valueOf(DateUtil.getMicroSecond()));
        // 2. GRVoteType Type Tx Create
        String local_sender = validator;
        TxRequest txRequest = makeDefaultBuilder()
                .withSender(local_sender)
                .withReceiver(local_sender)
                .withPayloadType(ApiEnum.PayloadType.GRVoteType)
                .withFee(CurrencyUtil.generateXTO(CoinType, 1))
                .withAmount(CurrencyUtil.generateXTO(CoinType, 0))
                .withTime(time)
                .withPayloadBody(new TxGRVoteBody(true))
                .build();

        // 3. Account Key Export ( temp test ) and Account exist check
        String passwd = "1111";
        AccountExportResponse exportResponse = testXCube.exportAccount(null, local_sender, passwd).send();
        assertNull(exportResponse.getError());
        assertNotNull(exportResponse.getResult());
        // sender Account Address, export Key Account Address same check.
        Assert.assertEquals(ByteUtil.toNoPriFixHexString(local_sender).toLowerCase(), exportResponse.getResult().getAddress().toLowerCase());
        // private Key export
        String priKeyHexstr = getExportKey(passwd, exportResponse);
        // 4. Tx Sign
        try {
            sign(priKeyHexstr, txRequest);
        } catch (NoSuchAlgorithmException e) {
            throw e;
        } catch (SignatureException e) {
            throw e;
        }
        // R, S, V null check
        assertNotNull(txRequest.getR());
        assertNotNull(txRequest.getS());
        assertNotNull(txRequest.getV());

        // 5. GRVoteType Tx Send
        TxSendResponse sendResponse = xCube.sendTransaction(txRequest).send();
        // server response sign check result error case
        if (sendResponse.getError() != null && sendResponse.getError().getCode() == 323) {
            fail();
        }
        assertNull(sendResponse.getError());
        assertNotNull(sendResponse.getResult());

        // 6. getTransaction tx exist check
        TxResponse txRes = xCube.getTransaction(null, targetChainId, sendResponse.getResult().getTxHash()).send();
        assertNull(txRes.getError());
        assertNotNull(txRes.getResult());

        // 7. Type check
        Assert.assertEquals(txRes.getResult().getPayloadType(), ApiEnum.PayloadType.GRVoteType);

        // 8. pubkey and message same check
        Byte b = new Byte(txRes.getResult().getV());
        ECKey.ECDSASignature verifysign = ECKey.ECDSASignature.fromComponents(txRes.getResult().getR().toByteArray(), txRes.getResult().getS().toByteArray(), b.byteValue());
        byte[] resMsg = txRes.generateTxRequest(true).marshalProto(false).toByteArray();
        XbSignUtil xbSignUtil = new XbSignUtil();
        ECKey recoverkey = xbSignUtil.getRecoveryKey(verifysign, resMsg);
        assertEquals(ByteUtil.toNoPriFixHexString(txRes.getResult().getSender()), Hex.toHexString(recoverkey.getAddress()));

        // 9. ecdsa/sha256 sender sign verify
        try {
            boolean isVerify = xbSignUtil.verify(verifysign, resMsg);
            assertTrue(isVerify);
        } catch (NoSuchAlgorithmException e) {
            throw e;
        }
        // 10. tx Hash same Check
        Assert.assertEquals(sendResponse.getResult().getTxHash(), txRes.getResult().getTxHash());
    }

    @Test
    @Order(order = 9)
    public void recoverValidatorSignTx() throws Exception {
        /*
        RecoverValidatorType(9),
         */
        // 1. client time setting
        BigInteger time = new BigInteger(String.valueOf(DateUtil.getMicroSecond()));
        // 2. RecoverValidatorType Type Tx Create
        String local_sender = validator;
        TxRequest txRequest = makeDefaultBuilder()
                .withSender(local_sender)
                .withReceiver(local_sender)
                .withPayloadType(ApiEnum.PayloadType.RecoverValidatorType)
                .withFee(CurrencyUtil.generateXTO(CoinType, 1))
                .withAmount(CurrencyUtil.generateXTO(CoinType, 0))
                .withTime(time)
                .withPayloadBody(new TxRecoverBody())
                .build();

        // 3. Account Key Export ( temp test ) and Account exist check
        String passwd = "1111";
        AccountExportResponse exportResponse = testXCube.exportAccount(null, local_sender, passwd).send();
        assertNull(exportResponse.getError());
        assertNotNull(exportResponse.getResult());
        // sender Account Address, export Key Account Address same check.
        Assert.assertEquals(ByteUtil.toNoPriFixHexString(local_sender).toLowerCase(), exportResponse.getResult().getAddress().toLowerCase());
        // private Key export
        String priKeyHexstr = getExportKey(passwd, exportResponse);
        // 4. Tx Sign
        try {
            sign(priKeyHexstr, txRequest);
        } catch (NoSuchAlgorithmException e) {
            throw e;
        } catch (SignatureException e) {
            throw e;
        }
        // R, S, V null check
        assertNotNull(txRequest.getR());
        assertNotNull(txRequest.getS());
        assertNotNull(txRequest.getV());

        // 5. RecoverValidatorType Tx Send
        TxSendResponse sendResponse = xCube.sendTransaction(txRequest).send();
        // server response sign check result error case
        if (sendResponse.getError() != null && sendResponse.getError().getCode() == 323) {
            fail();
        }
        assertNull(sendResponse.getError());
        assertNotNull(sendResponse.getResult());

        // 6. getTransaction tx exist check
        TxResponse txRes = xCube.getTransaction(null, targetChainId, ByteUtil.toNoPriFixHexString(sendResponse.getResult().getTxHash())).send();
        assertNull(txRes.getError());
        assertNotNull(txRes.getResult());

        // 7. Type check
        Assert.assertEquals(txRes.getResult().getPayloadType(), ApiEnum.PayloadType.RecoverValidatorType);

        // 8. pubkey and message same check
        Byte b = new Byte(txRes.getResult().getV());
        ECKey.ECDSASignature verifysign = ECKey.ECDSASignature.fromComponents(txRes.getResult().getR().toByteArray(), txRes.getResult().getS().toByteArray(), b.byteValue());
        byte[] resMsg = txRes.generateTxRequest(true).marshalProto(false).toByteArray();
        XbSignUtil xbSignUtil = new XbSignUtil();
        ECKey recoverkey = xbSignUtil.getRecoveryKey(verifysign, resMsg);
        assertEquals(ByteUtil.toNoPriFixHexString(txRes.getResult().getSender()), Hex.toHexString(recoverkey.getAddress()));

        // 9. ecdsa/sha256 sender sign verify
        try {
            boolean isVerify = xbSignUtil.verify(verifysign, resMsg);
            assertTrue(isVerify);
        } catch (NoSuchAlgorithmException e) {
            throw e;
        }
        // 10. tx Hash same Check
        Assert.assertEquals(sendResponse.getResult().getTxHash(), txRes.getResult().getTxHash());
    }

    @Test
    @Order(order = 10)
    public void makeXChainSignTx() throws Exception {
       /*
       MakeXChainType(10);
        */
        // 1. client time setting
        BigInteger time = new BigInteger(String.valueOf(DateUtil.getMicroSecond()));

        List<TxMakeXChainBody.AssetHolder> assetHolders = new ArrayList<>();
        assetHolders.add(new TxMakeXChainBody.AssetHolder(sender, fee));
        assetHolders.add(new TxMakeXChainBody.AssetHolder(receiver, fee));

        List<TxMakeXChainBody.Seed> seeds = new ArrayList<>();
        seeds.add(new TxMakeXChainBody.Seed("4bd2f7c0e58a868c6225cda8afcd2735ce9df4dd", "10.0.2.15", 9000));

        List<TxMakeXChainBody.Validator> validators = new ArrayList<>();
        validators.add(new TxMakeXChainBody.Validator(new TxMakeXChainBody.Validator.PubKey("xblock/PubKeySecp256k1", "AwdD/RUVKuvKWxn2ybEVPJ/R0eKeBjj1BNk/OcUZvG/+"), "120",
                defaultCompanyName,
                defaultCompanyDesc,
                defaultCompanyUrl,
                defaultCompanyLogoUrl,
                defaultCompanyLat,
                defaultCompanyLon));
        validators.add(new TxMakeXChainBody.Validator(new TxMakeXChainBody.Validator.PubKey("xblock/PubKeySecp256k1", "AiapKeCawZDErZJKSqzGToBaR+6zy+ViU9d/S/noTaFB"), "100",
                defaultCompanyName,
                defaultCompanyDesc,
                defaultCompanyUrl,
                defaultCompanyLogoUrl,
                defaultCompanyLat,
                defaultCompanyLon));

        TxMakeXChainBody txMakeXChainBody = TxMakeXChainBody.builder()
                .withDepth(10)
                .withHasAsset(true)
                .withNonExchangeChain(true)
                .withAirdropRate(100)
                .withAssetHolders(assetHolders)
                .withSeeds(seeds)
                .withValidators(validators)
                .build();

        // 2. MakeXChainType Type Tx Create
        String local_sender = validator;
        TxRequest txRequest = makeDefaultBuilder()
                .withSender(local_sender)
                .withReceiver(local_sender)
                .withPayloadType(ApiEnum.PayloadType.MakeXChainType)
                .withFee(CurrencyUtil.generateXTO(CoinType, 1000))
                .withAmount(CurrencyUtil.generateXTO(CoinType, 0))
                .withTime(time)
                .withPayloadType(ApiEnum.PayloadType.MakeXChainType)
                .withPayloadBody(txMakeXChainBody)
                .build();

        // 3. Account Key Export ( temp test ) and Account exist check
        String passwd = "1111";
        AccountExportResponse exportResponse = testXCube.exportAccount(null, local_sender, passwd).send();
        assertNull(exportResponse.getError());
        assertNotNull(exportResponse.getResult());
        // sender Account Address, export Key Account Address same check.
        Assert.assertEquals(ByteUtil.toNoPriFixHexString(local_sender).toLowerCase(), exportResponse.getResult().getAddress().toLowerCase());
        // private Key export
        String priKeyHexstr = getExportKey(passwd, exportResponse);
        // 4. Tx Sign
        try {
            sign(priKeyHexstr, txRequest);
        } catch (NoSuchAlgorithmException e) {
            throw e;
        } catch (SignatureException e) {
            throw e;
        }
        // R, S, V null check
        assertNotNull(txRequest.getR());
        assertNotNull(txRequest.getS());
        assertNotNull(txRequest.getV());

        // 5. MakeXChainType Tx Send
        TxSendResponse sendResponse = xCube.sendTransaction(txRequest).send();
        // server response sign check result error case
        if (sendResponse.getError() != null && sendResponse.getError().getCode() == 323) {
            fail();
        }
        assertNull(sendResponse.getError());
        assertNotNull(sendResponse.getResult());

        // 6. getTransaction tx exist check
        TxResponse txRes = xCube.getTransaction(null, targetChainId, ByteUtil.toNoPriFixHexString(sendResponse.getResult().getTxHash())).send();
        assertNull(txRes.getError());
        assertNotNull(txRes.getResult());

        // 7. Type check
        Assert.assertEquals(txRes.getResult().getPayloadType(), ApiEnum.PayloadType.MakeXChainType);

        // 8. pubkey and message same check
        Byte b = new Byte(txRes.getResult().getV());
        ECKey.ECDSASignature verifysign = ECKey.ECDSASignature.fromComponents(txRes.getResult().getR().toByteArray(), txRes.getResult().getS().toByteArray(), b.byteValue());
        byte[] resMsg = txRes.generateTxRequest(true).marshalProto(false).toByteArray();
        XbSignUtil xbSignUtil = new XbSignUtil();
        ECKey recoverkey = xbSignUtil.getRecoveryKey(verifysign, resMsg);
        assertEquals(ByteUtil.toNoPriFixHexString(txRes.getResult().getSender()), Hex.toHexString(recoverkey.getAddress()));

        // 9. ecdsa/sha256 sender sign verify
        try {
            boolean isVerify = xbSignUtil.verify(verifysign, resMsg);
            assertTrue(isVerify);
        } catch (NoSuchAlgorithmException e) {
            throw e;
        }
        // 10. tx Hash same Check
        Assert.assertEquals(sendResponse.getResult().getTxHash(), txRes.getResult().getTxHash());
    }

    @Test
    @Order(order = 11)
    public void TestSignTransaction() {
        // 1. client time setting
        BigInteger time = new BigInteger(String.valueOf(DateUtil.getMicroSecond()));
        // 2. Common Type Tx Create
        String local_sender = sender;
        TxRequest.Builder builder = makeDefaultBuilder();
        builder.withSender(local_sender);
        builder.withReceiver(receiver);
        builder.withPayloadType(ApiEnum.PayloadType.CommonType);
        builder.withFee(CurrencyUtil.generateXTO(CurrencyUtil.CurrencyType.CoinType, 1));
        builder.withAmount(CurrencyUtil.generateXTO(CurrencyUtil.CurrencyType.CoinType, 10));
        builder.withTime(time);
        builder.withPayloadBody(new TxCommonBody("test네트웍1"));
        TxRequest txRequest = builder.build();

        TxSignResponse txSignResponse = xCube.signTransaction(null, txRequest).send();
        txSignResponse.setSign(txRequest);
        assertNull(txSignResponse.getError());
        //System.out.println(txSignResponse.getResult());
        assertNotNull(txSignResponse.getResult());
        assertNotNull(txSignResponse.getResult().getV());
        assertNotNull(txSignResponse.getResult().getR());
        assertNotNull(txSignResponse.getResult().getS());
        //System.out.println(txRequest.toString());
    }
}
