package com.xbctechnologies.core.apis.dto.xtypes;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.xbctechnologies.core.apis.dto.ApiEnum;
import com.xbctechnologies.core.apis.dto.xtypes.proto.UndelegatingTxProto;
import com.xbctechnologies.core.exception.TransactionException;
import io.swagger.annotations.ApiParam;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@AllArgsConstructor
public class TxUndelegatingBody extends TxParentBody {
    @JsonIgnore
    private final String METHOD = "tx_undelegating";
    @ApiParam(value = "This is a field where the sender can enter a transaction value arbitrarily defined.")
    private String input;

    public static Builder builder() {
        return new Builder();
    }

    @Override
    public void validate() throws TransactionException {

    }

    @Override
    public String getMethod() {
        return METHOD;
    }

    @Override
    public ApiEnum.PayloadType getPayloadType() {
        return ApiEnum.PayloadType.UndelegatingType;
    }

    @Override
    public Object marshalProto() {
        UndelegatingTxProto.UndelegatingTx.Builder builder = UndelegatingTxProto.UndelegatingTx.newBuilder();
        if (input != null) {
            builder.setInput(input);
        }

        return builder.build();
    }

    public static final class Builder {
        private String input;

        private Builder() {
        }

        public Builder withInput(String input) {
            this.input = input;
            return this;
        }

        public TxUndelegatingBody build() {
            return new TxUndelegatingBody(input);
        }
    }
}
