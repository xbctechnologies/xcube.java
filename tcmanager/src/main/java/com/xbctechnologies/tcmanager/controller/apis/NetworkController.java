package com.xbctechnologies.tcmanager.controller.apis;

import com.xbctechnologies.core.apis.dto.res.LongResponse;
import com.xbctechnologies.core.apis.dto.res.network.NetworkPeersResponse;
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

@Api(tags = {"F. Network"}, description = "Network API")
@RestController
public class NetworkController {
    @Autowired
    private XCubeClient xcubeClient;

    @ApiOperation(value = "Get peer count")
    @RequestMapping(value = "/getPeerCnt", method = {RequestMethod.POST}, produces = "application/json")
    public LongResponse getPeerCnt(@RequestParam(defaultValue = ParamUtil.TARGET_CHAIN_ID) String targetChainId) throws TransactionException {
        return xcubeClient.xCube.getPeerCnt(null, targetChainId).send();
    }

    @ApiOperation(value = "Get peers")
    @RequestMapping(value = "/getPeers", method = {RequestMethod.POST}, produces = "application/json")
    public NetworkPeersResponse getPeers(@RequestParam(defaultValue = ParamUtil.TARGET_CHAIN_ID) String targetChainId) throws TransactionException {
        return xcubeClient.xCube.getPeers(null, targetChainId).send();
    }
}
