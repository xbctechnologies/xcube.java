package tx;

import com.xbctechnologies.core.TestParent;
import com.xbctechnologies.core.apis.dto.ApiEnum;
import com.xbctechnologies.core.apis.dto.TxRequest;
import com.xbctechnologies.core.apis.dto.xtypes.*;
import com.xbctechnologies.core.apis.dto.xtypes.proto.TxProto;
import org.junit.Test;

import java.io.File;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import static com.xbctechnologies.core.apis.dto.ApiEnum.PayloadType.*;
import static org.junit.Assert.assertEquals;

public class TestTx extends TestParent {
    private String targetChainId = "0T";
    private String sender = "1b51d971dda843e8dd4550ee7aff728e9d65b2af";
    private String receiver = "9de91697e8fa30a7424e229121782fc8cc62aba0";
    private BigInteger fee = new BigInteger("1000000000000000000000");
    private BigInteger amount = new BigInteger("10000000");
    private BigInteger time = new BigInteger("1535443994622835");

    private String testFile = "/testFile";
    private String testPrivKey = "/testPrivKey.json";

    private TxRequest.Builder makeDefaultBuilder() {
        return TxRequest.builder()
                .withIsSync(true)
                .withTargetChainId(targetChainId)
                .withSender(sender)
                .withReceiver(receiver)
                .withFee(fee)
                .withAmount(amount)
                .withTime(time);
    }

    private void compareValue(TxRequest txRequest, String expected) {
        TxProto.Tx tx = txRequest.marshalProto(false);
//        try{
//            System.out.println(JsonFormat.printer().print(tx));
//        }catch (Exception e){
//        }
        byte[] data = tx.toByteArray();
        String actual = TestParent.generateByteToBase64(data);
        assertEquals(expected, actual);
    }

    @Test
    public void commonTx() {
        String go = "CgIwVBIoMWI1MWQ5NzFkZGE4NDNlOGRkNDU1MGVlN2FmZjcyOGU5ZDY1YjJhZhooOWRlOTE2OTdlOGZhMzBhNzQyNGUyMjkxMjE3ODJmYzhjYzYyYWJhMCIWMTAwMDAwMDAwMDAwMDAwMDAwMDAwMCoIMTAwMDAwMDAyCggBEgYKBHRlc3Q6EDE1MzU0NDM5OTQ2MjI4MzU=";

        TxRequest.Builder builder = makeDefaultBuilder();
        TxRequest txRequest = builder
                .withPayloadType(CommonType)
                .withPayloadBody(new TxCommonBody("test"))
                .build();
        compareValue(txRequest, go);
    }

    @Test
    public void fileTx() {
        String go = "CgIwVBIoOWRlOTE2OTdlOGZhMzBhNzQyNGUyMjkxMjE3ODJmYzhjYzYyYWJhMBoWMTAwMDAwMDAwMDAwMDAwMDAwMDAwMCIIMTAwMDAwMDAqjwEIAhqKAQgBEiClLXxJdR1zbja0miOaVX7De2zyW1jcX/D8W00xEOZIUyIQeGNoYWluIGF1dGggZGF0YSooMWI1MWQ5NzFkZGE4NDNlOGRkNDU1MGVlN2FmZjcyOGU5ZDY1YjJhZiooOWRlOTE2OTdlOGZhMzBhNzQyNGUyMjkxMjE3ODJmYzhjYzYyYWJhMDIQMTUzNTQ0Mzk5NDYyMjgzNQ==";

        List<String> authors = new ArrayList<>();
        authors.add(sender);
        authors.add(receiver);

        TxFileBody txFileBody = new TxFileBody();
        txFileBody.setOp(ApiEnum.OpType.RegisterType);
        txFileBody.setFile(new File(this.getClass().getResource(testFile).getFile()));
        txFileBody.setInfo("xchain auth data");
        txFileBody.setAuthors(authors);

        TxRequest.Builder builder = makeDefaultBuilder();
        TxRequest txRequest = builder
                .withPayloadType(FileType)
                .withPayloadBody(txFileBody)
                .build();
        compareValue(txRequest, go);
    }

    @Test
    public void bondingTx() {
        String go = "CgIwVBIoMWI1MWQ5NzFkZGE4NDNlOGRkNDU1MGVlN2FmZjcyOGU5ZDY1YjJhZhooOWRlOTE2OTdlOGZhMzBhNzQyNGUyMjkxMjE3ODJmYzhjYzYyYWJhMCIWMTAwMDAwMDAwMDAwMDAwMDAwMDAwMCoIMTAwMDAwMDAyDggDIgoKCDEwMDAwMDAwOhAxNTM1NDQzOTk0NjIyODM1";

        TxRequest.Builder builder = makeDefaultBuilder();
        TxRequest txRequest = builder
                .withAmount(amount)
                .withPayloadType(BondingType)
                .withPayloadBody(new TxBondingBody())
                .build();
        compareValue(txRequest, go);
    }

    @Test
    public void unbondingTx() {
        String go = "CgIwVBIoMWI1MWQ5NzFkZGE4NDNlOGRkNDU1MGVlN2FmZjcyOGU5ZDY1YjJhZhooOWRlOTE2OTdlOGZhMzBhNzQyNGUyMjkxMjE3ODJmYzhjYzYyYWJhMCIWMTAwMDAwMDAwMDAwMDAwMDAwMDAwMCoIMTAwMDAwMDAyDggEKgoKCDEwMDAwMDAwOhAxNTM1NDQzOTk0NjIyODM1";

        TxRequest.Builder builder = makeDefaultBuilder();
        TxRequest txRequest = builder
                .withAmount(amount)
                .withPayloadType(UnbondingType)
                .withPayloadBody(new TxUnbondingBody())
                .build();
        compareValue(txRequest, go);
    }

    @Test
    public void delegatingTx() {
        String go = "CgIwVBIoMWI1MWQ5NzFkZGE4NDNlOGRkNDU1MGVlN2FmZjcyOGU5ZDY1YjJhZhooOWRlOTE2OTdlOGZhMzBhNzQyNGUyMjkxMjE3ODJmYzhjYzYyYWJhMCIWMTAwMDAwMDAwMDAwMDAwMDAwMDAwMCoIMTAwMDAwMDAyOAgFMjQKKDFiNTFkOTcxZGRhODQzZThkZDQ1NTBlZTdhZmY3MjhlOWQ2NWIyYWYSCDEwMDAwMDAwOhAxNTM1NDQzOTk0NjIyODM1";

        TxRequest.Builder builder = makeDefaultBuilder();
        TxRequest txRequest = builder
                .withReceiver(sender)
                .withAmount(amount)
                .withPayloadType(DelegatingType)
                .withPayloadBody(new TxDelegatingBody())
                .build();
        compareValue(txRequest, go);
    }

    @Test
    public void undelegatingTx() {
        String go = "CgIwVBIoMWI1MWQ5NzFkZGE4NDNlOGRkNDU1MGVlN2FmZjcyOGU5ZDY1YjJhZhooOWRlOTE2OTdlOGZhMzBhNzQyNGUyMjkxMjE3ODJmYzhjYzYyYWJhMCIWMTAwMDAwMDAwMDAwMDAwMDAwMDAwMCoIMTAwMDAwMDAyOAgGOjQKKDFiNTFkOTcxZGRhODQzZThkZDQ1NTBlZTdhZmY3MjhlOWQ2NWIyYWYSCDEwMDAwMDAwOhAxNTM1NDQzOTk0NjIyODM1";

        TxRequest.Builder builder = makeDefaultBuilder();
        TxRequest txRequest = builder
                .withReceiver(sender)
                .withAmount(amount)
                .withPayloadType(UndelegatingType)
                .withPayloadBody(new TxUndelegatingBody())
                .build();
        compareValue(txRequest, go);
    }

    @Test
    public void grProposalTx() {
        String go = "CgIwVBIoMWI1MWQ5NzFkZGE4NDNlOGRkNDU1MGVlN2FmZjcyOGU5ZDY1YjJhZhooOWRlOTE2OTdlOGZhMzBhNzQyNGUyMjkxMjE3ODJmYzhjYzYyYWJhMCIWMTAwMDAwMDAwMDAwMDAwMDAwMDAwMCoIMTAwMDAwMDAyZAgHQmAKCDEwMDAwMDAwEB4YRiIIMTAwMDAwMDAqCDEwMDAwMDAwMggxMDAwMDAwMDoIMTAwMDAwMDBCCDEwMDAwMDAwSApQClgKYBRoCnAKeAqAAQqKAQUxLjAuMJIBBAgKEAo6EDE1MzU0NDM5OTQ2MjI4MzU=";
        TxGRProposalBody txGRProposalBody = TxGRProposalBody.builder()
                .withRewardXtoPerCoin(amount)
                .withMinCommonTxFee(amount)
                .withMinBondingTxFee(amount)
                .withMinGRProposalTxFee(amount)
                .withMinGRVoteTxFee(amount)
                .withMinXTxFee(amount)
                .withMaxBlockNumsForVoting(10)
                .withMinBlockNumsToGRProposal(10)
                .withMinBlockNumsUtilReflection(10)
                .withMaxBlockNumsUtilReflection(20)
                .withBlockNumsFreezingValidator(10)
                .withBlockNumsUtilUnbonded(10)
                .withMaxDelegatableValidatorNums(10)
                .withValidatorNums(10)
                .withFirstCompatibleVersion("1.0.0")
                .withCurrentReflection(new TxGRProposalBody.CurrentReflection(10, 10))
                .build();

        TxRequest.Builder builder = makeDefaultBuilder();
        TxRequest txRequest = builder
                .withPayloadType(GRProposalType)
                .withPayloadBody(txGRProposalBody)
                .build();

        compareValue(txRequest, go);
    }

    @Test
    public void grVoteTx() {
        String go = "CgIwVBIoMWI1MWQ5NzFkZGE4NDNlOGRkNDU1MGVlN2FmZjcyOGU5ZDY1YjJhZhooOWRlOTE2OTdlOGZhMzBhNzQyNGUyMjkxMjE3ODJmYzhjYzYyYWJhMCIWMTAwMDAwMDAwMDAwMDAwMDAwMDAwMCoIMTAwMDAwMDAyBggISgIIAToQMTUzNTQ0Mzk5NDYyMjgzNQ==";

        TxRequest.Builder builder = makeDefaultBuilder();
        TxRequest txRequest = builder
                .withPayloadType(GRVoteType)
                .withPayloadBody(new TxGRVoteBody(true))
                .build();
        compareValue(txRequest, go);
    }

    @Test
    public void recoverTx() {
        String go = "CgIwVBIoMWI1MWQ5NzFkZGE4NDNlOGRkNDU1MGVlN2FmZjcyOGU5ZDY1YjJhZhooOWRlOTE2OTdlOGZhMzBhNzQyNGUyMjkxMjE3ODJmYzhjYzYyYWJhMCIWMTAwMDAwMDAwMDAwMDAwMDAwMDAwMCoIMTAwMDAwMDAyAggJOhAxNTM1NDQzOTk0NjIyODM1";

        TxRequest.Builder builder = makeDefaultBuilder();
        TxRequest txRequest = builder
                .withPayloadType(RecoverValidatorType)
                .build();
        compareValue(txRequest, go);
    }

    @Test
    public void makeXChainTx() {
        String go = "CgIwVBIoMWI1MWQ5NzFkZGE4NDNlOGRkNDU1MGVlN2FmZjcyOGU5ZDY1YjJhZhooOWRlOTE2OTdlOGZhMzBhNzQyNGUyMjkxMjE3ODJmYzhjYzYyYWJhMCIWMTAwMDAwMDAwMDAwMDAwMDAwMDAwMCoIMTAwMDAwMDAywQEIClK8AQgKEAEYASgKOjQKKDFiNTFkOTcxZGRhODQzZThkZDQ1NTBlZTdhZmY3MjhlOWQ2NWIyYWYSCDEwMDAwMDAwOjQKKDlkZTkxNjk3ZThmYTMwYTc0MjRlMjI5MTIxNzgyZmM4Y2M2MmFiYTASCDEwMDAwMDAwQhcKDwoFdHlwZTESBnZhbHVlMRIEMTAwMEIXCg8KBXR5cGUyEgZ2YWx1ZTISBDEwMDBKFE1ha2UgeGNoYWluIGZvciB0ZXN0OhAxNTM1NDQzOTk0NjIyODM1";

        List<TxMakeXChainBody.AssetHolder> assetHolders = new ArrayList<>();
        assetHolders.add(new TxMakeXChainBody.AssetHolder(sender, amount));
        assetHolders.add(new TxMakeXChainBody.AssetHolder(receiver, amount));

        List<TxMakeXChainBody.Validator> validators = new ArrayList<>();
        validators.add(new TxMakeXChainBody.Validator(new TxMakeXChainBody.Validator.PubKey("type1", "value1"), "1000",
                defaultCompanyName,
                defaultCompanyDesc,
                defaultCompanyUrl,
                defaultCompanyLogoUrl,
                defaultCompanyLat,
                defaultCompanyLon));
        validators.add(new TxMakeXChainBody.Validator(new TxMakeXChainBody.Validator.PubKey("type2", "value2"), "1000",
                defaultCompanyName,
                defaultCompanyDesc,
                defaultCompanyUrl,
                defaultCompanyLogoUrl,
                defaultCompanyLat,
                defaultCompanyLon));

        TxMakeXChainBody txMakeXChainBody = TxMakeXChainBody.builder()
                .withDepth(10)
                .withHasAsset(true)
                .withEnableSubAsset(true)
                .withNonExchangeChain(false)
                .withAirdropRate(10)
                .withAssetHolders(assetHolders)
                .withValidators(validators)
                .withCustomDesc("Make xchain for test")
                .build();

        TxRequest.Builder builder = makeDefaultBuilder();
        TxRequest txRequest = builder
                .withPayloadType(MakeXChainType)
                .withPayloadBody(txMakeXChainBody)
                .build();

        compareValue(txRequest, go);
    }
}
