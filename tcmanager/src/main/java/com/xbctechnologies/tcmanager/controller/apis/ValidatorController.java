package com.xbctechnologies.tcmanager.controller.apis;

import com.xbctechnologies.tcmanager.config.XCubeClient;
import com.xbctechnologies.core.apis.dto.res.BoolResponse;
import com.xbctechnologies.core.apis.dto.res.validator.ValidatorBondResponse;
import com.xbctechnologies.core.apis.dto.res.validator.ValidatorSetResponse;
import com.xbctechnologies.core.exception.TransactionException;
import com.xbctechnologies.core.utils.ParamUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Api(tags = {"H. Validator"}, description = "Validator API")
@RestController
public class ValidatorController {
    @Autowired
    private XCubeClient xcubeClient;

    @ApiOperation(value = "Is validator")
    @RequestMapping(value = "/isValidator", method = {RequestMethod.POST}, produces = "application/json")
    public BoolResponse isValidator(@RequestParam(defaultValue = ParamUtil.TARGET_CHAIN_ID) String targetChainId, @RequestParam(defaultValue = "0xd52ff6084b6dec53b74b2ac9133fe3541709fa7f") String accountAddr) throws TransactionException {
        return xcubeClient.xCube.isValidator(null, targetChainId, accountAddr).send();
    }

    @ApiOperation(value = "Get validator of")
    @RequestMapping(value = "/getValidatorsOf", method = {RequestMethod.POST}, produces = "application/json")
    public ValidatorBondResponse getValidatorsOf(@RequestParam(defaultValue = ParamUtil.TARGET_CHAIN_ID) String targetChainId, @RequestParam(defaultValue = "0xd52ff6084b6dec53b74b2ac9133fe3541709fa7f") String accountAddr) throws TransactionException {
        return xcubeClient.xCube.getValidatorsOf(null, targetChainId, accountAddr).send();
    }

    @ApiOperation(value = "Get validator set")
    @RequestMapping(value = "/getValidatorSet", method = {RequestMethod.POST}, produces = "application/json")
    public ValidatorSetResponse getValidatorSet(@RequestParam(defaultValue = ParamUtil.TARGET_CHAIN_ID) String targetChainId, @RequestParam long blockNo) throws TransactionException {
        return xcubeClient.xCube.getValidatorSet(null, targetChainId, blockNo).send();
    }
}
