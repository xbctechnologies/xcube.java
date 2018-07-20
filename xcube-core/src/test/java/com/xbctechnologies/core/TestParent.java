package com.xbctechnologies.core;

import com.xbctechnologies.core.apis.XCube;
import com.xbctechnologies.core.apis.dto.res.account.AccountBalanceResponse;
import com.xbctechnologies.core.apis.dto.res.data.CurrentGovernance;
import com.xbctechnologies.core.apis.dto.res.data.ProgressGovernance;
import com.xbctechnologies.core.apis.dto.xtypes.TxGRProposalBody;
import com.xbctechnologies.core.apis.TestXCube;
import com.xbctechnologies.core.component.rest.RestHttpClient;
import com.xbctechnologies.core.component.rest.RestHttpConfig;
import com.xbctechnologies.core.utils.Base64Util;
import com.xbctechnologies.core.utils.CurrencyUtil;
import org.junit.Before;

import javax.xml.bind.DatatypeConverter;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;

import static com.xbctechnologies.core.utils.CurrencyUtil.CurrencyType.CoinType;

public class TestParent {
    public XCube xCube;
    public TestXCube testXCube;

    public final String targetChainId = "1T";

    public final String sender = "0x9ac601f1a9c8385cb1fd794d030898168b0b617a";
    public BigInteger senderAmount = CurrencyUtil.generateXTO(CoinType, 7000000);
    public final String receiver = "0x7826d36525a285072fd8fe7cbe1597013d8d9761";
    public BigInteger receiverAmount = CurrencyUtil.generateXTO(CoinType, 4000000);
    public final String validator = "0xd52ff6084b6dec53b74b2ac9133fe3541709fa7f";
    public BigInteger validatorAmount = CurrencyUtil.generateXTO(CoinType, 10000000);

    public final String testFile = "/testFile";
    public final String testDummyFile = "/checkNotSameFile";
    public final String testFileOver1MB = "/SampleVideo_1280x720_1mb.mp4";

    public final String defaultCompanyName = "Xblocksystems";
    public final String defaultCompanyDesc = "Xblocksystems is specialized in the field of block chain and security certification.";
    public final String defaultCompanyUrl = "http://www.xblocksys.com";
    public final String defaultCompanyLogoUrl = "http://www.xblocksys.com/img/xbs_08.png";
    public final String defaultCompanyLat = "37.520958";
    public final String defaultCompanyLon = "127.029161";

    public final String privKeyPassword = "1111";
    public final String privKeyJson = "{\n" +
            "  \"result\": {\n" +
            "    \"address\": \"9ac601f1a9c8385cb1fd794d030898168b0b617a\",\n" +
            "    \"crypto\": {\n" +
            "      \"cipher\": \"aes-128-ctr\",\n" +
            "      \"ciphertext\": \"9f1b9a92c88f6f135764f7bc16c02db7ea8eaa08ed7c67c32b3ccbf773e2118c\",\n" +
            "      \"cipherparams\": {\n" +
            "        \"iv\": \"3b227e8926e3f0f7a6280cc7ed4b663b\"\n" +
            "      },\n" +
            "      \"kdf\": \"scrypt\",\n" +
            "      \"n\": 262144,\n" +
            "      \"r\": 8,\n" +
            "      \"p\": 1,\n" +
            "      \"dklen\": 32,\n" +
            "      \"c\": 0,\n" +
            "      \"prf\": \"\",\n" +
            "      \"salt\": \"5a785a9c993943a7e1b3c2785c71d6a1c89ba279cf4c3a4ee18f396d59b570bc\",\n" +
            "      \"mac\": \"c390c34b95a305b96045ad5ee98b7e6b85ebf7b2fdaa0715414201ffe3338ad6\"\n" +
            "    },\n" +
            "    \"id\": \"1d0cdbaa-f33a-4b78-87bb-042f1979d4ee\",\n" +
            "    \"version\": 1\n" +
            "  }\n" +
            "}";

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

    public static String generateByteToString(byte[] val) {
        StringBuilder sb = new StringBuilder("[");
        StringBuilder binarySB = new StringBuilder();
        for (byte b : val) {
            binarySB.append(String.format("%s ", b & 0xff));
        }
        sb.append(binarySB.substring(0, binarySB.toString().length() - 1));
        sb.append("]");

        return sb.toString();
    }

    public static String generateByteToHexString(byte[] val) {
        return DatatypeConverter.printHexBinary(val);
    }

    public static String generateByteToBase64(byte[] val) {
        return Base64Util.encode(val);
    }

    public AccountBalanceResponse makeAccountBalance(String address, String totalBalance, String availableBalance, String stakingBalance, String predictionRewardBalance, String lockingBalance, CurrencyUtil.CurrencyType currencyType) {
        AccountBalanceResponse.Result accountBalanceResult = new AccountBalanceResponse.Result();
        accountBalanceResult.setAddress(address);
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

    public ProgressGovernance.Result makeProgressGR() {
        Map<String, BigInteger> stakeMap = new HashMap<>();
        stakeMap.put(validator, CurrencyUtil.generateXTO(CurrencyUtil.CurrencyType.CoinType, 7270002));

        ProgressGovernance.Result expectedGR = new ProgressGovernance.Result();
        expectedGR.setExpectedGRVersion(2);
        expectedGR.setStake(stakeMap);
        expectedGR.setAgreeRate(0);
        expectedGR.setDisagreeRate(0);
        expectedGR.setPass(false);

        expectedGR.setRewardXtoPerCoin(CurrencyUtil.generateXTO(CurrencyUtil.CurrencyType.CoinType, 1000));
        expectedGR.setMinCommonTxFee(CurrencyUtil.generateXTO(CurrencyUtil.CurrencyType.CoinType, 1));
        expectedGR.setMinBondingTxFee(CurrencyUtil.generateXTO(CurrencyUtil.CurrencyType.CoinType, 10000));
        expectedGR.setMinGRProposalTxFee(CurrencyUtil.generateXTO(CurrencyUtil.CurrencyType.CoinType, 100));
        expectedGR.setMinGRVoteTxFee(CurrencyUtil.generateXTO(CurrencyUtil.CurrencyType.CoinType, 0));
        expectedGR.setMinXTxFee(CurrencyUtil.generateXTO(CurrencyUtil.CurrencyType.CoinType, 1000));
        expectedGR.setMaxBlockNumsForVoting(4);
        expectedGR.setMinBlockNumsToGRProposal(23);
        expectedGR.setMinBlockNumsUtilReflection(2);
        expectedGR.setMaxBlockNumsUtilReflection(5);
        expectedGR.setBlockNumsFreezingValidator(3);
        expectedGR.setBlockNumsUtilUnbonded(1);
        expectedGR.setMaxDelegatableValidatorNums(50);
        expectedGR.setValidatorNums(50);
        expectedGR.setFirstCompatibleVersion("1.0.1-stable");
        expectedGR.setCurrentReflection(new TxGRProposalBody.CurrentReflection(2, 3));

        return expectedGR;
    }

    public CurrentGovernance.Result makeCurrentGR() {
        CurrentGovernance.Result expectedGR = new CurrentGovernance.Result();
        expectedGR.setGrVersion(1);
        expectedGR.setProposalBlockNo(1);
        expectedGR.setEndOfVotingBlockNo(0);
        expectedGR.setReflectionBlockNo(1);

        expectedGR.setRewardXtoPerCoin(CurrencyUtil.generateXTO(CurrencyUtil.CurrencyType.CoinType, 1000));
        expectedGR.setMinCommonTxFee(CurrencyUtil.generateXTO(CurrencyUtil.CurrencyType.CoinType, 1));
        expectedGR.setMinBondingTxFee(CurrencyUtil.generateXTO(CurrencyUtil.CurrencyType.CoinType, 10000));
        expectedGR.setMinGRProposalTxFee(CurrencyUtil.generateXTO(CurrencyUtil.CurrencyType.CoinType, 100));
        expectedGR.setMinGRVoteTxFee(CurrencyUtil.generateXTO(CurrencyUtil.CurrencyType.CoinType, 0));
        expectedGR.setMinXTxFee(CurrencyUtil.generateXTO(CurrencyUtil.CurrencyType.CoinType, 1000));
        expectedGR.setMaxBlockNumsForVoting(4);
        expectedGR.setMinBlockNumsToGRProposal(23);
        expectedGR.setMinBlockNumsUtilReflection(2);
        expectedGR.setMaxBlockNumsUtilReflection(5);
        expectedGR.setBlockNumsFreezingValidator(3);
        expectedGR.setBlockNumsUtilUnbonded(1);
        expectedGR.setMaxDelegatableValidatorNums(50);
        expectedGR.setValidatorNums(50);
        expectedGR.setFirstCompatibleVersion("1.0.1-stable");
        expectedGR.setCurrentReflection(new TxGRProposalBody.CurrentReflection(0, 1));

        return expectedGR;
    }
}
