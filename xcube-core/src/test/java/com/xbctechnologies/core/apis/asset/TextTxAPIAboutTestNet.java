package com.xbctechnologies.core.apis.asset;

import com.google.common.collect.Sets;
import com.xbctechnologies.core.TestParent;
import com.xbctechnologies.core.apis.XCube;
import com.xbctechnologies.core.apis.asset.thread.DivideData;
import com.xbctechnologies.core.apis.asset.thread.VerifyAccountThread;
import com.xbctechnologies.core.apis.asset.thread.VerifyBlockThread;
import com.xbctechnologies.core.apis.asset.thread.VerifyTxThread;
import com.xbctechnologies.core.apis.dto.res.LongResponse;
import com.xbctechnologies.core.apis.dto.res.Response;
import com.xbctechnologies.core.apis.dto.res.account.AccountAddrListResponse;
import com.xbctechnologies.core.apis.dto.res.block.BlockResponse;
import com.xbctechnologies.core.apis.dto.res.data.CurrentGovernance;
import com.xbctechnologies.core.apis.dto.res.data.ProgressGovernance;
import com.xbctechnologies.core.apis.dto.res.data.TotalBalanceResponse;
import com.xbctechnologies.core.apis.dto.res.network.NetworkPeersResponse;
import com.xbctechnologies.core.apis.dto.res.validator.SimpleValidatorResponse;
import com.xbctechnologies.core.apis.dto.res.validator.SimpleValidatorsResponse;
import com.xbctechnologies.core.apis.dto.res.validator.ValidatorListResponse;
import com.xbctechnologies.core.apis.dto.res.validator.ValidatorResponse;
import com.xbctechnologies.core.component.rest.RestHttpClient;
import com.xbctechnologies.core.component.rest.RestHttpConfig;
import com.xbctechnologies.core.order.OrderedRunner;
import com.xbctechnologies.core.utils.CurrencyUtil;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static com.xbctechnologies.core.utils.CurrencyUtil.CurrencyType.CoinType;
import static org.junit.Assert.*;

@RunWith(OrderedRunner.class)
public class TextTxAPIAboutTestNet extends TestParent {
    private List<XCube> xCubeList = new ArrayList<>();

    @Before
    public void init() {
        String[] hosts = new String[]{"52.78.40.119:7979", "13.125.47.48:7979", "13.124.186.168:7979", "13.125.233.138:7979"};
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

    public void CompareNodeData() throws Exception {
        //Validator list
        ValidatorListResponse baseValidatorList = null;
        for (int i = 0; i < xCubeList.size(); i++) {
            if (i == 0) {
                baseValidatorList = xCubeList.get(i).getValidatorList(null, targetChainId).send();
            } else {
                ValidatorListResponse targetValidatorList = xCubeList.get(i).getValidatorList(null, targetChainId).send();
                assertEquals(baseValidatorList.getValidatorList(), targetValidatorList.getValidatorList());
            }
        }
        System.out.println("Validator List");

        //SimpleValidator list
        SimpleValidatorsResponse baseSimpleValidatorList = null;
        for (int i = 0; i < xCubeList.size(); i++) {
            if (i == 0) {
                baseSimpleValidatorList = xCubeList.get(i).getSimpleValidators(null, targetChainId).send();
            } else {
                SimpleValidatorsResponse targetSimpleValidatorList = xCubeList.get(i).getSimpleValidators(null, targetChainId).send();
                assertEquals(baseSimpleValidatorList.getSimpleValidatorList(), targetSimpleValidatorList.getSimpleValidatorList());
            }
        }
        System.out.println("SimpleValidator List");

        //Get validator & Get simpleValidator
        for (ValidatorListResponse.Result validator : baseValidatorList.getValidatorList()) {
            ValidatorResponse baseValidator = null;
            SimpleValidatorResponse baseSimpleValidator = null;
            for (int i = 0; i < xCubeList.size(); i++) {
                if (i == 0) {
                    baseValidator = xCubeList.get(i).getValidator(null, targetChainId, validator.getValidatorAccountAddr()).send();
                    baseSimpleValidator = xCubeList.get(i).getSimpleValidator(null, targetChainId, validator.getValidatorAccountAddr()).send();
                } else {
                    ValidatorResponse targetValidator = xCubeList.get(i).getValidator(null, targetChainId, validator.getValidatorAccountAddr()).send();
                    SimpleValidatorResponse targetSimpleValidator = xCubeList.get(i).getSimpleValidator(null, targetChainId, validator.getValidatorAccountAddr()).send();
                    assertEquals(baseValidator.getValidator(), targetValidator.getValidator());
                    assertEquals(baseSimpleValidator.getSimpleValidator(), targetSimpleValidator.getSimpleValidator());
                }
            }
        }
        System.out.println("Validator & SimpleValidator");

        //Get peer cnt
        for (int i = 0; i < xCubeList.size(); i++) {
            LongResponse basePeerCnt = xCubeList.get(i).getPeerCnt(null, targetChainId).send();
            assertTrue(basePeerCnt.getResult() > 0);
        }
        System.out.println("Peer cnt");

        //Get peers
        for (int i = 0; i < xCubeList.size(); i++) {
            NetworkPeersResponse basePeers = xCubeList.get(i).getPeers(null, targetChainId).send();
            assertNotNull(basePeers.getPeers());
        }
        System.out.println("Peers");

        //Get version
        Response baseVersion = null;
        for (int i = 0; i < xCubeList.size(); i++) {
            if (i == 0) {
                baseVersion = xCubeList.get(i).getVersion(null).send();
            } else {
                Response targetVersion = xCubeList.get(i).getVersion(null).send();
                assertEquals(baseVersion.getResult(), targetVersion.getResult());
            }
        }
        System.out.println("Version");

        //Get progress governance
        ProgressGovernance baseProgressGovernance = null;
        for (int i = 0; i < xCubeList.size(); i++) {
            if (i == 0) {
                baseProgressGovernance = xCubeList.get(i).getProgressGovernance(null, targetChainId).send();
                if (baseProgressGovernance.getGR() != null) {
                    baseProgressGovernance.getGR().setStake(generateSortedMapOfBig(baseProgressGovernance.getGR().getStake()));
                    baseProgressGovernance.getGR().setVotingResult(generateSortedMapOfBool(baseProgressGovernance.getGR().getVotingResult()));
                }
            } else {
                ProgressGovernance targetProgressGovernance = xCubeList.get(i).getProgressGovernance(null, targetChainId).send();
                if (targetProgressGovernance.getGR() != null) {
                    targetProgressGovernance.getGR().setStake(generateSortedMapOfBig(targetProgressGovernance.getGR().getStake()));
                    targetProgressGovernance.getGR().setVotingResult(generateSortedMapOfBool(targetProgressGovernance.getGR().getVotingResult()));
                }
                assertEquals(baseProgressGovernance.getGR(), targetProgressGovernance.getGR());
            }
        }
        System.out.println("Progress gr");

        //Get current governance
        CurrentGovernance baseCurrentGovernance = null;
        for (int i = 0; i < xCubeList.size(); i++) {
            if (i == 0) {
                baseCurrentGovernance = xCubeList.get(i).getCurrentGovernance(null, targetChainId).send();
                baseCurrentGovernance.getGR().setEligibleToVoteMap(generateSortedMapOfBig(baseCurrentGovernance.getGR().getEligibleToVoteMap()));
                baseCurrentGovernance.getGR().setVoteHistory(generateSortedMapOfBool(baseCurrentGovernance.getGR().getVoteHistory()));
            } else {
                CurrentGovernance targetCurrentGovernance = xCubeList.get(i).getCurrentGovernance(null, targetChainId).send();
                targetCurrentGovernance.getGR().setEligibleToVoteMap(generateSortedMapOfBig(targetCurrentGovernance.getGR().getEligibleToVoteMap()));
                targetCurrentGovernance.getGR().setVoteHistory(generateSortedMapOfBool(targetCurrentGovernance.getGR().getVoteHistory()));
                assertEquals(baseCurrentGovernance.getGR(), targetCurrentGovernance.getGR());
            }
        }
        System.out.println("Current gr");

        //Get rpc size
        LongResponse baseRpcSize = null;
        for (int i = 0; i < xCubeList.size(); i++) {
            if (i == 0) {
                baseRpcSize = xCubeList.get(i).getRPCSize(null).send();
            } else {
                LongResponse targetRpcSize = xCubeList.get(i).getRPCSize(null).send();
                assertEquals(baseRpcSize.getResult(), targetRpcSize.getResult());
            }
        }
        System.out.println("PRC size");

        //Get total coin
        TotalBalanceResponse baseTotal = null;
        for (int i = 0; i < xCubeList.size(); i++) {
            if (i == 0) {
                baseTotal = xCubeList.get(i).getTotalATX(null, targetChainId, CurrencyUtil.CurrencyType.XTOType).send();
            } else {
                TotalBalanceResponse targetTotal = xCubeList.get(i).getTotalATX(null, targetChainId, CurrencyUtil.CurrencyType.XTOType).send();
                assertEquals(baseTotal.getTotalBalance(), targetTotal.getTotalBalance());
            }
        }
        System.out.println("Total coin" + " : " + baseTotal.getTotalBalance());

        //Reward
        BigInteger initCoinAmount = new BigInteger("0");
        for (int i = 1; i <= 3000; i++) {
            initCoinAmount = initCoinAmount.add(CurrencyUtil.generateXTO(CoinType, i * 100000));
        }

        ValidatorListResponse validatorListResponse = xCubeList.get(0).getValidatorList(null, targetChainId).send();
        BigInteger totalReward = new BigInteger("0");
        for (ValidatorListResponse.Result result : validatorListResponse.getResult()) {
            if (result.getRewardBlocks() == null) {
                continue;
            }
            for (ValidatorListResponse.Result.Reward reward : result.getRewardBlocks()) {
                totalReward = totalReward.add(reward.getRewardPerCoin()
                        .multiply(new BigInteger(String.valueOf(reward.getEndBlockNo() - reward.getStartBlockNo()) + 1))
                        .multiply(CurrencyUtil.generateCurrencyUnitToCurrencyUnit(CurrencyUtil.CurrencyType.XTOType, CurrencyUtil.CurrencyType.CoinType, result.getTotalBondingBalance()))
                );
            }
        }
        assertEquals(initCoinAmount.add(totalReward), baseTotal.getTotalBalance().getTotalBalance());
    }

    private void verifyTestNet() throws Exception {
        Object lockedObj = new Object();

        AccountAddrListResponse accountAddrListResponse = xCubeList.get(0).getListAccount(null).send();
        List<String> accounts = accountAddrListResponse.getAccountList();
        List<List<String>> dividedAccounts = DivideData.divideListString(accounts, 300);

        ExecutorService execService = Executors.newFixedThreadPool(dividedAccounts.size());
        AtomicInteger completeCnt = new AtomicInteger();
        int totalItemCnt = accounts.size();
        AtomicInteger itemCnt1 = new AtomicInteger();
        AtomicInteger itemCnt2 = new AtomicInteger();
        AtomicInteger itemCnt3 = new AtomicInteger();

        synchronized (lockedObj) {
            for (List<String> data : dividedAccounts) {
                execService.execute(new VerifyAccountThread(totalItemCnt, itemCnt1, itemCnt2, itemCnt3, dividedAccounts.size(), completeCnt, data, targetChainId, xCubeList, lockedObj));
            }
            lockedObj.wait();
        }

        //Block Data
        BlockResponse baseLatestBlockData = null;
        for (int i = 0; i < xCubeList.size(); i++) {
            if (i == 0) {
                baseLatestBlockData = xCubeList.get(i).getBlockLatestBlock(null, targetChainId).send();
            } else {
                BlockResponse targetLatestBlockData = xCubeList.get(i).getBlockLatestBlock(null, targetChainId).send();
                assertEquals(baseLatestBlockData.getBlock(), targetLatestBlockData.getBlock());
            }
        }
        List<DivideData.LongData> dividedBlocks = DivideData.divideListLong(baseLatestBlockData.getBlock().getBlockNo(), 300);
        execService = Executors.newFixedThreadPool(dividedBlocks.size());
        List<String> txList = new CopyOnWriteArrayList();
        Set txSet = Sets.newConcurrentHashSet();
        synchronized (lockedObj) {
            long totalLongItemCnt = baseLatestBlockData.getBlock().getBlockNo();
            itemCnt1 = new AtomicInteger();
            completeCnt = new AtomicInteger();
            for (DivideData.LongData longData : dividedBlocks) {
                execService.execute(new VerifyBlockThread(totalLongItemCnt, itemCnt1, dividedBlocks.size(), completeCnt, txList, txSet, longData, targetChainId, xCubeList, lockedObj));
            }
            lockedObj.wait();
        }

        //Tx Data
        List<List<String>> dividedTx = DivideData.divideListString(txList, 10000);
        synchronized (lockedObj) {
            itemCnt1 = new AtomicInteger();
            completeCnt = new AtomicInteger();
            for (List<String> data : dividedTx) {
                execService.execute(new VerifyTxThread(txList.size(), itemCnt1, dividedTx.size(), completeCnt, data, targetChainId, xCubeList, lockedObj));
            }
            lockedObj.wait();
        }
    }

    @Test
    public void ValidateAllData() throws Exception {
        verifyTestNet();
        CompareNodeData();
    }
}
