package com.xbctechnologies.core.apis.dto.res.tx;

import com.xbctechnologies.core.apis.dto.ApiEnum;
import com.xbctechnologies.core.apis.dto.TxRequest;
import com.xbctechnologies.core.apis.dto.res.Response;
import com.xbctechnologies.core.apis.dto.xtypes.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigInteger;

@Data
@EqualsAndHashCode(callSuper = false)
public class TxResponse extends Response<TxResponse.Result> {
    public TxResponse.Result getTransaction() {
        return getResult();
    }

    @Data
    public static class Result {
        private String targetChainId;
        private String dataAccount;
        private String blockHash;
        private long blockNumber;
        private String txHash;
        private int txIndex;
        private long txSize;

        private String sender;
        private String receiver;
        private BigInteger amount;
        private BigInteger fee;
        private BigInteger time;

        private String v;
        private BigInteger r;
        private BigInteger s;

        private ApiEnum.PayloadType payloadType;
        private String payloadBody;
        private Object payloadBodyObj;
    }

    public TxRequest generateTxRequest(boolean isSync) {
        TxResponse.Result result = this.getResult();

        TxBody payloadBody = null;
        switch (result.payloadType) {
            case CommonType:
                payloadBody = (TxCommonBody) result.payloadBodyObj;
                break;
            case FileType:
                payloadBody = (TxFileBody) result.payloadBodyObj;//JsonUtil.generateJsonToClass(result.payloadBody, TxFileBody.class);
                break;
            case BondingType:
                payloadBody = (TxBondingBody) result.payloadBodyObj;//JsonUtil.generateJsonToClass(result.payloadBody, TxBondingBody.class);
                break;
            case UnbondingType:
                payloadBody = (TxUnbondingBody) result.payloadBodyObj;//JsonUtil.generateJsonToClass(result.payloadBody, TxUnbondingBody.class);
                break;
            case DelegatingType:
                payloadBody = (TxDelegatingBody) result.payloadBodyObj;//JsonUtil.generateJsonToClass(result.payloadBody, TxDelegatingBody.class);
                break;
            case UndelegatingType:
                payloadBody = (TxUndelegatingBody) result.payloadBodyObj;//JsonUtil.generateJsonToClass(result.payloadBody, TxUndelegatingBody.class);
                break;
            case GRProposalType:
                payloadBody = (TxGRProposalBody) result.payloadBodyObj;// JsonUtil.generateJsonToClass(result.payloadBody, TxGRProposalBody.class);
                break;
            case GRVoteType:
                payloadBody = (TxGRVoteBody) result.payloadBodyObj;//JsonUtil.generateJsonToClass(result.payloadBody, TxGRVoteBody.class);
                break;
            case RecoverValidatorType:
                payloadBody = (TxRecoverBody) result.payloadBodyObj;//JsonUtil.generateJsonToClass(result.payloadBody, TxRecoverBody.class);
                break;
            case MakeXChainType:
                payloadBody = (TxMakeXChainBody) result.payloadBodyObj;//JsonUtil.generateJsonToClass(result.payloadBody, TxMakeXChainBody.class);
                break;
        }
        return TxRequest.builder()
                .withIsSync(isSync)
                .withTargetChainId(result.getTargetChainId())
                .withSender(result.sender)
                .withReceiver(result.receiver)
                .withFee(result.fee)
                .withAmount(result.amount)
                .withTime(result.time)
                .withPayloadType(result.payloadType)
                .withPayloadBody(payloadBody)
                .build();
    }
}