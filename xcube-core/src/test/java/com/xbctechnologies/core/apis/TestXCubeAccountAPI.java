package com.xbctechnologies.core.apis;

import com.xbctechnologies.core.apis.dto.res.BoolResponse;
import com.xbctechnologies.core.apis.dto.res.account.AccountAddrListResponse;
import com.xbctechnologies.core.apis.dto.res.account.AccountExportResponse;
import com.xbctechnologies.core.apis.dto.res.account.AccountNewResponse;
import com.xbctechnologies.core.component.rest.RestHttpClient;
import com.xbctechnologies.core.component.rest.RestHttpConfig;
import com.xbctechnologies.core.order.Order;
import com.xbctechnologies.xcrypto.api.XbSignUtil;
import com.xbctechnologies.xcrypto.ec.ECKey;
import com.xbctechnologies.xcrypto.util.ByteUtil;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.spongycastle.util.encoders.Hex;

import java.util.List;

import static org.junit.Assert.*;

public class TestXCubeAccountAPI {
    private XCube xCube;
    private TestXCube testXCube;
    private String targetChainId = "1T";
    private String address = "0xeba3f7d6983141ecbca0319aed800458d9a3f2d5";
    private String persistentAddress = "0x9ac601f1a9c8385cb1fd794d030898168b0b617a";

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

    @Test
    @Order(order = 1)
    public void TestNewAccount() {
        /*
         Account Create check
        */

        // 1 case Account Create success
        String password = "1111";
        AccountNewResponse accountNewResponse = xCube.newAccount(null, password).send();
        assertNull(accountNewResponse.getError());
        assertNotNull(accountNewResponse.getAddress());
        System.out.println(accountNewResponse.getAddress());
        // 2 case Account Create fail ( password is nul check )
        password = null;
        accountNewResponse = xCube.newAccount(null, password).send();
        if (accountNewResponse.getError() != null) {
            Assert.assertEquals(accountNewResponse.getError().getCode(), 707);
        }
        assertNull(accountNewResponse.getAddress());
        // 3 case Account Create fail ( password is empty check )
        password = "";
        accountNewResponse = xCube.newAccount(null, password).send();
        if (accountNewResponse.getError() != null) {
            Assert.assertEquals(accountNewResponse.getError().getCode(), 707);
        }
        assertNull(accountNewResponse.getAddress());
    }

    @Test
    @Order(order = 2)
    public void TestImportKey() {
        // Account Key import check

        // 1 case Account Key import success
        XbSignUtil xbSignUtil = new XbSignUtil();
        ECKey key = xbSignUtil.ecKeyGen();
        // 1. ECC PriKey Hex Format
        String priKeyHexstr = key.getPrivKeyHexString();
        System.out.println("priKeyHexstr: " + priKeyHexstr);
        String passphrase = "1111";
        String address = Hex.toHexString(key.getAddress());
        System.out.println("address: " + address);
        BoolResponse boolResponse = xCube.importAccount(null, priKeyHexstr, address, passphrase).send();
        assertNull(boolResponse.getError());
        assertEquals(true, boolResponse.getResult());

        // 2 case exist Account Key import fail
        passphrase = "1111";
        address = Hex.toHexString(key.getAddress());
        boolResponse = xCube.importAccount(null, priKeyHexstr, address, passphrase).send();
        if (boolResponse.getError() != null) {
            assertEquals(boolResponse.getError().getCode(), 708);
        }

        // 3 case Account Key import fail ( password is null check )
        key = xbSignUtil.ecKeyGen();
        passphrase = null;
        address = Hex.toHexString(key.getAddress());
        boolResponse = xCube.importAccount(null, priKeyHexstr, address, passphrase).send();
        if (boolResponse.getError() != null) {
            assertEquals(boolResponse.getError().getCode(), 707);
        }

        // 4 case Account Key import fail ( password is empty check )
        key = xbSignUtil.ecKeyGen();
        passphrase = "";
        address = Hex.toHexString(key.getAddress());
        boolResponse = xCube.importAccount(null, priKeyHexstr, address, passphrase).send();
        if (boolResponse.getError() != null) {
            assertEquals(boolResponse.getError().getCode(), 707);
        }
    }

    @Test
    @Order(order = 3)
    public void TestUnlockKey() throws InterruptedException {
        // Account Key UnLock check
        String password = "1111";
        long secondsDuration = 3;
        BoolResponse boolResponse = xCube.unlockAccount(null, targetChainId, address, password, secondsDuration).send();
        assertNull(boolResponse.getError());
        assertEquals(true, boolResponse.getResult());

        boolResponse = xCube.unlockAccount(null, targetChainId, address, password, secondsDuration).send();
        if (boolResponse.getError() != null) {
            assertEquals(boolResponse.getError().getCode(), 709);
        }

        Thread.sleep(4 * 1000);

        boolResponse = xCube.unlockAccount(null, targetChainId, address, password, secondsDuration).send();
        assertNull(boolResponse.getError());
        assertEquals(true, boolResponse.getResult());
    }

    @Test
    @Order(order = 4)
    public void TestLockKey() throws InterruptedException {
        Thread.sleep(5 * 1000);
        // Account Key Lock check
        String password = "1111";
        long secondsDuration = 3;
        BoolResponse boolResponse = xCube.unlockAccount(null, targetChainId, address, password, secondsDuration).send();
        assertNull(boolResponse.getError());
        assertEquals(true, boolResponse.getResult());

        Thread.sleep(1 * 1000);

        boolResponse = xCube.lockAccount(null, targetChainId, address).send();
        assertNull(boolResponse.getError());
        assertEquals(true, boolResponse.getResult());
    }

    @Test
    @Order(order = 5)
    public void TestUnlockAndTimeoutCheck() throws InterruptedException {
        Thread.sleep(4 * 1000);

        // Account Key Unlock and timoeout Key Lock check
        String password = "1111";
        long secondsDuration = 3;
        BoolResponse boolResponse = xCube.unlockAccount(null, targetChainId, address, password, secondsDuration).send();
        assertNull(boolResponse.getError());
        assertEquals(true, boolResponse.getResult());

        Thread.sleep(4 * 1000);

        boolResponse = xCube.lockAccount(null, targetChainId, address).send();
        if (boolResponse.getError() != null) {
            assertEquals(boolResponse.getError().getCode(), 706);
        }
    }

    @Test
    @Order(order = 6)
    public void TestPersistentAccountLockAndTimeoutUnlock() throws InterruptedException {
        String localaddress = persistentAddress;
        BoolResponse boolResponse = xCube.lockAccount(null, targetChainId, localaddress).send();
        assertNull(boolResponse.getError());
        assertEquals(true, boolResponse.getResult());

        String password = "1111";
        long secondsDuration = 3;
        boolResponse = xCube.unlockAccount(null, targetChainId, localaddress, password, secondsDuration).send();
        assertNull(boolResponse.getError());
        assertEquals(true, boolResponse.getResult());

        Thread.sleep(4 * 1000);

        boolResponse = xCube.lockAccount(null, targetChainId, localaddress).send();
        if (boolResponse.getError() != null) {
            assertEquals(boolResponse.getError().getCode(), 706);
        }
    }

    @Test
    @Order(order = 7)
    public void TestGetListAccount() {
        // Account List Get check
        AccountAddrListResponse accountAddrListResponse = xCube.getListAccount(null).send();
        assertNull(accountAddrListResponse.getError());
        assertNotNull(accountAddrListResponse.getAccountList());
        assertNotEquals(0, accountAddrListResponse.getAccountList().size());
        List<String> accountlist = accountAddrListResponse.getAccountList();
        System.out.println("account size: " + accountAddrListResponse.getAccountList().size());
        /*for(int i=0; i < accountAddrListResponse.getAccountList().size(); i++) {
            String account = accountAddrListResponse.getAccountList().get(i);
            System.out.println(account);
        }*/
    }

    @Test
    public void TestExportAccount() {
        //ca79b815924a6b093c3093ee03993bbcd1796eb7
        String localaddress = "0xca79b815924a6b093c3093ee03993bbcd1796eb7";
        String passphrase = "1111";
        AccountExportResponse exportResponse = testXCube.exportAccount(null, localaddress, passphrase).send();
        assertNull(exportResponse.getError());
        assertNotNull(exportResponse.getResult());
        // sender Account Address, export Key Account Address same check.
        Assert.assertEquals(ByteUtil.toNoPriFixHexString(localaddress.toLowerCase()), exportResponse.getResult().getAddress().toLowerCase());
    }
}
