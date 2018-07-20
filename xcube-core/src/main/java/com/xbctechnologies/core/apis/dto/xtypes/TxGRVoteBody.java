package com.xbctechnologies.core.apis.dto.xtypes;

import com.xbctechnologies.core.apis.dto.ApiEnum;
import com.xbctechnologies.core.apis.dto.xtypes.proto.GrVoteTxProto;
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
public class TxGRVoteBody extends TxParentBody {
    private boolean yesOrNo;

    public static Builder builder() {
        return new Builder();
    }

    @Override
    public void validate() throws TransactionException {

    }

    @Override
    @ApiParam(hidden = true)
    public String getMethod() {
        return METHOD;
    }

    @Override
    public ApiEnum.PayloadType getPayloadType() {
        return ApiEnum.PayloadType.GRVoteType;
    }

    @Override
    public Object marshalProto() {
        return GrVoteTxProto.GrVoteTx.newBuilder()
                .setYesOrNo(yesOrNo)
                .build();
    }

    public static final class Builder {
        private boolean yesOrNo;

        private Builder() {
        }

        public Builder withYesOrNo(boolean yesOrNo) {
            this.yesOrNo = yesOrNo;
            return this;
        }

        public TxGRVoteBody build() {
            return new TxGRVoteBody(yesOrNo);
        }
    }
}
