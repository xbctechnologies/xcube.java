package com.xbctechnologies.core.apis.dto.xtypes;

import com.xbctechnologies.core.apis.dto.ApiEnum;
import com.xbctechnologies.core.exception.TransactionException;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(subTypes = {TxCommonBody.class, TxFileBody.class, TxBondingBody.class, TxUnbondingBody.class, TxDelegatingBody.class, TxUndelegatingBody.class, TxGRProposalBody.class, TxGRVoteBody.class, TxRecoverBody.class, TxMakeXChainBody.class})
public interface TxBody<T> {
    void validate() throws TransactionException;

    @ApiModelProperty(hidden = true)
    String getMethod();

    @ApiModelProperty(hidden = true)
    ApiEnum.PayloadType getPayloadType();

    T marshalProto();
}
