package com.xbctechnologies.core;

import com.xbctechnologies.core.apis.TestXCube;
import com.xbctechnologies.core.apis.XCube;
import com.xbctechnologies.core.apis.dto.res.account.AccountBalanceResponse;
import com.xbctechnologies.core.apis.dto.res.data.CurrentGovernance;
import com.xbctechnologies.core.apis.dto.res.data.ProgressGovernance;
import com.xbctechnologies.core.apis.dto.res.data.TotalAtxResponse;
import com.xbctechnologies.core.apis.dto.xtypes.TxGRProposalBody;
import com.xbctechnologies.core.component.rest.RestHttpClient;
import com.xbctechnologies.core.component.rest.RestHttpConfig;
import com.xbctechnologies.core.utils.Base64Util;
import com.xbctechnologies.core.utils.CurrencyUtil;
import com.xbctechnologies.core.utils.NumberUtil;
import lombok.Data;
import org.junit.Assert;
import org.junit.Before;

import javax.xml.bind.DatatypeConverter;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.xbctechnologies.core.utils.CurrencyUtil.CurrencyType.*;
import static org.junit.Assert.assertEquals;

public class TestParent {
    public XCube xCube;
    public TestXCube testXCube;

    public final String targetChainId = "1T";

    public final String sender = "0x9ac601f1a9c8385cb1fd794d030898168b0b617a";
    public final String receiver = "0x7826d36525a285072fd8fe7cbe1597013d8d9761";
    public final String validator = "0xd52ff6084b6dec53b74b2ac9133fe3541709fa7f";

    public final BigInteger rewardXtoPerCoin = CurrencyUtil.generateXTO(GXTOType, 1);

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

    public BigInteger getInitBalance() {
        BigInteger totalAmount = new BigInteger("0");
        for (int i = 1; i <= 10; i++) {
            totalAmount = totalAmount.add(new BigInteger(String.valueOf(i) + "000000000000000000000000"));
        }

        return totalAmount;
    }

    @Data
    public class ExpectedReward {
        private long blockNo;

        private BigInteger totalStakingOfValidator;
        private BigInteger totalStakingOfDelegator;

        private BigInteger reward;
        private BigInteger originFee;
        private BigInteger actualRewardAboutFee;
        private BigInteger diffReward;
    }

    @Data
    public class ExpectedRewardResult {
        private List<ExpectedReward> expectedRewards = new ArrayList<>();
        private BigInteger totalBalance;
        private BigInteger totalReward = new BigInteger("0");
        private BigInteger totalDiffReward = new BigInteger("0");
    }

    public ExpectedReward makeExpectedReward(long blockNo, BigInteger totalStakingOfValidator, BigInteger totalStakingOfDelegator, BigInteger originFee) {
        ExpectedReward expectedReward = new ExpectedReward();
        expectedReward.setBlockNo(blockNo);

        expectedReward.totalStakingOfValidator = new BigInteger(totalStakingOfValidator.toString());
        expectedReward.totalStakingOfDelegator = new BigInteger(totalStakingOfDelegator.toString());

        if (originFee == null) {
            expectedReward.originFee = new BigInteger("0");
        } else {
            expectedReward.originFee = new BigInteger(originFee.toString());
        }

        return expectedReward;
    }

    public void calculateExpectedReward(ExpectedRewardResult expectedRewardResult, ExpectedReward expectedReward, BigInteger changedRewardXtoPerCoin) {
        BigInteger totalStaking = new BigInteger(expectedReward.getTotalStakingOfValidator().toString()).add(expectedReward.getTotalStakingOfDelegator());
        totalStaking = CurrencyUtil.generateCurrencyUnitToCurrencyUnit(XTOType, CoinType, totalStaking);
        expectedReward.setReward(totalStaking.multiply(changedRewardXtoPerCoin == null ? rewardXtoPerCoin : changedRewardXtoPerCoin));

        BigInteger rewardUnitAboutFee = new BigInteger(expectedReward.getOriginFee().toString()).divide(totalStaking);
        expectedReward.setActualRewardAboutFee(rewardUnitAboutFee.multiply(totalStaking));
        expectedReward.setDiffReward(expectedReward.getOriginFee().subtract(expectedReward.getActualRewardAboutFee()));

        expectedRewardResult.getExpectedRewards().add(expectedReward);
        expectedRewardResult.setTotalDiffReward(expectedRewardResult.getTotalDiffReward().add(expectedReward.getDiffReward()));

        BigInteger tempTotalReward = expectedRewardResult.getTotalReward().add(expectedReward.getReward());
        tempTotalReward = tempTotalReward.add(expectedReward.getActualRewardAboutFee());
        expectedRewardResult.setTotalReward(tempTotalReward);

        BigInteger tempTotalBalance = expectedRewardResult.getTotalBalance().add(expectedReward.getReward()).subtract(expectedReward.getOriginFee());
        tempTotalBalance = tempTotalBalance.add(expectedReward.getActualRewardAboutFee());
        expectedRewardResult.setTotalBalance(tempTotalBalance);
    }

    public void assertEqualTotalBalance(ExpectedRewardResult expectedRewardResult, BigInteger subAmount) {
        TotalAtxResponse totalAtxResponse = xCube.getTotalATX(null, targetChainId, CurrencyUtil.CurrencyType.XTOType).send();
        assertEquals(expectedRewardResult.getTotalBalance().subtract(subAmount), totalAtxResponse.getResult().getTotalBalance());
    }

    public void CheckATXBalance(ExpectedRewardResult expectedRewardResult, BigInteger subAmount) throws Exception {
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

        //Fee에 대한 보상값을 계산할때 블록당 전체Fee / 총지분량(ATX) 할때 소숫점이 발생하여, 차이값이 생김.
        //subAmount는 실제로 이전 트랜잭션의 Fee 값이지만 이 메소드를 수행하는 시점에는 해당 Fee에 대한 보상이 이루어지지 않았기 대문에 1의 차이가 발생한다. (보상은 현재 블록의 이전블록 까지 이루어짐)
        assertEquals(expectedRewardResult.getTotalBalance().subtract(subAmount), totalAtxResponse.getResult().getTotalBalance());
        System.out.println(String.format("totalBalance:%s\ntotalReward:%s\ninitBalance:%s\npureActualBalance:%s\ndiff:%s\nlostAmountByFee:%s",
                NumberUtil.comma(expectedRewardResult.getTotalBalance()),
                NumberUtil.comma(expectedRewardResult.getTotalReward()),
                NumberUtil.comma(getInitBalance()),
                NumberUtil.comma(expectedRewardResult.getTotalBalance().subtract(expectedRewardResult.getTotalReward())),
                NumberUtil.comma(getInitBalance().subtract(expectedRewardResult.getTotalBalance().subtract(expectedRewardResult.getTotalReward()))),
                CurrencyUtil.generateStringCurrencyUnitToCurrencyUnit(CurrencyUtil.CurrencyType.XTOType, CurrencyUtil.CurrencyType.CoinType, expectedRewardResult.getTotalDiffReward())
        ));
    }
}
