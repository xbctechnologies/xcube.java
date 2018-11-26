package com.xbctechnologies.core.apis.asset.thread;

import com.xbctechnologies.core.apis.XCube;
import com.xbctechnologies.core.apis.dto.res.BoolResponse;
import com.xbctechnologies.core.apis.dto.res.account.AccountBalanceResponse;
import com.xbctechnologies.core.apis.dto.res.account.AccountBondInfoResponse;
import com.xbctechnologies.core.apis.dto.res.account.AccountResponse;
import com.xbctechnologies.core.apis.dto.res.validator.ValidatorBondResponse;
import com.xbctechnologies.core.utils.CurrencyUtil;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.assertEquals;

public class VerifyAccountThread implements Runnable {
    private int totalItemCnt;
    private AtomicInteger itemCnt1;
    private AtomicInteger itemCnt2;
    private AtomicInteger itemCnt3;
    private int totalCnt;
    private AtomicInteger completeCnt;
    private List<XCube> xCubeList;
    private Object lockedObj;
    private String targetChainId;
    private List<String> accounts;


    public VerifyAccountThread(int totalItemCnt, AtomicInteger itemCnt1, AtomicInteger itemCnt2, AtomicInteger itemCnt3, int totalCnt, AtomicInteger completeCnt, List<String> accounts, String targetChainId, List<XCube> xCubeList, Object lockedObj) {
        this.totalItemCnt = totalItemCnt;
        this.itemCnt1 = itemCnt1;
        this.itemCnt2 = itemCnt2;
        this.itemCnt3 = itemCnt3;
        this.totalCnt = totalCnt;
        this.completeCnt = completeCnt;
        this.accounts = accounts;
        this.targetChainId = targetChainId;
        this.xCubeList = xCubeList;
        this.lockedObj = lockedObj;
    }

    @Override
    public void run() {
        //Balance Data
        for (String account : accounts) {
            AccountBalanceResponse baseBalanceData = null;
            System.out.println(String.format("Balance - %s/%s", itemCnt1.addAndGet(1), totalItemCnt));
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
        for (String account : accounts) {
            System.out.println(String.format("Bonding - %s/%s", itemCnt2.addAndGet(1), totalItemCnt));
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
        for (String account : accounts) {
            System.out.println(String.format("Account - %s/%s", itemCnt3.addAndGet(1), totalItemCnt));
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

        //Is validator
        int tempCnt = 0;
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

        int completedCnt = completeCnt.addAndGet(1);
        if (completedCnt == totalCnt) {
            System.out.println(String.format("Accounts - %s/%s", completedCnt, totalCnt));
            synchronized (lockedObj) {
                lockedObj.notify();
            }
        }
        System.out.println(String.format("Accounts - %s/%s", completedCnt, totalCnt));
    }
}
