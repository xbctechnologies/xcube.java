package com.xbctechnologies.core.apis.dto.xtypes;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.xbctechnologies.core.apis.dto.ApiEnum;
import com.xbctechnologies.core.apis.dto.xtypes.proto.BondingTxProto;
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
public class TxBondingBody extends TxParentBody {
    @JsonIgnore
    private final String METHOD = "tx_bonding";
    @ApiParam(value = "This is a company name")
    private String companyName;
    @ApiParam(value = "This is a company description")
    private String companyDesc;
    @ApiParam(value = "This is a company url")
    private String companyUrl;
    @ApiParam(value = "This is a company logo url")
    private String companyLogoUrl;
    @ApiParam(value = "This is a company latitude")
    private String companyLat;
    @ApiParam(value = "This is a company longitude")
    private String companyLon;

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
        return ApiEnum.PayloadType.BondingType;
    }

    @Override
    public Object marshalProto() {
        BondingTxProto.BondingTx.Builder builder = BondingTxProto.BondingTx.newBuilder();
        if (companyName != null) {
            builder.setCompanyName(companyName);
        }
        if (companyDesc != null) {
            builder.setCompanyDesc(companyDesc);
        }
        if (companyUrl != null) {
            builder.setCompanyUrl(companyUrl);
        }
        if (companyLogoUrl != null) {
            builder.setCompanyLogoUrl(companyLogoUrl);
        }
        if (companyLat != null) {
            builder.setCompanyLat(companyLat);
        }
        if (companyLon != null) {
            builder.setCompanyLon(companyLon);
        }

        return builder.build();
    }

    public static final class Builder {
        private String companyName;
        private String companyDesc;
        private String companyUrl;
        private String companyLogoUrl;
        private String companyLat;
        private String companyLon;

        private Builder() {
        }

        public Builder withCompanyName(String companyName) {
            this.companyName = companyName;
            return this;
        }

        public Builder withCompanyDesc(String companyDesc) {
            this.companyDesc = companyDesc;
            return this;
        }

        public Builder withCompanyUrl(String companyUrl) {
            this.companyUrl = companyUrl;
            return this;
        }

        public Builder withCompanyLogoUrl(String companyLogoUrl) {
            this.companyLogoUrl = companyLogoUrl;
            return this;
        }

        public Builder withCompanyLat(String companyLat) {
            this.companyLat = companyLat;
            return this;
        }

        public Builder withCompanyLon(String companyLon) {
            this.companyLon = companyLon;
            return this;
        }

        public TxBondingBody build() {
            return new TxBondingBody(companyName, companyDesc, companyUrl, companyLogoUrl, companyLat, companyLon);
        }
    }
}
