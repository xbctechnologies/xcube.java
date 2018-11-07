package com.xbctechnologies.core;

import com.xbctechnologies.core.apis.TestXCube;
import com.xbctechnologies.core.apis.XCube;
import com.xbctechnologies.core.apis.dto.res.account.AccountBalanceResponse;
import com.xbctechnologies.core.apis.dto.res.data.CurrentGovernance;
import com.xbctechnologies.core.apis.dto.res.data.ProgressGovernance;
import com.xbctechnologies.core.apis.dto.res.data.TotalAtxResponse;
import com.xbctechnologies.core.apis.dto.res.data.ValidatorListResponse;
import com.xbctechnologies.core.apis.dto.xtypes.TxGRProposalBody;
import com.xbctechnologies.core.component.rest.RestHttpClient;
import com.xbctechnologies.core.component.rest.RestHttpConfig;
import com.xbctechnologies.core.utils.Base64Util;
import com.xbctechnologies.core.utils.CurrencyUtil;
import com.xbctechnologies.core.utils.NumberUtil;
import lombok.AllArgsConstructor;
import lombok.Data;
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

    private static Map<Long, Map<String, BigInteger>> subRewardMap = new HashMap<>();
    public final String targetChainId = "1T";

    public final String sender = "0x9ac601f1a9c8385cb1fd794d030898168b0b617a";
    public BigInteger senderAmount = CurrencyUtil.generateXTO(CoinType, 7000000);
    public final String receiver = "0x7826d36525a285072fd8fe7cbe1597013d8d9761";
    public BigInteger receiverAmount = CurrencyUtil.generateXTO(CoinType, 4000000);
    public final String validator = "0xd52ff6084b6dec53b74b2ac9133fe3541709fa7f";
    public BigInteger validatorAmount = CurrencyUtil.generateXTO(CoinType, 10000000);

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
    @AllArgsConstructor
    public class Unstaking {
        private boolean isOnlyDelegator;
        private long startBlockNo;
        private long endBlockNo;
        private BigInteger unstaking;
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

    public void calculateExpectedReward(ExpectedRewardResult expectedRewardResult, ExpectedReward expectedReward) {
        BigInteger totalStaking = new BigInteger(expectedReward.getTotalStakingOfValidator().toString()).add(expectedReward.getTotalStakingOfDelegator());
        totalStaking = CurrencyUtil.generateCurrencyUnitToCurrencyUnit(XTOType, CoinType, totalStaking);
        expectedReward.setReward(totalStaking.multiply(rewardXtoPerCoin));

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

    /**
     * Unbonding, Undelegating에 따른 보상값이 차감될때.
     *
     * @return
     */
    public BigInteger calculateRewardWithExpectedAndActual(long blockCnt, BigInteger totalStaking, BigInteger totalStakingOfValidator, BigInteger totalExpectedRewardAmount, BigInteger fee, List<Unstaking> unstakingList) {
        subRewardMap = new HashMap<>();
        ValidatorListResponse validatorListResponse = xCube.getValidatorList(null, targetChainId).send();
        for (ValidatorListResponse.Result result : validatorListResponse.getResult()) {
            //Validator로 등록하였지만 보상이 없는 경우 (블록생성에 서명을 하지 않음. - 노드가 기동중이 아님)
            if (result.getRewardBlocks() == null) {
                continue;
            }
        }

        /*for (ValidatorListResponse.Result result : validatorListResponse.getResult()) {
            //Validator로 등록하였지만 보상이 없는 경우
            if (result.getRewardBlocks() == null) {
                continue;
            }
            for (ValidatorListResponse.Result.Reward reward : result.getRewardBlocks()) {
                Map<String, BigInteger> expectedRewardMap = argsForCalculate.get(reward.getBlockNo());

                BigInteger bondingBalance = new BigInteger(reward.getBondingBalance().toString());
                BigInteger bondingBalanceOfValidator = new BigInteger(reward.getBondingBalanceOfValidator().toString());

                BigInteger cumulativeUnstakingOfValidator = new BigInteger("0");
                BigInteger cumulativeUnstakingOfDelegator = new BigInteger("0");
                if (unstakingList != null && unstakingList.size() > 0) {
                    for (Unstaking unstaking : unstakingList) {
                        if (reward.getBlockNo() >= unstaking.startBlockNo && reward.getBlockNo() <= unstaking.endBlockNo) {
                            if (!unstaking.isOnlyDelegator) {
                                cumulativeUnstakingOfValidator = cumulativeUnstakingOfValidator.add(unstaking.unstaking);
                            }
                            cumulativeUnstakingOfDelegator = cumulativeUnstakingOfDelegator.add(unstaking.unstaking);
                        }
                    }
                    bondingBalanceOfValidator = bondingBalanceOfValidator.add(cumulativeUnstakingOfValidator);
                    bondingBalance = bondingBalance.add(cumulativeUnstakingOfDelegator);
                }
                BigInteger bondingBalanceOfCoinType = CurrencyUtil.generateCurrencyUnitToCurrencyUnit(CurrencyUtil.CurrencyType.XTOType, CurrencyUtil.CurrencyType.CoinType, bondingBalance);
                BigInteger bondingBalanceOfValidatorOfCoinType = CurrencyUtil.generateCurrencyUnitToCurrencyUnit(CurrencyUtil.CurrencyType.XTOType, CurrencyUtil.CurrencyType.CoinType, bondingBalanceOfValidator);

                //Remove remainder from the original reward
                BigInteger unitForReward = expectedRewardMap.get("expectedRewardWithFee").divide(bondingBalanceOfCoinType);
                BigInteger totalReward = unitForReward.multiply(bondingBalanceOfCoinType);

                //Calculate reward ratio of validator and delegator
                BigInteger rewardUnitForParticipant = new BigInteger(totalReward.toString()).divide(new BigInteger("100"));

                BigInteger expectedValidatorReward = new BigInteger(rewardUnitForParticipant.toString())
                        .multiply(new BigInteger(String.valueOf(reward.getValidatorRewardRate())))
                        .divide(bondingBalanceOfValidatorOfCoinType)
                        .multiply(bondingBalanceOfValidatorOfCoinType);

                BigInteger expectedDelegatorReward = new BigInteger(rewardUnitForParticipant.toString())
                        .multiply(new BigInteger(String.valueOf(reward.getDelegatorRewardRate())))
                        .divide(bondingBalanceOfCoinType)
                        .multiply(bondingBalanceOfCoinType);

                //Calculate remain balance after unstaking
                BigInteger expectedGivenReward = expectedValidatorReward.add(expectedDelegatorReward);
                BigInteger expectedRemainValidatorReward = new BigInteger(expectedValidatorReward.toString());
                BigInteger expectedRemainDelegatorReward = new BigInteger(expectedDelegatorReward.toString());

                BigInteger payValidatorReward = null;
                BigInteger payDelegatorReward = null;
                if (unstakingList != null && unstakingList.size() > 0) {
                    payValidatorReward = expectedValidatorReward.divide(bondingBalanceOfValidatorOfCoinType)
                            .multiply(CurrencyUtil.generateCurrencyUnitToCurrencyUnit(CurrencyUtil.CurrencyType.XTOType, CurrencyUtil.CurrencyType.CoinType, cumulativeUnstakingOfValidator));
                    payDelegatorReward = expectedDelegatorReward.divide(bondingBalanceOfCoinType)
                            .multiply(CurrencyUtil.generateCurrencyUnitToCurrencyUnit(CurrencyUtil.CurrencyType.XTOType, CurrencyUtil.CurrencyType.CoinType, cumulativeUnstakingOfDelegator));

                    expectedGivenReward = expectedGivenReward.subtract(payValidatorReward).subtract(payDelegatorReward);
                    expectedRemainValidatorReward = expectedRemainValidatorReward.subtract(payValidatorReward);
                    expectedRemainDelegatorReward = expectedRemainDelegatorReward.subtract(payDelegatorReward);
                }

                Map<String, BigInteger> rewardMap = new HashMap<>();
                rewardMap.put("expectedPayValidatorReward", payValidatorReward);
                rewardMap.put("expectedPayDelegatorReward", payDelegatorReward);

                rewardMap.put("expectedGivenReward", expectedGivenReward);
                rewardMap.put("actualGivenReward", reward.getRewardBalanceForValidator().add(reward.getRewardBalanceForDelegator()));

                rewardMap.put("expectedRemainValidatorReward", expectedRemainValidatorReward);
                rewardMap.put("expectedRemainDelegatorReward", expectedRemainDelegatorReward);
                rewardMap.put("actualRemainValidatorReward", reward.getRewardBalanceForValidator());
                rewardMap.put("actualRemainDelegatorReward", reward.getRewardBalanceForDelegator());

                rewardMap.put("subReward", expectedRewardMap.get("expectedRewardWithFee").subtract(expectedValidatorReward.add(expectedDelegatorReward)));
                subRewardMap.put(reward.getBlockNo(), rewardMap);

                totalAmount = totalAmount.add(expectedRewardMap.get("addedReward"));

                assertEquals(rewardMap.get("expectedGivenReward"), rewardMap.get("actualGivenReward"));
                assertEquals(rewardMap.get("expectedRemainValidatorReward"), rewardMap.get("actualRemainValidatorReward"));
                assertEquals(rewardMap.get("expectedRemainDelegatorReward"), rewardMap.get("actualRemainDelegatorReward"));
            }
        }*/

        return totalExpectedRewardAmount;
    }

    public BigInteger printRewardMap(boolean isDisplay) {
        final List<BigInteger> subReward = new ArrayList<>();
        subReward.add(new BigInteger("0"));
        subRewardMap.forEach((blockNo, rewardMap) -> {
            subReward.set(0, subReward.get(0).add(rewardMap.get("subReward")));
            if (isDisplay) {
                System.out.println(
                        String.format("blockNo:%s, subReward:%s, expectedPayValidatorReward:%s, expectedPayDelegatorReward:%s, expectedGivenReward:%s, actualGivenReward:%s, expectedRemainValidatorReward:%s, actualRemainValidatorReward:%s, expectedRemainDelegatorReward:%s, actualRemainDelegatorReward:%s",
                                blockNo,
                                NumberUtil.comma(rewardMap.get("subReward")),
                                NumberUtil.comma(rewardMap.get("expectedPayValidatorReward")),
                                NumberUtil.comma(rewardMap.get("expectedPayDelegatorReward")),
                                NumberUtil.comma(rewardMap.get("expectedGivenReward")),
                                NumberUtil.comma(rewardMap.get("actualGivenReward")),
                                NumberUtil.comma(rewardMap.get("expectedRemainValidatorReward")),
                                NumberUtil.comma(rewardMap.get("actualRemainValidatorReward")),
                                NumberUtil.comma(rewardMap.get("expectedRemainDelegatorReward")),
                                NumberUtil.comma(rewardMap.get("actualRemainDelegatorReward"))
                        ));
            }
        });

        return subReward.get(0);
    }

    /**
     * 각 블록No에서의 예상되는 Reward 셋팅
     *
     * @param addedReward
     * @param fee         이전 블록생성시 발생한 Fee
     * @return
     */
    public Map<String, BigInteger> putRewardArgs(String addedReward, String fee) {
        Map<String, BigInteger> args = new HashMap<>();
        args.put("addedReward", CurrencyUtil.generateXTO(CurrencyUtil.CurrencyType.CoinType, NumberUtil.generateStringToBigInteger(addedReward)));
        args.put("expectedRewardWithFee", args.get("addedReward").add(CurrencyUtil.generateXTO(CurrencyUtil.CurrencyType.CoinType, NumberUtil.generateStringToBigInteger(fee))));
        return args;
    }
}
