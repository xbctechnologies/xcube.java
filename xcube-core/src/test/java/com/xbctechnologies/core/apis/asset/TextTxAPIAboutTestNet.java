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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
        Set<String> txSet = new HashSet<>();
        List<String> txList = new ArrayList<>();
        for (int blockNo = 1; blockNo <= baseLatestBlockData.getBlock().getBlockNo(); blockNo++) {
            BlockResponse baseBlockData = null;
            BlockTxCntResponse baseBlockTxCntData = null;
            for (int i = 0; i < xCubeList.size(); i++) {
                if (i == 0) {
                    baseBlockData = xCubeList.get(i).getBlockByNumber(null, targetChainId, blockNo).send();
                    baseBlockTxCntData = xCubeList.get(i).getBlockTxCount(null, targetChainId, blockNo).send();
                    System.out.println(String.format("Validate block - blockNo:%s, txCnt:%s, accumulatedTxCnt:%s (%s/%s)", blockNo, baseBlockData.getBlock().getNumTxs(), baseBlockData.getBlock().getTotal_txs(), blockNo, baseLatestBlockData.getBlock().getBlockNo()));
                    if (baseBlockData.getBlock().getTransactions() != null) {
                        txSet.addAll(baseBlockData.getBlock().getTransactions());
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

        //Check duplication tx
        if (txList.size() != txSet.size()) {
            fail(String.format("Duplication transaction - list:%s, set:%s", txList.size(), txSet.size()));
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
//        String[] users = new String[]{
//                "0xa4f39f99f293d9588ed9ed4fcd917ada6d2ab8df",
//                "0x92141ef54df1403ac872d1ef582b3b043596a8b9",
//                "0xbffa85d9a7ba93669be80bab277999251e323a80",
//                "0xcc0760d6da605d250fdd486d2628b3dbdaeb8ee6",
//                "0x7fd884a2ccc301a806a17be44617ae0fe125ef85",
//                "0xb4eee2fb49756a43403e465dc02bac88ab68d544",
//                "0x00462d62c5e84738014eb730554bcffdf57c140b",
//                "0x584ae6d5c2ec4649d5ae571891b9c5a4988d99c8",
//                "0x4d258c49e32bd40413dc16235ab0d4ac6a620173",
//                "0xe658b9746c34168f5292c05e0d6ce8ced7f1e891",
//                "0x4d30463545faa263ea20d0cec185f68f48476ae3",
//                "0xd69206769ef0537019319e43be1c89e5beebbedd",
//                "0xf8556f46b75ff79ccaa17a03d5b82a78d363d795",
//                "0xe27f13829ef902662554d70b90eef496bfb15907",
//                "0x44cc6f41486d2b11a8f230f14fb9588c23c4f81c",
//                "0xcfd69ef6237799949e1a456f3e7dd0c8e34525f4",
//                "0x85796267213fb295b1bc4952745ff7eddab06fb6",
//                "0x20205e362318fc7ff100853aefe9fb0716172963",
//                "0x7c50f0b6e3c45869a4cfd147f727ed287271d2e4",
//                "0xc949f4da2e1fb8134edea0dc9db5e6742cd4b25f",
//                "0xb05068fedf5b759696d5cbd31337024af6b06f77",
//                "0x6c6a579841ce209ebb211f33fe33a6c54869b4a8",
//                "0xc26ab8a86fcf5acbd88ad82acac6b5e3d984e6a7",
//                "0x3dc5739f8faa0b7ce9ba34741baadb6e7383cb67",
//                "0x4fe640cd1a13ec1da16abb32504c1a5e12bc1212",
//                "0x2d293be50979af840437596d6d92e3d98bd9625a",
//                "0x833693dfd0f7fd5d18f7070c314e44adb1425fb2",
//                "0x7e4333c81b57e049af3164a894916a48d405a7b0",
//                "0xf1ee09df6de50084db09b5db59f4c64558319fb3",
//                "0x07f45375dcc4990201f9fa76c8851d48d7220697",
//                "0xb4be309060691b3a4b99eee7dd58da8a6dcc72d1",
//                "0xae7c1771491d0f9d42705f26ec130bbd08c80268",
//                "0x386c930854a49b54e744986bd7737d6e69fe33d7",
//                "0x97c05564844e240c2e7d90c526cf0568c119f6e2",
//                "0x7f563a3f9027d82d8c95c74aefbd8b957c9acdee",
//                "0xd06b6afbd72d611af93b67b35aefe81a99546ea6",
//                "0x14e6aa7965a01006127cdbb3f6f6fbd5cc6b0205",
//                "0xc49285f2d04cbd16675267dff359f2719a5da173",
//                "0xf8ebbc6fad9d619b7b1b11ac26a6610af894c2e6",
//                "0x566e1cd35e61e3ac891d9583bf527dadc830dd1f",
//                "0xa0f9804a58ad4d2c8c7755046c3115f9481eff21",
//                "0x53f0236e3c646ef6c4cfe2b0c8c5f0ff02423aef",
//                "0xe895b93bfc067ee7b3572df62e3921db06e4af7b",
//                "0x82615d2b8c6b706024282716ff414ec2547bb869",
//                "0xc55267368987a97278c9e9d960314d349efa7fd5",
//                "0x8b5d04c6fabf4537b9866ed2171feb5b3d384e70",
//                "0xc07bf930e4ed4bbb4edb970297356354676d93e2",
//                "0x8de617b5b97c0242441274ceddbaeb5c68a1c71a",
//                "0xdf8116cd8f7350243b1bbfe9bdd5181b6c5e1dc5",
//                "0x296d90e8326092abbb6b7394d1619ace015b8cea",
//                "0x66b2a1830f06a9f1774152b60018b3e7afa220eb",
//                "0x758920bcdb07eaa8df05e52a68129a43acf92c73",
//                "0x3d0a8a4499430fb9eeeb1616895fd13fee658cec",
//                "0xbce9896a5ba5a0c6eb0579f712b204f9e44ca63b",
//                "0x38ad3a0f45fba3e54bd4ffd73a336ab464b8fab9",
//                "0x6435db0c3463fac518519639033567a526814349",
//                "0xe3756e3b80cb4599a58ecc1784c048998704e7b0",
//                "0xd487b320e16a7504b44baf55ff9ffb11db93340f",
//                "0x30474c73f1a6c170adc9ac007135349edbde10a2",
//                "0xa4ec3fd12fec60843801f14a821b429f6127c03c",
//                "0x7628a3a4822c294c792ba0d8f58b31eaecefec99",
//                "0xb5bcfdd87d29d1e735167cac7694d522019fcc4f",
//                "0xa7723a2fa19506c9364badf8d57caf9bab2f2624",
//                "0xcaba5fba6d3a6e3c80bdc41ef0230241b0060f2e",
//                "0x41c743011f54822a7fd8f694662a86101994db46",
//                "0x75e3e81c194599d0feab560bd5c71407c9393212",
//                "0x65b233e82192d0c321c829265a9386a93515ec8c",
//                "0xa93be2bbe9af3425f0a5bd6eb7128f2a80c6e441",
//                "0xae8bbb437706c177deebe2a92425166f4f0483f7",
//                "0x4ae56c0925059cd9695fad8b356f60023b473050",
//                "0x79b92b278a7d9b5cc0d16abddda3004c47cd7ad9",
//                "0x59b485956ef65657131faa06668baf0f77574702",
//                "0x0185ae9ba78ff10871d0908d586e9d4d3d62d87c",
//                "0xe16f9e6e5ffe73f22ecb54d2050bc243fb6ecf37",
//                "0x04483d630bce7d5bcc82f98be9ff4d223d0cb0bf",
//                "0xa9be0a3d7b0d2c86087172f48dbf32280e00ce96",
//                "0x57c46f9a6c11a9ad5eea014c921a2cc45d247187",
//                "0x9711ff4421467dde492da31894982464fac25ab4",
//                "0xd4c1e6fe9997af52cef688c86df25e9bf37a648c",
//                "0xdc5797f6590e602c11cd0a1a309a7e0241c35f4b",
//                "0x9cb910e6861bed272b79647f01f4e1d235a9d755",
//                "0x5a09c7bd90d6c8a7e9225cd1659751887791c537",
//                "0x7a0be3ac8a5d058013c5fdff951f332eb8cddffc",
//                "0xeaadd270779a4e1d4fc77c874cd9d6d85d4536d3",
//                "0xdbca97a2b5ebc02e02933d4f203412f7fc2d2de1",
//                "0x1bfe121ec5744e131c5ba9c3391d4d28f49b7f6d",
//                "0xee8b7e94ff6f5d7442bcee718d3cc139a14e8268",
//                "0xad9f2fe4ca30e913827c709581b57f951dc2799d",
//                "0x93b87359c4632ffaf6aeb6fbc1d01388cfae941d",
//                "0x5639ed2dccc8d0ba2ec6a28f906b21313957a028",
//                "0xe26a7fbfaf7de61ef4c7110d01566789c8e00c01",
//                "0x433200e7de859e05d7da5d9e0fc582c8bfa21db3",
//                "0x565fc30f64492b7f577bf583d233702fbfdaae80",
//                "0x325064ab0c7248f4d33f354072e55c931db5dbfd",
//                "0x4e7ae88f9b174e1004d2e2739e273f2c051ccedd",
//                "0xdf708b1f42e3826853f1f5aa676a3e5725e0858e",
//                "0x91b2dde380a0b68eb748685057ef5f59b4aa54db",
//                "0x7dbc3fde61d2f4637e518ec1c291917e12245e42",
//                "0xfb7359ce594f4631ac699c25b4c695b0892ec077",
//                "0x6c0311d652c2e0b3a4f8c441c2344367194e486c",
//        };
//
//        long cnt = 0;
//        for (String user : users) {
//            AccountResponse accountResponse = xCubeList.get(0).getAccount(null, targetChainId, user).send();
//            cnt += accountResponse.getAccount().getTxNo();
//        }
//        System.out.println(xCubeList.get(0).getBlockLatestBlock(null, targetChainId).send().getBlock().getTotal_txs());
//        System.out.println(cnt);
    }
}
