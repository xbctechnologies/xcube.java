package com.xbctechnologies.core.apis.asset;

import com.xbctechnologies.core.TestParent;
import com.xbctechnologies.core.apis.XCube;
import com.xbctechnologies.core.apis.dto.res.BoolResponse;
import com.xbctechnologies.core.apis.dto.res.LongResponse;
import com.xbctechnologies.core.apis.dto.res.Response;
import com.xbctechnologies.core.apis.dto.res.account.AccountAddrListResponse;
import com.xbctechnologies.core.apis.dto.res.account.AccountBalanceResponse;
import com.xbctechnologies.core.apis.dto.res.account.AccountBondInfoResponse;
import com.xbctechnologies.core.apis.dto.res.account.AccountResponse;
import com.xbctechnologies.core.apis.dto.res.block.BlockResponse;
import com.xbctechnologies.core.apis.dto.res.block.BlockTxCntResponse;
import com.xbctechnologies.core.apis.dto.res.data.CurrentGovernance;
import com.xbctechnologies.core.apis.dto.res.data.ProgressGovernance;
import com.xbctechnologies.core.apis.dto.res.data.TotalBalanceResponse;
import com.xbctechnologies.core.apis.dto.res.network.NetworkPeersResponse;
import com.xbctechnologies.core.apis.dto.res.tx.TxReceiptResponse;
import com.xbctechnologies.core.apis.dto.res.tx.TxResponse;
import com.xbctechnologies.core.apis.dto.res.validator.*;
import com.xbctechnologies.core.component.rest.RestHttpClient;
import com.xbctechnologies.core.component.rest.RestHttpConfig;
import com.xbctechnologies.core.order.OrderedRunner;
import com.xbctechnologies.core.utils.CurrencyUtil;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.List;

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

    public void CompareNodeData(List<String> accounts, String newAccount) throws Exception {
        //Balance Data
        if (newAccount != null) {
            accounts.add(newAccount);
        }
        int tempCnt = 0;
        for (String account : accounts) {
            System.out.println(String.format("Balance - %s/%s", ++tempCnt, accounts.size()));
            AccountBalanceResponse baseBalanceData = null;
            for (int i = 0; i < xCubeList.size(); i++) {
                if (i == 0) {
                    baseBalanceData = xCubeList.get(i).getBalance(null, targetChainId, account, CurrencyUtil.CurrencyType.XTOType).send();
                } else {
                    AccountBalanceResponse targetBalanceData = xCubeList.get(i).getBalance(null, targetChainId, account, CurrencyUtil.CurrencyType.XTOType).send();
                    assertEquals(baseBalanceData.getBalance(), targetBalanceData.getBalance());
                }
            }
        }

        //Bonding Data
        tempCnt = 0;
        for (String account : accounts) {
            System.out.println(String.format("Bonding - %s/%s", ++tempCnt, accounts.size()));
            AccountBondInfoResponse baseBondingData = null;
            for (int i = 0; i < xCubeList.size(); i++) {
                if (i == 0) {
                    baseBondingData = xCubeList.get(i).getBonding(null, targetChainId, account, CurrencyUtil.CurrencyType.XTOType).send();
                } else {
                    AccountBondInfoResponse targetBondingData = xCubeList.get(i).getBonding(null, targetChainId, account, CurrencyUtil.CurrencyType.XTOType).send();
                    assertEquals(baseBondingData.getBonding(), targetBondingData.getBonding());
                }
            }
        }

        //Account Data
        tempCnt = 0;
        for (String account : accounts) {
            System.out.println(String.format("Account - %s/%s", ++tempCnt, accounts.size()));
            AccountResponse baseAccountData = null;
            for (int i = 0; i < xCubeList.size(); i++) {
                if (i == 0) {
                    baseAccountData = xCubeList.get(i).getAccount(null, targetChainId, account).send();
                } else {
                    AccountResponse targetAccountData = xCubeList.get(i).getAccount(null, targetChainId, account).send();
                    assertEquals(baseAccountData.getAccount(), targetAccountData.getAccount());
                }
            }
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

        //All block data
        tempCnt = 0;
        List<String> txList = new ArrayList<>();
        for (int blockNo = 1; blockNo <= baseLatestBlockData.getBlock().getBlockNo(); blockNo++) {
            System.out.println(String.format("AllBlock tx - %s/%s", (++tempCnt), txList.size()));
            BlockResponse baseBlockData = null;
            BlockTxCntResponse baseBlockTxCntData = null;
            for (int i = 0; i < xCubeList.size(); i++) {
                if (i == 0) {
                    baseBlockData = xCubeList.get(i).getBlockByNumber(null, targetChainId, blockNo).send();
                    baseBlockTxCntData = xCubeList.get(i).getBlockTxCount(null, targetChainId, blockNo).send();
                    System.out.println(String.format("Validate block - blockNo:%s, txCnt:%s, accumulatedTxCnt:%s", blockNo, baseBlockData.getBlock().getNumTxs(), baseBlockData.getBlock().getTotal_txs()));
                    if (baseBlockData.getBlock().getTransactions() != null) {
                        txList.addAll(baseBlockData.getBlock().getTransactions());
                    }
                } else {
                    BlockResponse targetBlockData = xCubeList.get(i).getBlockByNumber(null, targetChainId, blockNo).send();
                    BlockTxCntResponse targetBlockTxCntData = xCubeList.get(i).getBlockTxCount(null, targetChainId, blockNo).send();
                    assertEquals(baseBlockData.getBlock(), targetBlockData.getBlock());
                    assertEquals(baseBlockTxCntData.getTxCnt(), targetBlockTxCntData.getTxCnt());
                }
            }
        }

        //All tx data
        tempCnt = 0;
        for (String tx : txList) {
            System.out.println(String.format("Transaction - %s/%s", (++tempCnt), txList.size()));
            TxResponse baseTxData = null;
            TxReceiptResponse baseTxReceiptData = null;
            for (int i = 0; i < xCubeList.size(); i++) {
                if (i == 0) {
                    baseTxData = xCubeList.get(i).getTransaction(null, targetChainId, tx).send();
                    baseTxReceiptData = xCubeList.get(i).getTransactionReceipt(null, targetChainId, tx).send();
                } else {
                    TxResponse targetTxData = xCubeList.get(i).getTransaction(null, targetChainId, tx).send();
                    TxReceiptResponse targetTxReceiptData = xCubeList.get(i).getTransactionReceipt(null, targetChainId, tx).send();
                    assertEquals(baseTxData.getTransaction(), targetTxData.getTransaction());
                    assertEquals(baseTxReceiptData.getTransactionReceipt(), targetTxReceiptData.getTransactionReceipt());
                }
            }
        }

        //Is validator
        tempCnt = 0;
        for (String account : accounts) {
            System.out.println(String.format("IsValidator - %s/%s", ++tempCnt, accounts.size()));
            BoolResponse baseIsValidator = null;
            for (int i = 0; i < xCubeList.size(); i++) {
                if (i == 0) {
                    baseIsValidator = xCubeList.get(i).isValidator(null, targetChainId, account).send();
                } else {
                    BoolResponse targetIsValidator = xCubeList.get(i).isValidator(null, targetChainId, account).send();
                    assertEquals(baseIsValidator.isValidator(), targetIsValidator.isValidator());
                }
            }
        }

        //Validator of
        tempCnt = 0;
        for (String account : accounts) {
            System.out.println(String.format("ValidatorOf - %s/%s", ++tempCnt, accounts.size()));
            ValidatorBondResponse baseValidatorBond = null;
            for (int i = 0; i < xCubeList.size(); i++) {
                if (i == 0) {
                    baseValidatorBond = xCubeList.get(i).getValidatorsOf(null, targetChainId, account).send();
                } else {
                    ValidatorBondResponse targetValidatorBond = xCubeList.get(i).getValidatorsOf(null, targetChainId, account).send();
                    assertEquals(baseValidatorBond.getBonding(), targetValidatorBond.getBonding());
                }
            }
        }

        //Validator set
        for (int blockNo = 1; blockNo <= baseLatestBlockData.getBlock().getBlockNo(); blockNo++) {
            ValidatorSetResponse baseValidatorSet = null;
            for (int i = 0; i < xCubeList.size(); i++) {
                if (i == 0) {
                    baseValidatorSet = xCubeList.get(i).getValidatorSet(null, targetChainId, blockNo).send();
                } else {
                    ValidatorSetResponse targetValidatorSet = xCubeList.get(i).getValidatorSet(null, targetChainId, blockNo).send();
                    assertEquals(baseValidatorSet.getValidatorSet(), targetValidatorSet.getValidatorSet());
                }
            }
        }

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

        //Get peer cnt
        for (int i = 0; i < xCubeList.size(); i++) {
            LongResponse basePeerCnt = xCubeList.get(i).getPeerCnt(null, targetChainId).send();
            assertTrue(basePeerCnt.getResult() > 0);
        }

        //Get peers
        for (int i = 0; i < xCubeList.size(); i++) {
            NetworkPeersResponse basePeers = xCubeList.get(i).getPeers(null, targetChainId).send();
            assertNotNull(basePeers.getPeers());
        }

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
    }

    @Test
    public void ValidateAllData() throws Exception {
        AccountAddrListResponse accountAddrListResponse = xCubeList.get(0).getListAccount(null).send();
        CompareNodeData(accountAddrListResponse.getAccountList(), null);
//        for (XCube client : xCubeList) {
//        }
    }
}
