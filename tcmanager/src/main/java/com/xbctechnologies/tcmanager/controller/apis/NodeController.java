package com.xbctechnologies.tcmanager.controller.apis;

import com.xbctechnologies.core.apis.dto.res.BoolResponse;
import com.xbctechnologies.core.apis.dto.res.Response;
import com.xbctechnologies.core.apis.dto.res.node.XChainInfoResponse;
import com.xbctechnologies.core.exception.TransactionException;
import com.xbctechnologies.core.utils.ParamUtil;
import com.xbctechnologies.tcmanager.config.XCubeClient;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Api(tags = {"G. Node"}, description = "Node API")
@RestController
public class NodeController {
    @Autowired
    private XCubeClient xcubeClient;

    @ApiOperation(value = "Sync")
    @RequestMapping(value = "/sync", method = {RequestMethod.POST}, produces = "application/json")
    public BoolResponse sync(@RequestParam(defaultValue = ParamUtil.TARGET_CHAIN_ID) String targetChainId) throws TransactionException {
        return xcubeClient.xCube.sync(null, targetChainId).send();
    }

    @ApiOperation(value = "Is sync")
    @RequestMapping(value = "/isSync", method = {RequestMethod.POST}, produces = "application/json")
    public BoolResponse isSync(@RequestParam(defaultValue = ParamUtil.TARGET_CHAIN_ID) String targetChainId) throws TransactionException {
        return xcubeClient.xCube.isSync(null, targetChainId).send();
    }

    @ApiOperation(value = "Get xchain info")
    @RequestMapping(value = "/getXChainInfo", method = {RequestMethod.POST}, produces = "application/json")
    public XChainInfoResponse getXChainInfo() throws TransactionException {
        return xcubeClient.xCube.getXChainInfo(null).send();
    }

    @ApiOperation(value = "Get node version")
    @RequestMapping(value = "/getVersion", method = {RequestMethod.POST}, produces = "application/json")
    public Response getVersion() throws TransactionException {
        return xcubeClient.xCube.getVersion(null).send();
    }
}
