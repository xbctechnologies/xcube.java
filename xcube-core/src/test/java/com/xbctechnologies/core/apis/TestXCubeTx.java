package com.xbctechnologies.core.apis;

import com.xbctechnologies.core.apis.dto.ApiEnum;
import com.xbctechnologies.core.apis.dto.TxRequest;
import com.xbctechnologies.core.apis.dto.res.BoolResponse;
import com.xbctechnologies.core.apis.dto.res.LongResponse;
import com.xbctechnologies.core.apis.dto.res.Response;
import com.xbctechnologies.core.apis.dto.res.account.*;
import com.xbctechnologies.core.apis.dto.res.block.BlockResponse;
import com.xbctechnologies.core.apis.dto.res.block.BlockTxCntResponse;
import com.xbctechnologies.core.apis.dto.res.data.ProgressGovernance;
import com.xbctechnologies.core.apis.dto.res.data.TotalAtxResponse;
import com.xbctechnologies.core.apis.dto.res.validator.ValidatorListResponse;
import com.xbctechnologies.core.apis.dto.res.network.NetworkPeersResponse;
import com.xbctechnologies.core.apis.dto.res.node.XChainInfoResponse;
import com.xbctechnologies.core.apis.dto.res.tx.TxReceiptResponse;
import com.xbctechnologies.core.apis.dto.res.tx.TxResponse;
import com.xbctechnologies.core.apis.dto.res.tx.TxSendResponse;
import com.xbctechnologies.core.apis.dto.res.tx.TxSignResponse;
import com.xbctechnologies.core.apis.dto.res.validator.ValidatorBondResponse;
import com.xbctechnologies.core.apis.dto.res.validator.ValidatorSetResponse;
import com.xbctechnologies.core.apis.dto.xtypes.*;
import com.xbctechnologies.core.order.Order;
import com.xbctechnologies.core.order.OrderedRunner;
import com.xbctechnologies.core.TestParent;
import com.xbctechnologies.core.exception.TransactionException;
import com.xbctechnologies.core.utils.CurrencyUtil;
import com.xbctechnologies.core.utils.DateUtil;
import com.xbctechnologies.core.utils.JsonUtil;
import com.xbctechnologies.xcrypto.api.XbSignUtil;
import com.xbctechnologies.xcrypto.ec.ECKey;
import com.xbctechnologies.xcrypto.util.ByteUtil;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.spongycastle.util.encoders.Hex;

import java.io.File;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import static com.xbctechnologies.core.utils.CurrencyUtil.CurrencyType.CoinType;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.junit.Assert.*;

/**
 * XBlockchain의 각 API를 테스트 해볼수 있는 클래스이다.
 */
@RunWith(OrderedRunner.class)
public class TestXCubeTx extends TestParent {
    private BigInteger fee = new BigInteger("1000000000000000000000");
    private BigInteger amount = new BigInteger("1000000000000000000000");

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
    @Order(order = 1)
    public void TestTxFullRequest() throws Exception {
        AccountBalanceResponse beforeSender = xCube.getBalance(null, targetChainId, sender, CurrencyUtil.CurrencyType.CoinType).send();
        AccountBalanceResponse beforeReceiver = xCube.getBalance(null, targetChainId, receiver, CurrencyUtil.CurrencyType.CoinType).send();

        //Send tx
        TxRequest txRequest = makeDefaultBuilder()
                .withPayloadType(ApiEnum.PayloadType.CommonType)
                .withPayloadBody(new TxCommonBody("test"))
                .build();

        TxSendResponse txSendResponse = xCube.sendTransaction(txRequest).send();
        assertNull(txSendResponse.getError());
        assertNotNull(txSendResponse.getResult());

        AccountBalanceResponse afterSender = xCube.getBalance(null, targetChainId, sender, CurrencyUtil.CurrencyType.CoinType).send();
        AccountBalanceResponse afterReceiver = xCube.getBalance(null, targetChainId, receiver, CurrencyUtil.CurrencyType.CoinType).send();

        System.out.println(beforeSender.getBalance() + " : " + afterSender.getBalance());
        System.out.println(beforeReceiver.getBalance() + " : " + afterReceiver.getBalance());
    }

    /**
     * Test Tx
     */
    @Test
    @Order(order = 2)
    public void TestTxCommonRequest() throws Exception {
        //https://developers.google.com/protocol-buffers/docs/javatutorial
        TxRequest txRequest = makeDefaultBuilder()
                .withPayloadType(ApiEnum.PayloadType.CommonType)
                .withPayloadBody(new TxCommonBody("test"))
                .build();

        TxSendResponse txSendResponse = xCube.sendTransaction(txRequest).send();
        assertNull(txSendResponse.getError());
        assertNotNull(txSendResponse.getResult());
    }

    @Test(expected = TransactionException.class)
    @Order(order = 3)
    public void TestTxCommonRequestException() throws Exception {
        TxRequest txRequest = makeDefaultBuilder()
                .withReceiver(null)
                .withPayloadType(ApiEnum.PayloadType.CommonType)
                .withPayloadBody(new TxCommonBody("test"))
                .build();

        xCube.sendTransaction(txRequest).send();
    }

    @Test
    @Order(order = 4)
    public void TestTxFileRequest() throws Exception {
        List<String> authors = new ArrayList<>();
        authors.add(sender);
        authors.add(receiver);

        TxFileBody txFileBody = new TxFileBody();
        txFileBody.setOp(ApiEnum.OpType.RegisterType);
        txFileBody.setFile(new File(this.getClass().getResource(testFile).getFile()));
        txFileBody.setInfo("xchain auth data");
        txFileBody.setAuthors(authors);

        TxRequest txRequest = makeDefaultBuilder()
                .withReceiver(sender)
                .withAmount(null)
                .withPayloadType(ApiEnum.PayloadType.FileType)
                .withPayloadBody(txFileBody)
                .build();

        TxSendResponse txSendResponse = xCube.sendTransaction(txRequest).send();
        assertNull(txSendResponse.getError());
        assertNotNull(txSendResponse.getResult());
        System.out.println(txSendResponse.getResult());
    }

    @Test
    @Order(order = 5)
    public void TestTxBondingRequest() throws Exception {
        TxRequest txRequest = makeDefaultBuilder()
                .withSender(sender)
                .withReceiver(sender)
                .withAmount(CurrencyUtil.generateXTO(CoinType, 100))
                .withFee(CurrencyUtil.generateXTO(CoinType, 10000))
                .withPayloadType(ApiEnum.PayloadType.BondingType)
                .withPayloadBody(new TxBondingBody())
                .build();

        TxSendResponse txSendResponse = xCube.bonding(txRequest).send();
        assertNull(txSendResponse.getError());
        assertNotNull(txSendResponse.getResult());
    }

    @Test
    @Order(order = 6)
    public void TestTxUnbondingRequest() throws Exception {
        TxRequest txRequest = makeDefaultBuilder()
                .withSender(sender)
                .withReceiver(sender)
                .withAmount(CurrencyUtil.generateXTO(CoinType, 100))
                .withPayloadType(ApiEnum.PayloadType.UnbondingType)
                .withPayloadBody(new TxUnbondingBody())
                .build();

        TxSendResponse txSendResponse = xCube.unbonding(txRequest).send();
        assertNull(txSendResponse.getError());
        assertNotNull(txSendResponse.getResult());
    }

    @Test
    @Order(order = 7)
    public void TestTxDelegatingRequest() throws Exception {
        TxRequest txRequest = makeDefaultBuilder()
                .withReceiver(validator)
                .withAmount(new BigInteger("100"))
                .withPayloadType(ApiEnum.PayloadType.DelegatingType)
                .withPayloadBody(new TxDelegatingBody())
                .build();

        TxSendResponse txSendResponse = xCube.delegating(txRequest).send();
        assertNull(txSendResponse.getError());
        assertNotNull(txSendResponse.getResult());
    }

    @Test
    @Order(order = 8)
    public void TestTxUndelegatingRequest() throws Exception {
        TxRequest txRequest = makeDefaultBuilder()
                .withReceiver(validator)
                .withAmount(new BigInteger("100"))
                .withPayloadType(ApiEnum.PayloadType.UndelegatingType)
                .withPayloadBody(new TxUndelegatingBody())
                .build();

        TxSendResponse txSendResponse = xCube.undelegating(txRequest).send();
        assertNull(txSendResponse.getError());
        assertNotNull(txSendResponse.getResult());
    }

    @Test
    @Order(order = 9)
    public void TestTxGRProposalRequest() throws Exception {
        //Proposal 할수있는 조건을 충족하기 위해
        for (int i = 0; i < 25; i++) {
            TestTxCommonRequest();
        }

        TxGRProposalBody txGRProposalBody = TxGRProposalBody.builder()
                .withCurrentReflection(new TxGRProposalBody.CurrentReflection(1, 2))
                .build();

        TxRequest txRequest = makeDefaultBuilder()
                .withSender(validator)
                .withReceiver(validator)
                .withAmount(null)
                .withPayloadType(ApiEnum.PayloadType.GRProposalType)
                .withPayloadBody(txGRProposalBody)
                .build();

        TxSendResponse txSendResponse = xCube.sendTransaction(txRequest).send();
        assertNull(txSendResponse.getError());
        assertNotNull(txSendResponse.getResult());
    }

    @Test
    @Order(order = 10)
    public void TestTxGRVoteRequest() throws Exception {
        TxRequest txRequest = makeDefaultBuilder()
                .withSender(validator)
                .withReceiver(validator)
                .withAmount(null)
                .withPayloadType(ApiEnum.PayloadType.GRVoteType)
                .withPayloadBody(new TxGRVoteBody(true))
                .build();

        TxSendResponse txSendResponse = xCube.sendTransaction(txRequest).send();
        assertNull(txSendResponse.getError());
        assertNotNull(txSendResponse.getResult());
    }

    @Test
    @Order(order = 11)
    public void TestTxRecoverValidatorRequest() throws Exception {
        TxRequest txRequest = makeDefaultBuilder()
                .withSender(validator)
                .withReceiver(validator)
                .withAmount(null)
                .withPayloadType(ApiEnum.PayloadType.RecoverValidatorType)
                .withPayloadBody(new TxRecoverBody())
                .build();

        TxSendResponse txSendResponse = xCube.sendTransaction(txRequest).send();
        assertNull(txSendResponse.getError());
        assertNotNull(txSendResponse.getResult());
    }

    @Test
    @Order(order = 12)
    public void TestTxMakeXChainRequest() throws Exception {
        List<TxMakeXChainBody.AssetHolder> assetHolders = new ArrayList<>();
        assetHolders.add(new TxMakeXChainBody.AssetHolder(sender, fee));
        assetHolders.add(new TxMakeXChainBody.AssetHolder(receiver, fee));
        assetHolders.add(new TxMakeXChainBody.AssetHolder(validator, fee));

        List<TxMakeXChainBody.Validator> validators = new ArrayList<>();
        validators.add(new TxMakeXChainBody.Validator(new TxMakeXChainBody.Validator.PubKey("xblock/PubKeySecp256k1", "Ak02Nx/qaOCMn0iox4XZaA1qKsSWcl2C2Em8/USAhrPR"), "100",
                defaultCompanyName,
                defaultCompanyDesc,
                defaultCompanyUrl,
                defaultCompanyLogoUrl,
                defaultCompanyLat,
                defaultCompanyLon));

        List<TxMakeXChainBody.Seed> seeds = new ArrayList<>();
        seeds.add(new TxMakeXChainBody.Seed("seedId", "192.168.0.1", 7979));

        TxMakeXChainBody txMakeXChainBody = TxMakeXChainBody.builder()
                .withDepth(10)
                .withHasAsset(true)
                .withNonExchangeChain(true)
                .withAirdropRate(100)
                .withAssetHolders(assetHolders)
                .withValidators(validators)
                .withSeeds(seeds)
                .build();

        TxRequest txRequest = makeDefaultBuilder()
                .withSender(sender)
                .withReceiver(sender)
                .withAmount(null)
                .withPayloadType(ApiEnum.PayloadType.MakeXChainType)
                .withPayloadBody(txMakeXChainBody)
                .build();

        TxSendResponse txSendResponse = xCube.sendTransaction(txRequest).send();
        assertNull(txSendResponse.getError());
        assertNotNull(txSendResponse.getResult());
    }

    @Test
    @Order(order = 13)
    public void TestTxSignTransaction() {
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

    @Test
    @Order(order = 14)
    public void TestTxCheckOriginal() throws Exception {
        /*TxCheckOriginalResponse txCheckOriginalResponse = xCube.checkOriginal(null, targetChainId, new File(this.getClass().getResource(testFile).getFile())).send();
        assertNull(txCheckOriginalResponse.getError());
        assertNotNull(txCheckOriginalResponse.isOrigin());*/
    }

    @Test
    @Order(order = 15)
    public void TestTxGetTransaction() throws TransactionException {
        // 1. Common Type Tx Create
        BigInteger time = new BigInteger(String.valueOf(DateUtil.getMicroSecond()));
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
        // 5. CommonType Tx Send
        TxSendResponse txSendResponse = xCube.sendTransaction(txRequest).send();
        // server response sign check result error case
        if (txSendResponse.getError() != null && txSendResponse.getError().getCode() == 323) {
            fail();
        }
        assertNotNull(txSendResponse.getResult());

        TxResponse txResponse = xCube.getTransaction(null, targetChainId, ByteUtil.toNoPriFixHexString(txSendResponse.getResult().getTxHash())).send();
        assertNull(txResponse.getError());
        assertNotNull(txResponse.getResult());
    }

    @Test
    @Order(order = 16)
    public void TestTxGetTransactionReceipt() throws TransactionException {
        // 1. Common Type Tx Create
        BigInteger time = new BigInteger(String.valueOf(DateUtil.getMicroSecond()));
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
        // 5. CommonType Tx Send
        TxSendResponse txSendResponse = xCube.sendTransaction(txRequest).send();
        // server response sign check result error case
        if (txSendResponse.getError() != null && txSendResponse.getError().getCode() == 323) {
            fail();
        }
        assertNotNull(txSendResponse.getResult());

        TxReceiptResponse txReceiptResponse = xCube.getTransactionReceipt(null, targetChainId, ByteUtil.toNoPriFixHexString(txSendResponse.getResult().getTxHash())).send();
        assertNull(txReceiptResponse.getError());
        assertNotNull(txReceiptResponse.getResult());
    }

    @Test
    @Order(order = 17)
    public void TestGetProgressGovernance() throws Exception {
        TestTxGRProposalRequest();

        ProgressGovernance progressGovernance = xCube.getProgressGovernance(null, targetChainId).send();
        assertNull(progressGovernance.getError());
        assertNotNull(progressGovernance.getResult());
    }

    /**
     * Test Block
     */
    @Test
    @Order(order = 18)
    public void TestBlockGetBlock() {
        int blockNo = 1;
        BlockResponse numberblockResponse = xCube.getBlockByNumber(null, targetChainId, blockNo).send();
        assertNull(numberblockResponse.getError());
        assertNotNull(numberblockResponse.getBlock());

        BlockResponse blockResponse = xCube.getBlock(null, targetChainId, numberblockResponse.getBlock().getBlockHash()).send();
        assertNull(blockResponse.getError());
        assertNotNull(blockResponse.getBlock());
    }

    @Test
    @Order(order = 19)
    public void TestBlockGetBlockByNumber() {
        BlockResponse blockResponse = xCube.getBlockByNumber(null, targetChainId, 22).send();
        assertNull(blockResponse.getError());
        assertNotNull(blockResponse.getBlock());
    }

    @Test
    @Order(order = 20)
    public void TestBlockGetBlockTxCntByBlockNo() {
        BlockTxCntResponse blockTxCntResponse = xCube.getBlockTxCount(null, targetChainId, 22).send();
        assertNull(blockTxCntResponse.getError());
        assertNotEquals(0, blockTxCntResponse.getTxCnt().longValue());
    }

    @Test
    @Order(order = 21)
    public void TestBlockGetBlockTxCntByBlockHash() {
        int blockNo = 1;
        BlockResponse numberblockResponse = xCube.getBlockByNumber(null, targetChainId, blockNo).send();
        assertNull(numberblockResponse.getError());
        assertNotNull(numberblockResponse.getBlock());

        BlockTxCntResponse blockTxCntResponse = xCube.getBlockTxCount(null, targetChainId, numberblockResponse.getBlock().getBlockHash()).send();
        assertNull(blockTxCntResponse.getError());
        assertEquals(0, blockTxCntResponse.getTxCnt().longValue());
    }

    /**
     * Test Account
     */
    @Test
    @Order(order = 22)
    public void TestAccountNew() {
        AccountNewResponse accountNewResponse = xCube.newAccount(null, "1111").send();
        assertNull(accountNewResponse.getError());
        assertNotNull(accountNewResponse.getAddress());
    }

    @Test
    @Order(order = 23)
    public void TestAccountImportAccount() throws Exception {
        // ECC Key Gen
        XbSignUtil xbSignUtil = new XbSignUtil();
        ECKey key = xbSignUtil.ecKeyGen();

        // ECC PriKey Hex Format
        String priKeyHexstr = key.getPrivKeyHexString();
        String passphrase = "1111";
        String address = Hex.toHexString(key.getAddress());
        //System.out.println("priKeyHexstr: " + priKeyHexstr);
        //System.out.println("address: " + address);

        //BoolResponse boolResponse = xCube.importAccount(null, new File(this.getClass().getResource(testPrivKey).getFile())).send();
        BoolResponse boolResponse = xCube.importAccount(null, priKeyHexstr, address, passphrase).send();
        assertNull(boolResponse.getError());
        assertEquals(true, boolResponse.getResult());
    }

    @Test
    @Order(order = 24)
    public void TestAccountExportAccount() {
        AccountExportResponse exportResponse = testXCube.exportAccount(null, sender, "1111").send();
        assertNull(exportResponse.getError());
        assertNotNull(exportResponse.getResult());
    }

    @Test
    @Order(order = 25)
    public void TestAccountLockAccount() {
        BoolResponse boolResponse = xCube.lockAccount(null, targetChainId, sender).send();
        assertNull(boolResponse.getError());
        assertEquals(true, boolResponse.getResult());
    }

    @Test
    @Order(order = 26)
    public void TestAccountUnlockAccount() {
        BoolResponse boolResponse = xCube.unlockAccount(null, targetChainId, sender, "1111", 3000).send();
        assertNull(boolResponse.getError());
        assertEquals(true, boolResponse.getResult());
    }

    @Test
    @Order(order = 27)
    public void TestAccountGetListAccount() {
        AccountAddrListResponse accountAddrListResponse = xCube.getListAccount(null).send();
        assertNull(accountAddrListResponse.getError());
        assertNotNull(accountAddrListResponse.getAccountList());
        assertNotEquals(0, accountAddrListResponse.getAccountList().size());
    }

    @Test
    @Order(order = 28)
    public void TestAccountGetBalance() {
        AccountBalanceResponse accountBalanceResponse = xCube.getBalance(null, targetChainId, receiver, null).send();
        assertNull(accountBalanceResponse.getError());
        assertNotNull(accountBalanceResponse.getBalance());
    }

    @Test
    @Order(order = 29)
    public void TestAccountGetBondingAmount() {
        AccountBondInfoResponse accountBondInfoResponse = xCube.getBonding(null, targetChainId, sender, null).send();
        assertNull(accountBondInfoResponse.getError());
        assertNotNull(accountBondInfoResponse.getBonding());
    }

    /**
     * Test Validator
     */
    @Test
    @Order(order = 30)
    public void TestValidatorIsValidator() {
        BoolResponse boolResponse = xCube.isValidator(null, targetChainId, sender).send();
        assertNull(boolResponse.getError());
        assertNotNull(boolResponse.getResult());
    }

    @Test
    @Order(order = 31)
    public void TestValidatorGetValidatorsOf() {
        ValidatorBondResponse validatorBondResponse = xCube.getValidatorsOf(null, targetChainId, sender).send();
//        validatorBondResponse.getBonding().forEach((k, v) -> System.out.println((k + ":" + v)));
        assertNull(validatorBondResponse.getError());
        assertNotNull(validatorBondResponse.getBonding());
    }

    @Test
    @Order(order = 32)
    public void TestValidatorGetValidatorSet() {
        ValidatorSetResponse validatorSetResponse = xCube.getValidatorSet(null, targetChainId, 1).send();
        assertNull(validatorSetResponse.getError());
        assertNotNull(validatorSetResponse.getValidatorSet());
    }

    /**
     * Test Network
     */
    @Test
    @Order(order = 33)
    public void TestNetworkGetPeerCnt() {
        LongResponse longResponse = xCube.getPeerCnt(null, targetChainId).send();
        assertNull(longResponse.getError());
        assertThat(longResponse.getResult(), greaterThanOrEqualTo(0L));
    }

    @Test
    @Order(order = 34)
    public void TestNetworkGetPeers() {
        NetworkPeersResponse networkPeersResponse = xCube.getPeers(null, targetChainId).send();
        assertNull(networkPeersResponse.getError());
        assertThat(networkPeersResponse.getPeers().size(), greaterThanOrEqualTo(0));
        assertNotNull(networkPeersResponse.getPeers());
    }

    @Test
    @Order(order = 35)
    public void TestNetworkAddPeer() {
        String[] peers = {"7826d36525a285072fd8fe7cbe1597013d8d9761@1.1.1.1:8090"};
        boolean persistent = false;
        //BoolResponse boolResponse = xCube.addPeer(null, targetChainId, true).send();
        /*BoolResponse boolResponse = xCube.addPeer(null, targetChainId, peers, persistent ).send();
        assertNull(boolResponse.getError());
        assertEquals(true, boolResponse.getResult());*/
    }

    /**
     * Test Node
     */
    @Test
    @Order(order = 36)
    public void TestNodeSync() {
        BoolResponse boolResponse = xCube.sync(null, targetChainId).send();
        assertNull(boolResponse.getError());
        assertEquals(true, boolResponse.getResult());
    }

    @Test
    @Order(order = 37)
    public void TestNodeIsSync() {
        BoolResponse boolResponse = xCube.isSync(null, targetChainId).send();
        assertNull(boolResponse.getError());
        assertEquals(true, boolResponse.getResult());
    }

    @Test
    @Order(order = 38)
    public void TestNodeGetXChainInfo() {
        XChainInfoResponse response = xCube.getXChainInfo(null).send();
        assertNull(response.getError());
        assertNotNull(response.getResult());
        Assert.assertNotEquals("", response.getResult());
        System.out.println(JsonUtil.generateClassToJson(response.getXChainInfo()));
    }

    @Test
    @Order(order = 39)
    public void TestNodeGetVersion() {
        Response response = xCube.getVersion(null).send();
        assertNull(response.getError());
        assertNotNull(response.getResult());
        assertNotEquals("", response.getResult());
    }

    @Test
    @Order(order = 40)
    public void TestValidator() {
//        AccountBalanceResponse actualSender = xCube.getBalance(null, targetChainId, "0xd52ff6084b6dec53b74b2ac9133fe3541709fa7f", CurrencyUtil.CurrencyType.XTOType).send();
//        System.out.println(JsonUtil.generateClassToJson(actualSender.getBalance()));
        ValidatorListResponse validatorListResponse = xCube.getValidatorList(null, targetChainId).send();
        System.out.println(JsonUtil.generateClassToJson(validatorListResponse.getResult()));
//        SimpleValidatorsResponse simpleValidatorsResponse = xCube.getSimpleValidators(null, targetChainId).send();
//        System.out.println(JsonUtil.generateClassToJson(simpleValidatorsResponse.getResult()));
        TotalAtxResponse totalAtxResponse = xCube.getTotalATX(null, targetChainId, CurrencyUtil.CurrencyType.XTOType).send();
        System.out.println(JsonUtil.generateClassToJson(totalAtxResponse.getResult()));
    }
}
