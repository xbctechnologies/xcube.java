package com.xbctechnologies.tcmanager.controller.apis;

import com.xbctechnologies.core.apis.dto.res.data.*;
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

@Api(tags = {"E. Data"}, description = "Data API")
@RestController
public class DataController {
    @Autowired
    private XCubeClient xcubeClient;

    @ApiOperation(value = "Get data by data address")
    @RequestMapping(value = "/getDataAccount", method = {RequestMethod.POST}, produces = "application/json")
    public DataAccountResponse getDataAccount(@RequestParam(defaultValue = ParamUtil.TARGET_CHAIN_ID) String targetChainId, @RequestParam String dataAccountAddr) throws TransactionException {
        return xcubeClient.xCube.getDataAccount(null, targetChainId, dataAccountAddr).send();
    }

    @ApiOperation(value = "Get total atx")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "currencyType", defaultValue = "CoinType", allowableValues = "XTOType,KXTOType,MXTOType,GXTOType,MICROCoinType,MILLICoinType,CoinType", paramType = "query", required = true),
    })
    @RequestMapping(value = "/getTotalATX", method = {RequestMethod.POST}, produces = "application/json")
    public TotalBalanceResponse getTotalATX(@RequestParam(defaultValue = ParamUtil.TARGET_CHAIN_ID) String targetChainId, String currencyType) throws TransactionException {
        return xcubeClient.xCube.getTotalATX(null, targetChainId, CurrencyUtil.CurrencyType.valueOf(currencyType)).send();
    }

    @ApiOperation(value = "Get progress governance")
    @RequestMapping(value = "/getProgressGovernance", method = {RequestMethod.POST}, produces = "application/json")
    public ProgressGovernance getProgressGovernance(@RequestParam(defaultValue = ParamUtil.TARGET_CHAIN_ID) String targetChainId) throws TransactionException {
        return xcubeClient.xCube.getProgressGovernance(null, targetChainId).send();
    }

    @ApiOperation(value = "Get current governance")
    @RequestMapping(value = "/getCurrentGovernance", method = {RequestMethod.POST}, produces = "application/json")
    public CurrentGovernance getCurrentGovernance(@RequestParam(defaultValue = ParamUtil.TARGET_CHAIN_ID) String targetChainId) throws TransactionException {
        return xcubeClient.xCube.getCurrentGovernance(null, targetChainId).send();
    }
}
