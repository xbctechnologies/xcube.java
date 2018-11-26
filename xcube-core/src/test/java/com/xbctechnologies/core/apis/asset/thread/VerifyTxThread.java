package com.xbctechnologies.core.apis.asset.thread;

import com.xbctechnologies.core.apis.XCube;
import com.xbctechnologies.core.apis.dto.res.tx.TxReceiptResponse;
import com.xbctechnologies.core.apis.dto.res.tx.TxResponse;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class VerifyTxThread implements Runnable {
    private int totalItemCnt;
    private AtomicInteger itemCnt1;
    private int totalCnt;
    private AtomicInteger completeCnt;
    private List<String> txList;
    private String targetChainId;
    private List<XCube> xCubeList;
    private Object lockedObj;

    public VerifyTxThread(int totalItemCnt, AtomicInteger itemCnt1, int totalCnt, AtomicInteger completeCnt, List<String> txList, String targetChainId, List<XCube> xCubeList, Object lockedObj) {
        this.totalItemCnt = totalItemCnt;
        this.itemCnt1 = itemCnt1;
        this.totalCnt = totalCnt;
        this.completeCnt = completeCnt;
        this.txList = txList;
        this.targetChainId = targetChainId;
        this.xCubeList = xCubeList;
        this.lockedObj = lockedObj;
    }

    @Override
    public void run() {
        //All tx data
        for (String tx : txList) {
            System.out.println(String.format("Tx item - %s/%s (%s)", itemCnt1.addAndGet(1), totalItemCnt, tx));
            TxResponse baseTxData = null;
            TxReceiptResponse baseTxReceiptData = null;
            for (int i = 0; i < xCubeList.size(); i++) {
                if (i == 0) {
                    baseTxData = xCubeList.get(i).getTransaction(null, targetChainId, tx).send();
                    baseTxReceiptData = xCubeList.get(i).getTransactionReceipt(null, targetChainId, tx).send();
                    assertNotNull(baseTxData.getTransaction());
                    assertNotNull(baseTxReceiptData.getTransactionReceipt());
                } else {
                    TxResponse targetTxData = xCubeList.get(i).getTransaction(null, targetChainId, tx).send();
                    TxReceiptResponse targetTxReceiptData = xCubeList.get(i).getTransactionReceipt(null, targetChainId, tx).send();
                    assertEquals(baseTxData.getTransaction(), targetTxData.getTransaction());
                    assertEquals(baseTxReceiptData.getTransactionReceipt(), targetTxReceiptData.getTransactionReceipt());
                    assertNotNull(targetTxData.getTransaction());
                    assertNotNull(targetTxReceiptData.getTransactionReceipt());
                }
            }
        }

        int completedCnt = completeCnt.addAndGet(1);
        if (completedCnt == totalCnt) {
            System.out.println(String.format("Tx - %s/%s", completedCnt, totalCnt));
            synchronized (lockedObj) {
                lockedObj.notify();
            }
        }
        System.out.println(String.format("Tx - %s/%s", completedCnt, totalCnt));
    }
}
