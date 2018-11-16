package com.xbctechnologies.core.apis.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.xbctechnologies.core.apis.dto.xtypes.proto.*;
import com.xbctechnologies.core.utils.ParamUtil;
import com.xbctechnologies.core.apis.dto.xtypes.TxBody;
import com.xbctechnologies.core.apis.dto.xtypes.TxFileBody;
import com.xbctechnologies.core.apis.serializer.BigIntSerializer;
import com.xbctechnologies.core.apis.serializer.PayloadBodySerializer;
import com.xbctechnologies.core.exception.TransactionException;
import com.xbctechnologies.core.utils.SignUtil;
import com.xbctechnologies.xcrypto.util.ByteUtil;
import io.swagger.annotations.ApiParam;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;

import java.math.BigInteger;

@Data
public class TxRequest {
    @JsonIgnore
    @ApiParam(value = "Set if you want the application to set the ID for the request")
    private Long reqId;
    @ApiParam(value = "If the value is true, it responds after block creation.", defaultValue = "true", required = true)
    private boolean isSync;

    @ApiParam(value = "Set the unique xchain ID on xblockchain", defaultValue = ParamUtil.TARGET_CHAIN_ID, required = true)
    private String targetChainId;
    @ApiParam(value = "Set the address of the account that generates the transaction", defaultValue = ParamUtil.SENDER, required = true)
    private String sender;
    @ApiParam(value = "Set the address of the account receiving the transaction", defaultValue = ParamUtil.RECEIVER, required = true)
    private String receiver;

    @ApiParam(value = "Set the commission for creating the transaction.", defaultValue = ParamUtil.FEE, required = true)
    @JsonSerialize(using = BigIntSerializer.class)
    private BigInteger fee;
    @ApiParam(value = "Set the amount of coins to send to the receiver.", defaultValue = "0", required = true)
    @JsonSerialize(using = BigIntSerializer.class)
    private BigInteger amount;
    @ApiParam(value = "Input time at signing when transaction is signed by client", defaultValue = "0")
    @JsonSerialize(using = BigIntSerializer.class)
    private BigInteger time;

    private int v;
    @JsonSerialize(using = BigIntSerializer.class)
    private BigInteger r;
    @JsonSerialize(using = BigIntSerializer.class)
    private BigInteger s;

    @ApiParam(value = "Set the type of tx", defaultValue = "CommonType", required = true)
    private ApiEnum.PayloadType payloadType;
    @JsonSerialize(using = PayloadBodySerializer.class)
    private TxBody payloadBody;

    //For test
    @JsonIgnore
    @ApiParam(value = "Ignore client validation", defaultValue = "false")
    private boolean notCheckValidation;

    public TxProto.Tx marshalProto(boolean ignoreFile) {
        TxProto.Tx.Builder txBuilder = TxProto.Tx.newBuilder()
                .setTargetChainId(targetChainId)
                .setReceiver(ByteUtil.toNoPriFixHexString(receiver))
                .setFee(fee.toString())
                .setAmount(amount.toString())
                .setTime(time.toString());

        TxProto.TxPayload.Builder payloadBuilder = TxProto.TxPayload.newBuilder();
        payloadBuilder.setPayloadType(payloadType.toValue());
        payloadBuilder.clearPayloadBody();
        switch (payloadType) {
            case CommonType:
                payloadBuilder.setCommon((CommonTxProto.CommonTx) payloadBody.marshalProto());
                break;
            case FileType:
                payloadBuilder.setFile((FileTxProto.FileTx) ((TxFileBody) payloadBody).marshalProto(ignoreFile));
                break;
            case BondingType:
                payloadBuilder.setBonding((BondingTxProto.BondingTx) payloadBody.marshalProto());
                break;
            case UnbondingType:
                payloadBuilder.setUnbonding((UnbondingTxProto.UnbondingTx) payloadBody.marshalProto());
                break;
            case DelegatingType:
                payloadBuilder.setDelegating((DelegatingTxProto.DelegatingTx) payloadBody.marshalProto());
                break;
            case UndelegatingType:
                payloadBuilder.setUndelegating((UndelegatingTxProto.UndelegatingTx) payloadBody.marshalProto());
                break;
            case GRProposalType:
                payloadBuilder.setGrProposal((GrProposalTxProto.GrProposalTx) payloadBody.marshalProto());
                break;
            case GRVoteType:
                payloadBuilder.setGrVote((GrVoteTxProto.GrVoteTx) payloadBody.marshalProto());
                break;
            case RecoverValidatorType:
                payloadBuilder.setRecoverTx((RecoverTxProto.RecoverTx) payloadBody.marshalProto());
                break;
            case MakeXChainType:
                payloadBuilder.setMakeXChain((MakeXChainTxProto.MakeXChainTx) payloadBody.marshalProto());
                break;
        }
        txBuilder.setTxPayload(payloadBuilder.build());

        return txBuilder.build();
    }

    public static Builder builder() {
        return new Builder();
    }

    public void validate() throws TransactionException {
        if (StringUtils.isEmpty(targetChainId)) {
            throw new TransactionException("Must be set target chainId");
        }

        if (StringUtils.isEmpty(sender)) {
            throw new TransactionException("Must be set sender");
        }

        if (StringUtils.isEmpty(receiver)) {
            throw new TransactionException("Must be set receiver");
        }

        if (!sender.startsWith("0x") || !receiver.startsWith("0x")) {
            throw new TransactionException("The sender and receiver must have the prefix 0x");
        }

        if (payloadType == null) {
            throw new TransactionException("Must be set payload type");
        }

        if (r != null && s != null) {
            try {
                String addr = SignUtil.getAddress(this);
                if (!addr.equals(sender)) {
                    throw new TransactionException("The signature is invalid");
                }
            } catch (Exception e) {
                throw new TransactionException(e.getMessage());
            }
        }

        if (payloadBody == null) {
            throw new TransactionException("Must be set payload body");
        }

        switch (payloadType) {
            case CommonType:
            case BondingType:
            case UnbondingType:
            case DelegatingType:
            case UndelegatingType:
                break;
            default:
                if (amount != null && amount.compareTo(new BigInteger("0")) > 0) {
                    throw new TransactionException("If payloadtype is not commontype, amount should be set to null or 0");
                }
        }

        if (amount != null && amount.compareTo(new BigInteger("0")) < 0) {
            throw new TransactionException("Amount must be greater than zero");
        }

        if (payloadBody.getPayloadType() != payloadType) {
            throw new TransactionException(String.format("If payloadbody is %s, then payloadtype must be set to %s.", payloadBody.getClass().getSimpleName(), payloadBody.getPayloadType()));
        }

        switch (payloadType) {
            case FileType:
                if (((TxFileBody) payloadBody).getOp() == null) {
                    throw new TransactionException("The op field must be set");
                }
                switch (((TxFileBody) payloadBody).getOp()) {
                    case RegisterType:
                        if (!sender.equals(receiver)) {
                            throw new TransactionException("File registration should be the same as sender and receiver");
                        }
                        break;
                    case UpdateType:
                        if (sender.equals(receiver)) {
                            throw new TransactionException("The receiver field must use the value returned when registering the file.");
                        }
                        break;
                }
                break;
            case BondingType:
            case UnbondingType:
            case GRProposalType:
            case GRVoteType:
            case RecoverValidatorType:
            case MakeXChainType:
                if (!sender.equals(receiver)) {
                    throw new TransactionException("The sender and receiver should be the same.");
                }
                break;
        }
    }

    public void postProcess() {
    }

    public static final class Builder {
        private Long id;
        private boolean isSync;
        private String sender;
        private String receiver;
        private String targetChainId;
        private BigInteger fee = new BigInteger("0");
        private BigInteger amount = new BigInteger("0");
        private BigInteger time = new BigInteger("0");
        private ApiEnum.PayloadType payloadType;
        private TxBody payloadBody;
        private boolean notCheckValidation;

        private Builder() {
        }

        public static Builder aTxRequest() {
            return new Builder();
        }

        public Builder withId(Long id) {
            this.id = id;
            return this;
        }

        public Builder withIsSync(boolean isSync) {
            this.isSync = isSync;
            return this;
        }

        public Builder withSender(String sender) {
            this.sender = sender;
            return this;
        }

        public Builder withReceiver(String receiver) {
            this.receiver = receiver;
            return this;
        }

        public Builder withTargetChainId(String targetChainId) {
            this.targetChainId = targetChainId;
            return this;
        }

        public Builder withFee(BigInteger fee) {
            this.fee = fee;
            return this;
        }

        public Builder withAmount(BigInteger amount) {
            this.amount = amount;
            return this;
        }

        public Builder withTime(BigInteger time) {
            this.time = time;
            return this;
        }

        public Builder withPayloadType(ApiEnum.PayloadType payloadType) {
            this.payloadType = payloadType;
            return this;
        }

        public Builder withPayloadBody(TxBody payloadBody) {
            this.payloadBody = payloadBody;
            return this;
        }

        public Builder withNotCheckValidation(boolean notCheckValidation) {
            this.notCheckValidation = notCheckValidation;
            return this;
        }

        public TxRequest build() {
            TxRequest txRequest = new TxRequest();
            txRequest.setReqId(id);
            txRequest.setSync(isSync);
            txRequest.setSender(sender);
            txRequest.setReceiver(receiver);
            txRequest.setTargetChainId(targetChainId);
            txRequest.setFee(fee);
            txRequest.setAmount(amount);
            txRequest.setTime(time);
            txRequest.setPayloadType(payloadType);
            txRequest.setPayloadBody(payloadBody);
            txRequest.setNotCheckValidation(notCheckValidation);
            return txRequest;
        }
    }
}