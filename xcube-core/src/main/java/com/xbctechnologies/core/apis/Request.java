package com.xbctechnologies.core.apis;

import com.xbctechnologies.core.apis.dto.JsonRPC;
import com.xbctechnologies.core.apis.dto.res.Response;
import com.xbctechnologies.core.apis.dto.res.tx.TxReceiptResponse;
import com.xbctechnologies.core.apis.dto.res.tx.TxResponse;
import com.xbctechnologies.core.apis.dto.xtypes.*;
import com.xbctechnologies.core.component.rest.RestHttpClient;
import com.xbctechnologies.core.component.rest.dto.RestReqDto;
import com.xbctechnologies.core.component.rest.dto.RestResDto;
import com.xbctechnologies.core.utils.Base64Util;
import com.xbctechnologies.core.utils.JsonUtil;

import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

public class Request<S, T extends Response> {
    private static AtomicLong nextId = new AtomicLong(0);

    private RestHttpClient restHttpClient;
    private long id;
    private String method;
    private List<S> params;
    private Class<T> responseType;

    public Request(RestHttpClient restHttpClient, Long id, String method, List<S> params, Class<T> responseType) {
        this.restHttpClient = restHttpClient;
        this.id = id == null ? nextId.incrementAndGet() : id;
        this.method = method;
        this.params = params;
        this.responseType = responseType;
    }

    private void generatePayloadBody(T o) {
        if ((responseType != TxResponse.class && responseType != TxReceiptResponse.class)
                || o.getError() != null || o.getResult() == null) {
            return;
        }

        TxResponse.Result txResult = null;
        if (responseType == TxReceiptResponse.class) {
            txResult = ((TxReceiptResponse) o).getResult();
        } else if (responseType == TxResponse.class) {
            txResult = ((TxResponse) o).getResult();
        }

        String decodedBody = Base64Util.decodeStr(txResult.getPayloadBody());
        switch (txResult.getPayloadType()) {
            case CommonType:
                txResult.setPayloadBodyObj(JsonUtil.generateJsonToClass(decodedBody, TxCommonBody.class));
                break;
            case FileType:
                txResult.setPayloadBodyObj(JsonUtil.generateJsonToClass(decodedBody, TxFileBody.class));
                break;
            case GRProposalType:
                txResult.setPayloadBodyObj(JsonUtil.generateJsonToClass(decodedBody, TxGRProposalBody.class));
                break;
            case GRVoteType:
                txResult.setPayloadBodyObj(JsonUtil.generateJsonToClass(decodedBody, TxGRVoteBody.class));
                break;
            case RecoverValidatorType:
                txResult.setPayloadBodyObj(JsonUtil.generateJsonToClass(decodedBody, TxRecoverBody.class));
                break;
            case MakeXChainType:
                txResult.setPayloadBodyObj(JsonUtil.generateJsonToClass(decodedBody, TxMakeXChainBody.class));
                break;
            case BondingType:
                txResult.setPayloadBodyObj(JsonUtil.generateJsonToClass(decodedBody, TxBondingBody.class));
                break;
            case UnbondingType:
                txResult.setPayloadBodyObj(JsonUtil.generateJsonToClass(decodedBody, TxUnbondingBody.class));
                break;
            case DelegatingType:
                txResult.setPayloadBodyObj(JsonUtil.generateJsonToClass(decodedBody, TxDelegatingBody.class));
                break;
            case UndelegatingType:
                txResult.setPayloadBodyObj(JsonUtil.generateJsonToClass(decodedBody, TxUndelegatingBody.class));
                break;
        }
    }

    public T send() {
        JsonRPC jsonRPC = JsonRPC.builder()
                .withID(id)
                .withMethod(method)
                .withParams(params)
                .build();

        RestReqDto restReqDto = RestReqDto.builder()
                .withType(RestReqDto.Type.POST)
                .withIsJsonBody(true)
                .withRequestParams(jsonRPC)
                .build();

        RestResDto restResDto = restHttpClient.request(restReqDto);
        T o = JsonUtil.generateJsonToClass(restResDto.getResponseBody(), responseType);
        generatePayloadBody(o);

        return o;
    }
}
