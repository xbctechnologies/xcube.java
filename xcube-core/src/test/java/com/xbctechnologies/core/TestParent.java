package com.xbctechnologies.core;

import com.xbctechnologies.core.apis.TestXCube;
import com.xbctechnologies.core.apis.XCube;
import com.xbctechnologies.core.apis.dto.res.account.AccountBalanceResponse;
import com.xbctechnologies.core.apis.dto.res.data.CurrentGovernance;
import com.xbctechnologies.core.apis.dto.res.data.ProgressGovernance;
import com.xbctechnologies.core.apis.dto.res.data.TotalBalanceResponse;
import com.xbctechnologies.core.apis.dto.res.validator.ValidatorListResponse;
import com.xbctechnologies.core.apis.dto.res.validator.ValidatorResponse;
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
import java.util.*;

import static com.xbctechnologies.core.utils.CurrencyUtil.CurrencyType.*;
import static org.junit.Assert.assertEquals;

public class TestParent {
    public XCube xCube;
    public TestXCube testXCube;

    public BigInteger prevTotalStaking;

    public final String targetChainId = "1T";

    public final String sender = "0x96a76d177a4b361d2ebec4ca3dfdf8fd330a80c5";
    public final String receiver = "0xd09913fec8f4797b5344eddea930d2558e5d9015";
    public final String validator = "0xa642f33ec1a951eceded3cf9e51edea1a806105b";

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
            "    \"address\": \"96a76d177a4b361d2ebec4ca3dfdf8fd330a80c5\",\n" +
            "    \"crypto\": {\n" +
            "      \"cipher\": \"aes-128-ctr\",\n" +
            "      \"ciphertext\": \"946f6897b74ef9e3f32de0b2e78840c1ace962f9d39a4e1497c214597f986f41\",\n" +
            "      \"cipherparams\": {\n" +
            "        \"iv\": \"b3e80216cc5ac0fbf9dc118d5e8d26bb\"\n" +
            "      },\n" +
            "      \"kdf\": \"scrypt\",\n" +
            "      \"kdfparams\": {\n" +
            "        \"dklen\": 32,\n" +
            "        \"n\": 262144,\n" +
            "        \"p\": 1,\n" +
            "        \"r\": 8,\n" +
            "        \"salt\": \"3924a899921a18dbbaf36ace2ce5c68b34725b30ce10be0fda9898feb1a804b0\"\n" +
            "      },\n" +
            "      \"c\": 0,\n" +
            "      \"prf\": \"\",\n" +
            "      \"mac\": \"8b04192d8289087e47378a02b304ef496fd3fd563ecc808a5c10c3747656a586\"\n" +
            "    },\n" +
            "    \"id\": \"7d0f3c43-b518-4df7-98f7-c788c88b599a\",\n" +
            "    \"version\": 3\n" +
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

    public void accumulateRewardOfValidator(AccountBalanceResponse accountBalanceResponse, String validator, AccountBalanceResponse actualSender) {
        ValidatorResponse validatorResponse = xCube.getValidator(null, targetChainId, validator).send();
        if (validatorResponse.getValidator() == null) {
            return;
        }

        BigInteger totalBlockReward = new BigInteger("0");
        if (validatorResponse.getValidator().getRewardBlocks() != null) {
            ValidatorListResponse.Result.Delegator delegator = null;
            Iterator<String> iter = validatorResponse.getValidator().getDelegatorMap().keySet().iterator();
            while (iter.hasNext()) {
                String tempVal = iter.next();
                if (tempVal.equals(validator)) {
                    delegator = validatorResponse.getValidator().getDelegatorMap().get(tempVal);
                }
            }

            for (ValidatorListResponse.Result.Bonding bonding : delegator.getBondingList()) {
                for (ValidatorListResponse.Result.Reward reward : validatorResponse.getValidator().getRewardBlocks()) {
                    long rewardBlockCnt = 0;

                    if (bonding.getBlockNo() < reward.getStartBlockNo()) {
                        rewardBlockCnt = (reward.getEndBlockNo() - reward.getStartBlockNo()) + 1;
                    } else if (bonding.getBlockNo() >= reward.getStartBlockNo() && bonding.getBlockNo() <= reward.getEndBlockNo()) {
                        rewardBlockCnt = (reward.getEndBlockNo() - bonding.getBlockNo()) + 1;
                    } else {
                        break;
                    }

                    totalBlockReward = totalBlockReward.add(reward.getRewardPerCoin().multiply(new BigInteger(String.valueOf(rewardBlockCnt))).multiply(CurrencyUtil.generateCurrencyUnitToCurrencyUnit(CurrencyUtil.CurrencyType.XTOType, CurrencyUtil.CurrencyType.CoinType, bonding.getBondingBalance())));
                }
            }
        }

        BigInteger totalReward = new BigInteger("0").add(totalBlockReward).add(validatorResponse.getValidator().getRewardAmountFee());

        accountBalanceResponse.getBalance().setTotalBalance(accountBalanceResponse.getBalance().getTotalBalance().add(totalReward));
        accountBalanceResponse.getBalance().setPredictionRewardBalance(accountBalanceResponse.getBalance().getPredictionRewardBalance().add(totalReward));

        if (actualSender != null) {
            accountBalanceResponse.getBalance().setTotalBalance(accountBalanceResponse.getBalance().getTotalBalance().add(actualSender.getBalance().getLockingBalance()));
            accountBalanceResponse.getBalance().setLockingBalance(actualSender.getBalance().getLockingBalance());
        }
    }

    public BigInteger findLowestAmount(BigInteger amount) {
        BigInteger result = new BigInteger("0");

        String amountStr = amount.toString();
        int cnt = 1;
        for (int i = amountStr.length() - 1; i >= 0; i--) {
            int val = Character.getNumericValue(amountStr.charAt(i));
            if (val > 0) {
                result = result.add(new BigInteger(String.valueOf(val))).multiply(new BigInteger(String.valueOf(cnt)));
                break;
            }
            cnt *= 10;
        }

        return result;
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

        private BigInteger totalStakingForCalculationOfFee;

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

    public ExpectedReward makeExpectedRewardAboutMultiValidator(long blockNo, BigInteger originFee, BigInteger subStakingOfValidator, BigInteger subStakingOfDelegator) {
        ExpectedReward expectedReward = new ExpectedReward();
        expectedReward.setBlockNo(blockNo);
        expectedReward.totalStakingOfValidator = new BigInteger("0");
        expectedReward.totalStakingOfDelegator = new BigInteger("0");

        ValidatorListResponse validatorListResponse = xCube.getValidatorList(null, targetChainId).send();
        for (ValidatorListResponse.Result result : validatorListResponse.getValidatorList()) {
            boolean isInclusionReward = false;
            for (ValidatorListResponse.Result.Reward reward : result.getRewardBlocks()) {
                if (blockNo >= reward.getStartBlockNo() && blockNo <= reward.getEndBlockNo()) {
                    isInclusionReward = true;
                    break;
                }
            }
            if (isInclusionReward) {
                expectedReward.totalStakingOfValidator = expectedReward.totalStakingOfValidator.add(new BigInteger(result.getTotalBondingBalanceOfValidator().toString()));
                expectedReward.totalStakingOfDelegator = expectedReward.totalStakingOfDelegator.add(new BigInteger(result.getTotalBondingBalance().subtract(result.getTotalBondingBalanceOfValidator()).toString()));
            }
        }

        if (originFee == null) {
            expectedReward.originFee = new BigInteger("0");
        } else {
            expectedReward.originFee = new BigInteger(originFee.toString());
        }

        if (prevTotalStaking != null) {
            expectedReward.setTotalStakingForCalculationOfFee(prevTotalStaking);
        } else {
            expectedReward.setTotalStakingForCalculationOfFee(expectedReward.totalStakingOfValidator.add(expectedReward.totalStakingOfDelegator));
        }
        prevTotalStaking = expectedReward.totalStakingOfValidator.add(expectedReward.totalStakingOfDelegator);

        if (subStakingOfValidator != null) {
            expectedReward.setTotalStakingOfValidator(expectedReward.getTotalStakingOfValidator().subtract(subStakingOfValidator));
        }
        if (subStakingOfDelegator != null) {
            expectedReward.setTotalStakingOfDelegator(expectedReward.getTotalStakingOfDelegator().subtract(subStakingOfDelegator));
        }

        return expectedReward;
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

        if (expectedReward.getTotalStakingForCalculationOfFee() != null) {
            totalStaking = CurrencyUtil.generateCurrencyUnitToCurrencyUnit(XTOType, CoinType, expectedReward.getTotalStakingForCalculationOfFee());
        }
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
        TotalBalanceResponse totalBalanceResponse = xCube.getTotalATX(null, targetChainId, CurrencyUtil.CurrencyType.XTOType).send();
        assertEquals(expectedRewardResult.getTotalBalance().subtract(subAmount), totalBalanceResponse.getResult().getTotalBalance());
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
                "0xd09913fec8f4797b5344eddea930d2558e5d9015",
                "0x1167d0b1f1194a473691287dd3d886518a70b911",
                "0x5d74f2b7024c2258e1213cffdd983b068bbfade1",
                "0xe66cf2bc13cd7e607d4d619befdd6a51dbcd3adc",
                "0xfefffa046afb0aa030a6633a3976fcefe6791fbf",
                "0x60bcb2b65d1f086fc34aeb8f00a1f3794eed7771",
                "0x96a76d177a4b361d2ebec4ca3dfdf8fd330a80c5",
                "0xa642f33ec1a951eceded3cf9e51edea1a806105b",
                "0xaa0efb5946698728720e508a7029e539ecfa399a",
                "0xf652d4681058865cebfc25d2ed7934fa03005c6b",
        };
        for (String account : accounts) {
            AccountBalanceResponse actualSender = xCube.getBalance(null, targetChainId, account, CurrencyUtil.CurrencyType.XTOType).send();
            totalBalance = totalBalance.add(actualSender.getBalance().getTotalBalance());
            availableBalance = availableBalance.add(actualSender.getBalance().getAvailableBalance());
            stakingBalance = stakingBalance.add(actualSender.getBalance().getStakingBalance());
            predictionRewardBalance = predictionRewardBalance.add(actualSender.getBalance().getPredictionRewardBalance());
            lockingBalance = lockingBalance.add(actualSender.getBalance().getLockingBalance());
        }

        TotalBalanceResponse totalBalanceResponse = xCube.getTotalATX(null, targetChainId, CurrencyUtil.CurrencyType.XTOType).send();
        BigInteger tempTotalBalance = new BigInteger("0");
        tempTotalBalance = tempTotalBalance.add(totalBalanceResponse.getResult().getAvailableBalance()).add(totalBalanceResponse.getResult().getStakingBalance()).add(totalBalanceResponse.getResult().getPredictionRewardBalance()).add(totalBalanceResponse.getResult().getLockingBalance());
        Assert.assertEquals(totalAccount, totalBalanceResponse.getResult().getTotalAccount());
        Assert.assertEquals(totalBalance, totalBalanceResponse.getResult().getTotalBalance());
        Assert.assertEquals(availableBalance, totalBalanceResponse.getResult().getAvailableBalance());
        Assert.assertEquals(stakingBalance, totalBalanceResponse.getResult().getStakingBalance());
        Assert.assertEquals(predictionRewardBalance, totalBalanceResponse.getResult().getPredictionRewardBalance());
        Assert.assertEquals(lockingBalance, totalBalanceResponse.getResult().getLockingBalance());
        Assert.assertEquals(tempTotalBalance, totalBalanceResponse.getResult().getTotalBalance());

        //Fee에 대한 보상값을 계산할때 블록당 전체Fee / 총지분량(ATX) 할때 소숫점이 발생하여, 차이값이 생김.
        //subAmount는 실제로 이전 트랜잭션의 Fee 값이지만 이 메소드를 수행하는 시점에는 해당 Fee에 대한 보상이 이루어지지 않았기 대문에 1의 차이가 발생한다. (보상은 현재 블록의 이전블록 까지 이루어짐)
        BigInteger rewardByBlock = new BigInteger("0");
        for (ExpectedReward expectedReward : expectedRewardResult.getExpectedRewards()) {
            rewardByBlock = rewardByBlock.add(expectedReward.getReward());
        }
        BigInteger pureActualBalance = expectedRewardResult.getTotalBalance().subtract(rewardByBlock);

        assertEquals(expectedRewardResult.getTotalBalance().subtract(subAmount), totalBalanceResponse.getResult().getTotalBalance());
        assertEquals(expectedRewardResult.getTotalDiffReward(), getInitBalance().subtract(pureActualBalance));

        System.out.println(String.format("totalBalance:%s\ntotalReward:%s\ninitBalance:%s\npureActualBalance:%s\nbalanceDiff:%s\nlostAmountByFee:%s",
                NumberUtil.comma(expectedRewardResult.getTotalBalance()),
                NumberUtil.comma(expectedRewardResult.getTotalReward()),
                NumberUtil.comma(getInitBalance()),
                NumberUtil.comma(pureActualBalance),
                CurrencyUtil.generateStringCurrencyUnitToCurrencyUnit(CurrencyUtil.CurrencyType.XTOType, CurrencyUtil.CurrencyType.CoinType, getInitBalance().subtract(pureActualBalance)),
                CurrencyUtil.generateStringCurrencyUnitToCurrencyUnit(CurrencyUtil.CurrencyType.XTOType, CurrencyUtil.CurrencyType.CoinType, expectedRewardResult.getTotalDiffReward())
        ));
    }

    public Map<String, BigInteger> generateSortedMapOfBig(Map<String, BigInteger> val) {
        if (val == null) {
            return null;
        }
        Map<String, BigInteger> mySortedMap = new TreeMap();
        val.forEach((k, v) -> {
            mySortedMap.put(k, v);
        });
        return mySortedMap;
    }

    public Map<String, Boolean> generateSortedMapOfBool(Map<String, Boolean> val) {
        if (val == null) {
            return null;
        }
        Map<String, Boolean> mySortedMap = new TreeMap();

        val.forEach((k, v) -> {
            mySortedMap.put(k, v);
        });
        return mySortedMap;
    }
}
