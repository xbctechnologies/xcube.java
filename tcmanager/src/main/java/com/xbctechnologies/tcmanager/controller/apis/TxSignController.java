package com.xbctechnologies.tcmanager.controller.apis;

import com.xbctechnologies.core.apis.dto.xtypes.*;
import com.xbctechnologies.tcmanager.controller.util.IOUtil;
import com.xbctechnologies.core.apis.dto.ApiEnum;
import com.xbctechnologies.core.apis.dto.req.tx.TxRequest;
import com.xbctechnologies.core.apis.dto.res.tx.TxResponse;
import com.xbctechnologies.core.apis.dto.res.tx.TxSendResponse;
import com.xbctechnologies.core.exception.TransactionException;
import com.xbctechnologies.core.utils.DateUtil;
import com.xbctechnologies.core.utils.ParamUtil;
import com.xbctechnologies.core.utils.SignUtil;
import com.xbctechnologies.tcmanager.config.XCubeClient;
import com.xbctechnologies.tcmanager.controller.util.FileUtil;
import com.xbctechnologies.xcrypto.api.XbSignUtil;
import com.xbctechnologies.xcrypto.ec.ECKey;
import com.xbctechnologies.xcrypto.util.ByteUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.spongycastle.util.encoders.Hex;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;
import java.util.ArrayList;
import java.util.List;

@Api(tags = {"I. Tx Client Sign"}, description = "Transaction sign test")
@RestController
public class TxSignController {
    @Autowired
    private XCubeClient xcubeClient;
    private String targetChainId = ParamUtil.TARGET_CHAIN_ID;
    private String sender = ParamUtil.SENDER;
    private String receiver = ParamUtil.RECEIVER;
    private BigInteger fee = new BigInteger(ParamUtil.FEE);

    public void clientSign(TxRequest txRequest, String passwd, MultipartFile keyfile) throws TransactionException {
        String filecontent = IOUtil.getContent(keyfile);
        if (filecontent == null) {
            throw new TransactionException("key file is null.");

        }
        filecontent = IOUtil.getKeyJsonConvertString(filecontent);
        try {
            SignUtil.signTx(txRequest, passwd, filecontent);
        } catch (Exception e) {
            e.printStackTrace();
            String errMsg = e.toString();
            if (e.toString().indexOf("The mac value is incorrect") > 0) {
                errMsg = e.toString() + "(The password is incorrect.)";
            }
            throw new TransactionException("common transaction Client Sign fail " + errMsg);
        }
        // 1. Sender Account Address, file public key create Address same check.
        String pubKeyAddress = "";
        try {
            pubKeyAddress = SignUtil.getAddress(txRequest);
        } catch (Exception e) {
            e.printStackTrace();
            throw new TransactionException("public Key Address create fail " + e.toString());
        }

        if (!txRequest.getSender().equals(pubKeyAddress)) {
            System.out.println("txRequest.getSender(): " + txRequest.getSender());
            System.out.println("pubKeyAddress:         " + pubKeyAddress);
            throw new TransactionException("address Account Address, key file Account Address is mismatch");
        }
    }

    public void checkGetTransaction(TxSendResponse txSendResponse, boolean ignoreFile) throws TransactionException {
        if (txSendResponse.getError() != null) {
            return;
        }

        if (txSendResponse.getResult() == null) {
            throw new TransactionException("TxSendResponse getResult() is null. ( fee, amount, targetChainId check. )");
        }

        // server response sign check result error case
        if (txSendResponse.getError() != null && txSendResponse.getError().getCode() == 323) {
            throw new TransactionException("server response sign verify fail.");
        }

        TxResponse txRes = xcubeClient.xCube.getTransaction(null, targetChainId, ByteUtil.toNoPriFixHexString(txSendResponse.getResult().getTxHash())).send();
        if (txRes.getError() != null) {
            throw new TransactionException(txRes.getError().getMessage());
        }

        // 8. pubkey and message same check
        Byte b = new Byte(txRes.getResult().getV());
        ECKey.ECDSASignature verifysign = ECKey.ECDSASignature.fromComponents(txRes.getResult().getR().toByteArray(), txRes.getResult().getS().toByteArray(), b.byteValue());
        byte[] resMsg = txRes.generateTxRequest(true).marshalProto(ignoreFile).toByteArray();
        XbSignUtil xbSignUtil = new XbSignUtil();
        ECKey recoverkey = null;
        try {
            recoverkey = xbSignUtil.getRecoveryKey(verifysign, resMsg);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            throw new TransactionException(e.toString());
        } catch (SignatureException e) {
            e.printStackTrace();
            throw new TransactionException(e.toString());
        }
        if (!ByteUtil.toNoPriFixHexString(txRes.getResult().getSender()).equals(Hex.toHexString(recoverkey.getAddress()))) {
            throw new TransactionException("sender account Address, recover key Address is mismatch.");
        }

        // 9. ecdsa/sha256 sign verify
        boolean isVerify = false;
        try {
            isVerify = xbSignUtil.verify(verifysign, resMsg);
            if (!isVerify) {
                throw new TransactionException("sign verify fail.");
            }
        } catch (NoSuchAlgorithmException e) {
            throw new TransactionException(e.toString());
        }
        // 10. tx Hash same Check
        if (!txSendResponse.getResult().getTxHash().equals(txRes.getResult().getTxHash())) {
            throw new TransactionException("sendTransaction and getTransaction : tx Hash mismatch.");
        }
    }

    @ApiOperation(value = "Send common transaction Client Sign")
    @RequestMapping(value = "sendCommonTxClientSign", method = RequestMethod.POST, produces = "application/json", consumes = {"multipart/form-data", "application/x-www-form-urlencoded"})
    public TxSendResponse sendCommonTxSign(TxRequest txRequest,
                                           String input,
                                           @RequestParam(value = "passwd", required = true) String passwd,
                                           @ApiParam(value = "Sender Account encrypt key file select", required = true) @RequestPart(value = "keyfile") MultipartFile keyfile
    ) throws TransactionException {
        // 1. TxRequest payloadType check. (CommonType)
        if (txRequest.getPayloadType() != ApiEnum.PayloadType.CommonType) {
            throw new TransactionException("TxRequest is not CommonType. PayloadType check.");
        }
        // 2. TxRequest time setting
        if (txRequest.getTime() == null) {
            BigInteger time = new BigInteger(String.valueOf(DateUtil.getMicroSecond()));
            txRequest.setTime(time);
        }
        // 3. TxRequest payloadBody setting (TxCommonBody)
        TxCommonBody payloadBody = new TxCommonBody(input);
        txRequest.setPayloadBody(payloadBody);

        if (payloadBody == null) {
            throw new TransactionException("TxCommonBody is null");
        }
        // 4. Generate digital signature with selected key file
        clientSign(txRequest, passwd, keyfile);
        // 5. CommonType transaction send
        TxSendResponse txSendResponse = xcubeClient.xCube.sendTransaction(txRequest).send();
        // 6. getTransaction recheck
        checkGetTransaction(txSendResponse, false);
        // 7. return TxSendResponse
        return txSendResponse;
    }

    @ApiOperation(value = "Send file transaction Client Sign")
    @RequestMapping(value = "sendFileTxClientSign", method = RequestMethod.POST, produces = "application/json", consumes = {"multipart/form-data", "application/x-www-form-urlencoded"})
    public TxSendResponse sendFileTxClientSign(TxRequest txRequest,
                                               @RequestParam(value = "op", required = true) String op,
                                               @RequestParam(required = false) String reserved,
                                               @RequestParam(required = false) String info,
                                               @RequestParam(required = false) List<String> authors,
                                               @ApiParam(value = "file select", required = true) @RequestPart(value = "file") MultipartFile file,
                                               @RequestParam(value = "passwd", required = true) String passwd,
                                               @ApiParam(value = "Sender Account encrypt key file select", required = true) @RequestPart(value = "keyfile") MultipartFile keyfile
    ) throws TransactionException {
        // 1. TxRequest payloadType check. (FileType)
        if (txRequest.getPayloadType() != ApiEnum.PayloadType.FileType) {
            throw new TransactionException("TxRequest is not FileType. PayloadType check.");
        }
        // 2. TxRequest time setting
        if (txRequest.getTime() == null) {
            BigInteger time = new BigInteger(String.valueOf(DateUtil.getMicroSecond()));
            txRequest.setTime(time);
        }
        // 3. TxRequest payloadBody setting (TxFileBody)
        TxFileBody txFileBody = new TxFileBody();
        if (op == null) {
            throw new TransactionException("File OpType check. ( ex: 1[ RegisterType ], 2[ UpdateType ]");
        } else {
            if (op.equals("1")) {
                txFileBody.setOp(ApiEnum.OpType.RegisterType);
            } else if (op.equals("2")) {
                txFileBody.setOp(ApiEnum.OpType.UpdateType);
            } else {
                throw new TransactionException("File OpType check. ( ex: 1[ RegisterType ], 2[ UpdateType ]");
            }
        }
        File testfile = null;
        try {
            testfile = FileUtil.convert(file);

            if (testfile != null) {
                txFileBody.setFile(testfile);
            }
            txFileBody.setReserved(reserved);
            txFileBody.setInfo(info);
            txFileBody.setAuthors(authors);
            txRequest.setPayloadBody(txFileBody);
            // 4. Generate digital signature with selected key file
            clientSign(txRequest, passwd, keyfile);
            // 5. FileType transaction send
            TxSendResponse txSendResponse = xcubeClient.xCube.sendTransaction(txRequest).send();
            // 6. getTransaction recheck
            checkGetTransaction(txSendResponse, true);
            // 7. return TxSendResponse
            return txSendResponse;
        } catch (IOException e) {
            throw new TransactionException(e.toString());
        } finally {
            if (testfile != null) {
                testfile.delete();
            }
        }
    }

    @ApiOperation(value = "Send bonding transaction Client Sign")
    @RequestMapping(value = "sendBondingTxClientSign", method = RequestMethod.POST, produces = "application/json", consumes = {"multipart/form-data", "application/x-www-form-urlencoded"})
    public TxSendResponse sendBondingTxClientSign(TxRequest txRequest,
                                                  String input,
                                                  @RequestParam(value = "passwd", required = true) String passwd,
                                                  @ApiParam(value = "Sender Account encrypt key file select", required = true) @RequestPart(value = "keyfile") MultipartFile keyfile)
            throws TransactionException {
        //fee 10000000000000000000000
        //amount 1
        // 1. TxRequest payloadType check. (BondingType)
        if (txRequest.getPayloadType() != ApiEnum.PayloadType.BondingType) {
            throw new TransactionException("TxRequest is not BondingType. PayloadType check.");
        }
        // 2. TxRequest time setting
        if (txRequest.getTime() == null) {
            BigInteger time = new BigInteger(String.valueOf(DateUtil.getMicroSecond()));
            txRequest.setTime(time);
        }
        // 3. TxRequest payloadBody setting (TxBondingBody)
        TxBondingBody payloadBody = new TxBondingBody();
        if (payloadBody == null) {
            throw new TransactionException("TxBondingBody is null");
        }
        txRequest.setPayloadBody(payloadBody);
        // 4. Generate digital signature with selected key file
        clientSign(txRequest, passwd, keyfile);
        // 5. BondingType transaction send
        TxSendResponse txSendResponse = xcubeClient.xCube.bonding(txRequest).send();
        // 6. getTransaction recheck
        checkGetTransaction(txSendResponse, false);
        // 7. return TxSendResponse
        return txSendResponse;
    }

    @ApiOperation(value = "Send unbonding transaction Client Sign")
    @RequestMapping(value = "sendUnbondingTxClientSign", method = RequestMethod.POST, produces = "application/json", consumes = {"multipart/form-data", "application/x-www-form-urlencoded"})
    public TxSendResponse sendUnbondingTxClientSign(TxRequest txRequest,
                                                    String input,
                                                    @RequestParam(value = "passwd", required = true) String passwd,
                                                    @ApiParam(value = "Sender Account encrypt key file select", required = true) @RequestPart(value = "keyfile") MultipartFile keyfile)
            throws TransactionException {
        //fee 10000000000000000000000
        //amount 1
        // 1. TxRequest payloadType check. (UnbondingType)
        if (txRequest.getPayloadType() != ApiEnum.PayloadType.UnbondingType) {
            throw new TransactionException("TxRequest is not UnbondingType. PayloadType check.");
        }
        // 2. TxRequest time setting
        if (txRequest.getTime() == null) {
            BigInteger time = new BigInteger(String.valueOf(DateUtil.getMicroSecond()));
            txRequest.setTime(time);
        }
        // 3. TxRequest payloadBody setting (TxUnbondingBody)
        TxUnbondingBody payloadBody = new TxUnbondingBody(input);
        txRequest.setPayloadBody(payloadBody);
        if (payloadBody == null) {
            throw new TransactionException("TxUnbondingBody is null");
        }
        // 4. Generate digital signature with selected key file
        clientSign(txRequest, passwd, keyfile);
        // 5. UnbondingType transaction send
        TxSendResponse txSendResponse = xcubeClient.xCube.unbonding(txRequest).send();
        // 6. getTransaction recheck
        checkGetTransaction(txSendResponse, false);
        // 7. return TxSendResponse
        return txSendResponse;
    }

    @ApiOperation(value = "Send delegating transaction Client Sign")
    @RequestMapping(value = "sendDelegatingTxClientSign", method = RequestMethod.POST, produces = "application/json", consumes = {"multipart/form-data", "application/x-www-form-urlencoded"})
    public TxSendResponse sendDelegatingTxClientSign(TxRequest txRequest,
                                                     String input,
                                                     @RequestParam(value = "passwd", required = true) String passwd,
                                                     @ApiParam(value = "Sender Account encrypt key file select", required = true) @RequestPart(value = "keyfile") MultipartFile keyfile)
            throws TransactionException {
        // 1. TxRequest payloadType check. (DelegatingType)
        if (txRequest.getPayloadType() != ApiEnum.PayloadType.DelegatingType) {
            throw new TransactionException("TxRequest is not DelegatingType. PayloadType check.");
        }
        // 2. TxRequest time setting
        if (txRequest.getTime() == null) {
            BigInteger time = new BigInteger(String.valueOf(DateUtil.getMicroSecond()));
            txRequest.setTime(time);
        }
        // 3. TxRequest payloadBody setting (TxDelegatingBody)
        TxDelegatingBody payloadBody = new TxDelegatingBody(input);
        txRequest.setPayloadBody(payloadBody);
        if (payloadBody == null) {
            throw new TransactionException("TxDelegatingBody is null");
        }
        // 4. Generate digital signature with selected key file
        clientSign(txRequest, passwd, keyfile);
        // 5. DelegatingType transaction send
        TxSendResponse txSendResponse = xcubeClient.xCube.delegating(txRequest).send();
        // 6. getTransaction recheck
        checkGetTransaction(txSendResponse, false);
        // 7. return TxSendResponse
        return txSendResponse;
    }

    @ApiOperation(value = "Send undelegating transaction Client Sign")
    @RequestMapping(value = "sendUndelegatingTxClientSign", method = RequestMethod.POST, produces = "application/json", consumes = {"multipart/form-data", "application/x-www-form-urlencoded"})
    public TxSendResponse sendUndelegatingTxClientSign(TxRequest txRequest,
                                                       String input,
                                                       @RequestParam(value = "passwd", required = true) String passwd,
                                                       @ApiParam(value = "Sender Account encrypt key file select", required = true) @RequestPart(value = "keyfile") MultipartFile keyfile)
            throws TransactionException {
        // 1. TxRequest payloadType check. (UndelegatingType)
        if (txRequest.getPayloadType() != ApiEnum.PayloadType.UndelegatingType) {
            throw new TransactionException("TxRequest is not UndelegatingType. PayloadType check.");
        }
        // 2. TxRequest time setting
        if (txRequest.getTime() == null) {
            BigInteger time = new BigInteger(String.valueOf(DateUtil.getMicroSecond()));
            txRequest.setTime(time);
        }
        // 3. TxRequest payloadBody setting (TxUndelegatingBody)
        TxUndelegatingBody payloadBody = new TxUndelegatingBody(input);
        txRequest.setPayloadBody(payloadBody);
        if (payloadBody == null) {
            throw new TransactionException("TxUndelegatingBody is null");
        }
        // 4. Generate digital signature with selected key file
        clientSign(txRequest, passwd, keyfile);
        // 5. UndelegatingType transaction send
        TxSendResponse txSendResponse = xcubeClient.xCube.undelegating(txRequest).send();
        // 6. getTransaction recheck
        checkGetTransaction(txSendResponse, false);
        // 7. return TxSendResponse
        return txSendResponse;
    }

    @ApiOperation(value = "Send GRProposal transaction Client Sign")
    @RequestMapping(value = "sendGRProposalTxClientSign", method = RequestMethod.POST, produces = "application/json", consumes = {"multipart/form-data", "application/x-www-form-urlencoded"})
    public TxSendResponse sendGRProposalTxClientSign(TxRequest txRequest,
                                                     String input,
                                                     @RequestParam(value = "passwd", required = true) String passwd,
                                                     @ApiParam(value = "Sender Account encrypt key file select", required = true) @RequestPart(value = "keyfile") MultipartFile keyfile)
            throws TransactionException {
        //fee 10000000000000000000000
        //amount 1
        // 1. TxRequest payloadType check. (GRProposalType)
        if (txRequest.getPayloadType() != ApiEnum.PayloadType.GRProposalType) {
            throw new TransactionException("TxRequest is not GRProposalType. PayloadType check.");
        }
        // 2. TxRequest time setting
        if (txRequest.getTime() == null) {
            BigInteger time = new BigInteger(String.valueOf(DateUtil.getMicroSecond()));
            txRequest.setTime(time);
        }
        // 3. TxRequest payloadBody setting (TxGRProposalBody)
        TxGRProposalBody payloadBody = TxGRProposalBody.builder()
                .withCurrentReflection(new TxGRProposalBody.CurrentReflection(1, 2))
                .build();
        txRequest.setPayloadBody(payloadBody);
        if (payloadBody == null) {
            throw new TransactionException("TxGRProposalBody is null");
        }
        // 4. Generate digital signature with selected key file
        clientSign(txRequest, passwd, keyfile);
        // 5. GRProposalType transaction send
        TxSendResponse txSendResponse = xcubeClient.xCube.sendTransaction(txRequest).send();
        // 6. getTransaction recheck
        checkGetTransaction(txSendResponse, false);
        // 7. return TxSendResponse
        return txSendResponse;
    }

    @ApiOperation(value = "Send GRVote transaction Client Sign")
    @RequestMapping(value = "sendGRVoteTxClientSign", method = RequestMethod.POST, produces = "application/json", consumes = {"multipart/form-data", "application/x-www-form-urlencoded"})
    public TxSendResponse sendGRVoteTxClientSign(TxRequest txRequest,
                                                 String input,
                                                 @RequestParam(value = "passwd", required = true) String passwd,
                                                 @ApiParam(value = "Sender Account encrypt key file select", required = true) @RequestPart(value = "keyfile") MultipartFile keyfile)
            throws TransactionException {
        // 1. TxRequest payloadType check. (GRVoteType)
        if (txRequest.getPayloadType() != ApiEnum.PayloadType.GRVoteType) {
            throw new TransactionException("TxRequest is not GRVoteType. PayloadType check.");
        }
        // 2. TxRequest time setting
        if (txRequest.getTime() == null) {
            BigInteger time = new BigInteger(String.valueOf(DateUtil.getMicroSecond()));
            txRequest.setTime(time);
        }
        // 3. TxRequest payloadBody setting (TxGRVoteBody)
        TxGRVoteBody payloadBody = new TxGRVoteBody(true);
        txRequest.setPayloadBody(payloadBody);
        if (payloadBody == null) {
            throw new TransactionException("TxGRVoteBody is null");
        }
        // 4. Generate digital signature with selected key file
        clientSign(txRequest, passwd, keyfile);
        // 5. GRVoteType transaction send
        TxSendResponse txSendResponse = xcubeClient.xCube.sendTransaction(txRequest).send();
        // 6. getTransaction recheck
        checkGetTransaction(txSendResponse, false);
        // 7. return TxSendResponse
        return txSendResponse;
    }

    @ApiOperation(value = "Send RecoverValidator transaction Client Sign")
    @RequestMapping(value = "sendRecoverValidatorTxClientSign", method = RequestMethod.POST, produces = "application/json", consumes = {"multipart/form-data", "application/x-www-form-urlencoded"})
    public TxSendResponse sendRecoverValidatorTxClientSign(TxRequest txRequest,
                                                           String input,
                                                           @RequestParam(value = "passwd", required = true) String passwd,
                                                           @ApiParam(value = "Sender Account encrypt key file select", required = true) @RequestPart(value = "keyfile") MultipartFile keyfile)
            throws TransactionException {
        // 1. TxRequest payloadType check. (RecoverValidatorType)
        if (txRequest.getPayloadType() != ApiEnum.PayloadType.RecoverValidatorType) {
            throw new TransactionException("TxRequest is not RecoverValidatorType. PayloadType check.");
        }
        // 2. TxRequest time setting
        if (txRequest.getTime() == null) {
            BigInteger time = new BigInteger(String.valueOf(DateUtil.getMicroSecond()));
            txRequest.setTime(time);
        }
        // 3. TxRequest payloadBody setting (TxRecoverBody)
        TxRecoverBody payloadBody = new TxRecoverBody();
        txRequest.setPayloadBody(payloadBody);
        if (payloadBody == null) {
            throw new TransactionException("TxRecoverBody is null");
        }
        // 4. Generate digital signature with selected key file
        clientSign(txRequest, passwd, keyfile);
        // 5. RecoverValidatorType transaction send
        TxSendResponse txSendResponse = xcubeClient.xCube.sendTransaction(txRequest).send();
        // 6. getTransaction recheck
        checkGetTransaction(txSendResponse, false);
        // 7. return TxSendResponse
        return txSendResponse;
    }

    @ApiOperation(value = "Send XChain transaction Client Sign")
    @RequestMapping(value = "sendXChainTxClientSign", method = RequestMethod.POST, produces = "application/json", consumes = {"multipart/form-data", "application/x-www-form-urlencoded"})
    public TxSendResponse sendXChainTxClientSign(TxRequest txRequest,
                                                 String input,
                                                 @RequestParam(value = "passwd", required = true) String passwd,
                                                 @ApiParam(value = "Sender Account encrypt key file select", required = true) @RequestPart(value = "keyfile") MultipartFile keyfile)
            throws TransactionException {
        //fee 10000000000000000000000
        //amount 1
        // 1. TxRequest payloadType check. (MakeXChainType)
        if (txRequest.getPayloadType() != ApiEnum.PayloadType.MakeXChainType) {
            throw new TransactionException("TxRequest is not MakeXChainType. PayloadType check.");
        }
        // 2. TxRequest time setting
        if (txRequest.getTime() == null) {
            BigInteger time = new BigInteger(String.valueOf(DateUtil.getMicroSecond()));
            txRequest.setTime(time);
        }
        // 3. TxRequest payloadBody setting (TxMakeXChainBody)
        List<TxMakeXChainBody.AssetHolder> assetHolders = new ArrayList<>();
        assetHolders.add(new TxMakeXChainBody.AssetHolder(sender, fee));
        assetHolders.add(new TxMakeXChainBody.AssetHolder(receiver, fee));

        List<TxMakeXChainBody.Seed> seeds = new ArrayList<>();
        seeds.add(new TxMakeXChainBody.Seed("4bd2f7c0e58a868c6225cda8afcd2735ce9df4dd", "10.0.2.15", 9000));

        List<TxMakeXChainBody.Validator> validators = new ArrayList<>();
        validators.add(new TxMakeXChainBody.Validator(
                new TxMakeXChainBody.Validator.PubKey("xblock/PubKeySecp256k1", "AwdD/RUVKuvKWxn2ybEVPJ/R0eKeBjj1BNk/OcUZvG/+"),
                "120",
                "Xblocksystems",
                "Xblocksystems is specialized in the field of block chain and security certification.",
                "http://www.xblocksys.com",
                "http://www.xblocksys.com/img/xbs_08.png",
                "37.520958",
                "127.029161"
        ));
        validators.add(new TxMakeXChainBody.Validator(
                new TxMakeXChainBody.Validator.PubKey("xblock/PubKeySecp256k1", "AiapKeCawZDErZJKSqzGToBaR+6zy+ViU9d/S/noTaFB"),
                "100",
                "Xblocksystems",
                "Xblocksystems is specialized in the field of block chain and security certification.",
                "http://www.xblocksys.com",
                "http://www.xblocksys.com/img/xbs_08.png",
                "37.520958",
                "127.029161"
        ));

        TxMakeXChainBody payloadBody = TxMakeXChainBody.builder()
                .withDepth(10)
                .withHasAsset(true)
                .withNonExchangeChain(true)
                .withAirdropRate(100)
                .withAssetHolders(assetHolders)
                .withSeeds(seeds)
                .withValidators(validators)
                .build();
        txRequest.setPayloadBody(payloadBody);
        if (payloadBody == null) {
            throw new TransactionException("TxRecoverBody is null");
        }
        // 4. Generate digital signature with selected key file
        clientSign(txRequest, passwd, keyfile);
        // 5. MakeXChainType transaction send
        TxSendResponse txSendResponse = xcubeClient.xCube.sendTransaction(txRequest).send();
        // 6. getTransaction recheck
        checkGetTransaction(txSendResponse, false);
        // 7. return TxSendResponse
        return txSendResponse;
    }
}
