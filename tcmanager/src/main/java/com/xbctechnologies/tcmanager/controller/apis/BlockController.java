package com.xbctechnologies.tcmanager.controller.apis;

import com.xbctechnologies.core.apis.dto.res.block.BlockResponse;
import com.xbctechnologies.core.apis.dto.res.block.BlockTxCntResponse;
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

@Api(tags = {"C. Block"}, description = "Block API")
@RestController
public class BlockController {
    @Autowired
    private XCubeClient xcubeClient;

    @ApiOperation(value = "Get block by hash")
    @RequestMapping(value = "/getBlock", method = {RequestMethod.POST}, produces = "application/json")
    public BlockResponse getBlock(@RequestParam(defaultValue = ParamUtil.TARGET_CHAIN_ID) String targetChainId, @RequestParam String blockHash) throws TransactionException {
        return xcubeClient.xCube.getBlock(null, targetChainId, blockHash).send();
    }

    @ApiOperation(value = "Get block by number")
    @RequestMapping(value = "/getBlockByNumber", method = {RequestMethod.POST}, produces = "application/json")
    public BlockResponse getBlockByNumber(@RequestParam(defaultValue = ParamUtil.TARGET_CHAIN_ID) String targetChainId, @RequestParam long blockNo) throws TransactionException {
        return xcubeClient.xCube.getBlockByNumber(null, targetChainId, blockNo).send();
    }

    @ApiOperation(value = "Get transaction count in block")
    @RequestMapping(value = "/getBlockTxCount", method = {RequestMethod.POST}, produces = "application/json")
    public BlockTxCntResponse getBlockTxCount(@RequestParam(defaultValue = ParamUtil.TARGET_CHAIN_ID) String targetChainId, @RequestParam Object blockNoOrHash) throws TransactionException {
        return xcubeClient.xCube.getBlockTxCount(null, targetChainId, blockNoOrHash).send();
    }

    @ApiOperation(value = "Get latest block")
    @RequestMapping(value = "/getBlockLatestBlock", method = {RequestMethod.POST}, produces = "application/json")
    public BlockResponse getBlockLatestBlock(@RequestParam(defaultValue = ParamUtil.TARGET_CHAIN_ID) String targetChainId) throws TransactionException {
        return xcubeClient.xCube.getBlockLatestBlock(null, targetChainId).send();
    }
}
