package com.xbctechnologies.tcmanager.controller.apis;

import com.xbctechnologies.core.apis.dto.res.BoolResponse;
import com.xbctechnologies.core.apis.dto.res.account.AccountAddrListResponse;
import com.xbctechnologies.core.apis.dto.res.account.AccountBalanceResponse;
import com.xbctechnologies.core.apis.dto.res.account.AccountBondInfoResponse;
import com.xbctechnologies.core.apis.dto.res.account.AccountNewResponse;
import com.xbctechnologies.core.exception.TransactionException;
import com.xbctechnologies.core.utils.CurrencyUtil;
import com.xbctechnologies.core.utils.ParamUtil;
import com.xbctechnologies.tcmanager.config.XCubeClient;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.nio.file.Files;
import java.nio.file.Paths;

@Api(tags = {"D. Account"}, description = "Account API")
@RestController
public class AccountController {
    @Autowired
    private XCubeClient xcubeClient;

    @ApiOperation(value = "Create account")
    @RequestMapping(value = "/newAccount", method = {RequestMethod.POST}, produces = "application/json")
    public AccountNewResponse newAccount(@RequestParam String passphrase) throws TransactionException {
        return xcubeClient.xCube.newAccount(null, passphrase).send();
    }

    @ApiOperation(value = "Import account")
    @RequestMapping(value = "/importAccount", method = {RequestMethod.POST}, produces = "application/json")
    public BoolResponse importAccount(@RequestParam String accountFilePath, String accountAddr, String passphrase) throws Exception {
        byte[] encoded = Files.readAllBytes(Paths.get(accountFilePath));
        String priKeyHexstr = new String(encoded, "UTF-8");
        return xcubeClient.xCube.importAccount(null, priKeyHexstr, accountAddr, passphrase).send();
    }

    /*@ApiOperation(value = "Export account")
    @RequestMapping(value = "/exportAccount", method = {RequestMethod.POST}, produces = "application/json")
    public AccountExportResponse exportAccount(@RequestParam(defaultValue = "3630E60CF39F36AD902479923F2612B1FA6CEA3390A410B406945D72E892EEA8") String accountAddr, @RequestParam String passphrase) throws TransactionException {
        return xcubeClient.xCube.exportAccount(null, accountAddr, passphrase).send();
    }*/

    @ApiOperation(value = "Lock account")
    @RequestMapping(value = "/lockAccount", method = {RequestMethod.POST}, produces = "application/json")
    public BoolResponse lockAccount(@RequestParam(defaultValue = ParamUtil.TARGET_CHAIN_ID) String targetChainId, @RequestParam(defaultValue = ParamUtil.SENDER) String accountAddr) throws TransactionException {
        return xcubeClient.xCube.lockAccount(null, targetChainId, accountAddr).send();
    }

    @ApiOperation(value = "Unlock account")
    @RequestMapping(value = "/unlockAccount", method = {RequestMethod.POST}, produces = "application/json")
    public BoolResponse unlockAccount(
            @RequestParam(defaultValue = ParamUtil.TARGET_CHAIN_ID) String targetChainId,
            @RequestParam(defaultValue = ParamUtil.SENDER) String accountAddr,
            @RequestParam String passphrase,
            @RequestParam long secondsDuration) throws TransactionException {
        return xcubeClient.xCube.unlockAccount(null, targetChainId, accountAddr, passphrase, secondsDuration).send();
    }

    @ApiOperation(value = "Get accounts")
    @RequestMapping(value = "/getListAccount", method = {RequestMethod.POST}, produces = "application/json")
    public AccountAddrListResponse getListAccount() throws TransactionException {
        return xcubeClient.xCube.getListAccount(null).send();
    }

    @ApiOperation(value = "Get balance of account")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "currencyType", defaultValue = "CoinType", allowableValues = "XTOType,KXTOType,MXTOType,GXTOType,MICROCoinType,MILLICoinType,CoinType", paramType = "query", required = true),
    })
    @RequestMapping(value = "/getBalance", method = {RequestMethod.POST}, produces = "application/json")
    public AccountBalanceResponse getListAccount(
            @RequestParam(defaultValue = ParamUtil.TARGET_CHAIN_ID) String targetChainId,
            @RequestParam(defaultValue = ParamUtil.SENDER) String accountAddr,
            String currencyType

    ) throws TransactionException {
        return xcubeClient.xCube.getBalance(null, targetChainId, accountAddr, CurrencyUtil.CurrencyType.valueOf(currencyType)).send();
    }

    @ApiOperation(value = "Get bonding amount")
    @RequestMapping(value = "/getBondingAmount", method = {RequestMethod.POST}, produces = "application/json")
    public AccountBondInfoResponse getBondingAmount(
            @RequestParam(defaultValue = ParamUtil.TARGET_CHAIN_ID) String targetChainId,
            @RequestParam(defaultValue = "0xd52ff6084b6dec53b74b2ac9133fe3541709fa7f") String accountAddr,
            @RequestParam(defaultValue = "XTOType") CurrencyUtil.CurrencyType currencyType
    ) throws TransactionException {
        return xcubeClient.xCube.getBonding(null, targetChainId, accountAddr, currencyType).send();
    }
}
