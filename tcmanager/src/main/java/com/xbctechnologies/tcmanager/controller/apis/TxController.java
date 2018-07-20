package com.xbctechnologies.tcmanager.controller.apis;

import com.xbctechnologies.core.apis.dto.ApiEnum;
import com.xbctechnologies.core.apis.dto.req.tx.TxRequest;
import com.xbctechnologies.core.apis.dto.res.tx.*;
import com.xbctechnologies.core.apis.dto.xtypes.*;
import com.xbctechnologies.core.exception.TransactionException;
import com.xbctechnologies.core.utils.ParamUtil;
import com.xbctechnologies.tcmanager.config.XCubeClient;
import com.xbctechnologies.tcmanager.controller.util.FileUtil;
import com.xbctechnologies.xcrypto.util.ByteUtil;
import io.swagger.annotations.*;
import org.spongycastle.util.encoders.DecoderException;
import org.spongycastle.util.encoders.Hex;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.List;

@Api(tags = {"B. Transaction"}, description = "Transaction API")
@RestController
public class TxController {
    @Autowired
    private XCubeClient xcubeClient;

    @ApiOperation(value = "Send common transaction")
    @RequestMapping(value = "/sendCommonTx", method = {RequestMethod.POST}, produces = "application/json")
    public TxSendResponse sendCommonTx(TxRequest txRequest, @RequestBody(required = false) TxCommonBody payloadBody) throws TransactionException {
        txRequest.setPayloadBody(payloadBody);
        return xcubeClient.xCube.sendTransaction(txRequest).send();
    }

    @ApiOperation(value = "Send file transaction")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "payloadType", defaultValue = "FileType", paramType = "query", required = true),
            @ApiImplicitParam(name = "op", defaultValue = "RegisterType", allowableValues = "RegisterType,UpdateType", paramType = "query", required = true),
            @ApiImplicitParam(name = "filePath", value = "File location", dataType = "string", paramType = "query"),
    })
    @RequestMapping(value = "/sendFileTx", method = {RequestMethod.POST}, produces = "application/json")
    public TxSendResponse sendFileTx(TxRequest txRequest, String op,
                                     //String filePath,
                                     @ApiParam(value = "file select", required = true) @RequestPart(value = "file") MultipartFile file,
                                     @RequestParam(required = false) String reserved,
                                     @RequestParam(required = false) String info,
                                     @RequestParam(required = false) List<String> authors)
            throws TransactionException {
        // 1. TxRequest payloadType check. (FileType)
        if (txRequest.getPayloadType() != ApiEnum.PayloadType.FileType) {
            throw new TransactionException("TxRequest is not FileType. PayloadType check.");
        }

        File testfile = null;
        try {
            testfile = FileUtil.convert(file);

            TxFileBody txFileBody = new TxFileBody();
            txFileBody.setOp(ApiEnum.OpType.valueOf(op));
            if (testfile != null) {
                txFileBody.setFile(testfile);
            }
            txFileBody.setReserved(reserved);
            txFileBody.setInfo(info);
            txFileBody.setAuthors(authors);

            txRequest.setPayloadBody(txFileBody);
            return xcubeClient.xCube.sendTransaction(txRequest).send();
        } catch (IOException e) {
            throw new TransactionException(e.toString());
        } finally {
            if (testfile != null) {
                testfile.delete();
            }
        }
    }

    @ApiOperation(value = "Send bonding transaction")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "payloadType", defaultValue = "BondingType", paramType = "query", required = true),
    })
    @RequestMapping(value = "/sendBondingTx", method = {RequestMethod.POST}, produces = "application/json")
    public TxSendResponse sendBondingTx(TxRequest txRequest, @RequestBody TxBondingBody payloadBody) throws TransactionException {
        txRequest.setPayloadBody(payloadBody);
        return xcubeClient.xCube.bonding(txRequest).send();
    }

    @ApiOperation(value = "Send unbonding transaction")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "payloadType", defaultValue = "UnbondingType", paramType = "query", required = true),
    })
    @RequestMapping(value = "/sendUnbondingTx", method = {RequestMethod.POST}, produces = "application/json")
    public TxSendResponse sendUnbondingTx(TxRequest txRequest, @RequestBody TxUnbondingBody payloadBody) throws TransactionException {
        txRequest.setPayloadBody(payloadBody);
        return xcubeClient.xCube.unbonding(txRequest).send();
    }

    @ApiOperation(value = "Send delegatingType transaction")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "payloadType", defaultValue = "DelegatingType", paramType = "query", required = true),
    })
    @RequestMapping(value = "/sendDelegatingTx", method = {RequestMethod.POST}, produces = "application/json")
    public TxSendResponse sendDelegatingTx(TxRequest txRequest, @RequestBody TxDelegatingBody payloadBody) throws TransactionException {
        txRequest.setPayloadBody(payloadBody);
        return xcubeClient.xCube.delegating(txRequest).send();
    }

    @ApiOperation(value = "Send undelegating transaction")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "payloadType", defaultValue = "UndelegatingType", paramType = "query", required = true),
    })
    @RequestMapping(value = "/sendUndelegatingTx", method = {RequestMethod.POST}, produces = "application/json")
    public TxSendResponse sendUndelegatingTx(TxRequest txRequest, @RequestBody TxUndelegatingBody payloadBody) throws TransactionException {
        txRequest.setPayloadBody(payloadBody);
        return xcubeClient.xCube.undelegating(txRequest).send();
    }

    @ApiOperation(value = "Send governance proposal transaction")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "payloadType", defaultValue = "GRProposalType", paramType = "query", required = true),
    })
    @RequestMapping(value = "/sendGRProposalTx", method = {RequestMethod.POST}, produces = "application/json")
    public TxSendResponse sendGRProposalTx(TxRequest txRequest, @RequestBody TxGRProposalBody payloadBody) throws TransactionException {
        txRequest.setPayloadBody(payloadBody);
        return xcubeClient.xCube.sendTransaction(txRequest).send();
    }

    @ApiOperation(value = "Send governance vote transaction")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "payloadType", defaultValue = "GRVoteType", paramType = "query", required = true),
    })
    @RequestMapping(value = "/sendGRVoteTx", method = {RequestMethod.POST}, produces = "application/json")
    public TxSendResponse sendGRVoteTx(TxRequest txRequest, @RequestBody TxGRVoteBody payloadBody) throws TransactionException {
        txRequest.setPayloadBody(payloadBody);
        return xcubeClient.xCube.sendTransaction(txRequest).send();
    }

    @ApiOperation(value = "Send recover transaction")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "payloadType", defaultValue = "RecoverValidatorType", paramType = "query", required = true),
    })
    @RequestMapping(value = "/sendRecoverValidatorTx", method = {RequestMethod.POST}, produces = "application/json")
    public TxSendResponse sendRecoverValidatorTx(TxRequest txRequest) throws TransactionException {
        txRequest.setPayloadBody(new TxRecoverBody());
        return xcubeClient.xCube.sendTransaction(txRequest).send();
    }

    @ApiOperation(value = "Send xchain transaction")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "payloadType", defaultValue = "MakeXChainType", paramType = "query", required = true)
    })
    @RequestMapping(value = "/sendXChainTx", method = {RequestMethod.POST}, produces = "application/json")
    public TxSendResponse sendXChainTx(
            TxRequest txRequest,
            @RequestBody TxMakeXChainBody payloadBody) throws TransactionException {
        txRequest.setPayloadBody(payloadBody);
        return xcubeClient.xCube.sendTransaction(txRequest).send();
    }

    @ApiOperation(value = "Sign transaction")
    @RequestMapping(value = "/signTx", method = {RequestMethod.POST}, produces = "application/json")
    public TxSignResponse signTx(@RequestBody TxRequest txRequest) throws TransactionException {
        return xcubeClient.xCube.signTransaction(null, txRequest).send();
    }

    @ApiOperation(value = "Get transaction")
    @RequestMapping(value = "/checkOriginal", method = {RequestMethod.POST}, produces = "application/json")
    public TxCheckOriginalResponse checkOriginal(@RequestParam(defaultValue = ParamUtil.TARGET_CHAIN_ID) String targetChainId,
                                                 //@RequestParam String filePath)
                                                 @RequestParam(value = "dataAccountAddr", required = true) String dataAccountAddr,
                                                 @ApiParam(value = "file select", required = true) @RequestPart(value = "file") MultipartFile file)
            throws TransactionException, IOException, IllegalArgumentException {
        // 1. dataAccountAddr hex string check.
        try {
            Hex.decode(ByteUtil.toNoPriFixHexString(dataAccountAddr));
        } catch (DecoderException e) {
            throw new IllegalArgumentException("This is not a dataAccountAddr hex message format. " + e.toString());
        }

        File testfile = null;
        try {
            testfile = FileUtil.convert(file);
            return xcubeClient.xCube.checkOriginal(null, targetChainId, dataAccountAddr, testfile).send();
        } finally {
            if (testfile != null) {
                testfile.delete();
            }
        }
    }

    @ApiOperation(value = "Get transaction")
    @RequestMapping(value = "/getTransaction", method = {RequestMethod.POST}, produces = "application/json")
    public TxResponse getTransaction(@RequestParam(defaultValue = ParamUtil.TARGET_CHAIN_ID) String targetChainId, @RequestParam String txHash) throws TransactionException {
        return xcubeClient.xCube.getTransaction(null, targetChainId, txHash).send();
    }

    @ApiOperation(value = "Get transaction receipt")
    @RequestMapping(value = "/getTransactionReceipt", method = {RequestMethod.POST}, produces = "application/json")
    public TxReceiptResponse getTransactionReceipt(@RequestParam(defaultValue = ParamUtil.TARGET_CHAIN_ID) String targetChainId, @RequestParam String txHash) throws TransactionException {
        return xcubeClient.xCube.getTransactionReceipt(null, targetChainId, txHash).send();
    }
}
