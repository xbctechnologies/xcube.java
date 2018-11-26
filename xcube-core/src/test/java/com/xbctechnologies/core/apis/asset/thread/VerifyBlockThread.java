package com.xbctechnologies.core.apis.asset.thread;

import com.xbctechnologies.core.apis.XCube;
import com.xbctechnologies.core.apis.dto.res.block.BlockResponse;
import com.xbctechnologies.core.apis.dto.res.block.BlockTxCntResponse;
import com.xbctechnologies.core.apis.dto.res.validator.ValidatorSetResponse;

import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class VerifyBlockThread implements Runnable {
    private long totalLongItemCnt;
    private AtomicInteger itemCnt1;

    private int totalCnt;
    private AtomicInteger completeCnt;
    private List<String> txList;
    private Set txSet;
    private DivideData.LongData longData;
    private String targetChainId;
    private List<XCube> xCubeList;
    private Object lockedObj;

    public VerifyBlockThread(long totalLongItemCnt, AtomicInteger itemCnt1, int totalCnt, AtomicInteger completeCnt, List<String> txList, Set txSet, DivideData.LongData longData, String targetChainId, List<XCube> xCubeList, Object lockedObj) {
        this.totalLongItemCnt = totalLongItemCnt;
        this.itemCnt1 = itemCnt1;
        this.totalCnt = totalCnt;
        this.completeCnt = completeCnt;
        this.txList = txList;
        this.txSet = txSet;
        this.longData = longData;
        this.targetChainId = targetChainId;
        this.xCubeList = xCubeList;
        this.lockedObj = lockedObj;
    }

    @Override
    public void run() {
        //All block data
        for (long blockNo = longData.start; blockNo <= longData.end; blockNo++) {
            BlockResponse baseBlockData = null;
            BlockTxCntResponse baseBlockTxCntData = null;
            System.out.println(String.format("Block item - %s/%s", itemCnt1.addAndGet(1), totalLongItemCnt));
            for (int i = 0; i < xCubeList.size(); i++) {
                if (i == 0) {
                    baseBlockData = xCubeList.get(i).getBlockByNumber(null, targetChainId, blockNo).send();
                    baseBlockTxCntData = xCubeList.get(i).getBlockTxCount(null, targetChainId, blockNo).send();
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

            //Validator set
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

        int completedCnt = completeCnt.addAndGet(1);
        if (completedCnt == totalCnt) {
            System.out.println(String.format("Block - %s/%s", completedCnt, totalCnt));
            //Check duplication tx
            if (txList.size() != txSet.size()) {
                fail(String.format("Duplication transaction - list:%s, set:%s", txList.size(), txSet.size()));
            }
            synchronized (lockedObj) {
                lockedObj.notify();
            }
        }
        System.out.println(String.format("Block - %s/%s", completedCnt, totalCnt));
    }
}
