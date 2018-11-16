package com.xbctechnologies.tcmanager.controller.apis;

import com.xbctechnologies.core.apis.dto.res.validator.*;
import com.xbctechnologies.tcmanager.config.XCubeClient;
import com.xbctechnologies.core.apis.dto.res.BoolResponse;
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

    @ApiOperation(value = "Get validator list")
    @RequestMapping(value = "/getValidatorList", method = {RequestMethod.POST}, produces = "application/json")
    public ValidatorListResponse getValidatorList(@RequestParam(defaultValue = ParamUtil.TARGET_CHAIN_ID) String targetChainId) throws TransactionException {
        return xcubeClient.xCube.getValidatorList(null, targetChainId).send();
    }

    @ApiOperation(value = "Get validator")
    @RequestMapping(value = "/getValidator", method = {RequestMethod.POST}, produces = "application/json")
    public ValidatorResponse getValidator(@RequestParam(defaultValue = ParamUtil.TARGET_CHAIN_ID) String targetChainId, @RequestParam String validatorAddr) throws TransactionException {
        return xcubeClient.xCube.getValidator(null, targetChainId, validatorAddr).send();
    }

    @ApiOperation(value = "Get simple validator by validator address")
    @RequestMapping(value = "/getSimpleValidator", method = {RequestMethod.POST}, produces = "application/json")
    public SimpleValidatorResponse getSimpleValidator(@RequestParam(defaultValue = ParamUtil.TARGET_CHAIN_ID) String targetChainId, @RequestParam String validatorAddr) throws TransactionException {
        return xcubeClient.xCube.getSimpleValidator(null, targetChainId, validatorAddr).send();
    }

    @ApiOperation(value = "Get simple validators")
    @RequestMapping(value = "/getSimpleValidators", method = {RequestMethod.POST}, produces = "application/json")
    public SimpleValidatorsResponse getSimpleValidators(@RequestParam(defaultValue = ParamUtil.TARGET_CHAIN_ID) String targetChainId) throws TransactionException {
        return xcubeClient.xCube.getSimpleValidators(null, targetChainId).send();
    }
}
