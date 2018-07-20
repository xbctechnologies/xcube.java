package com.xbctechnologies.core.apis.dto.xtypes;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.xbctechnologies.core.apis.dto.ApiEnum;
import com.xbctechnologies.core.apis.dto.xtypes.proto.MakeXChainTxProto;
import com.xbctechnologies.core.apis.serializer.BigIntSerializer;
import com.xbctechnologies.core.apis.serializer.NumberToStringSerializer;
import com.xbctechnologies.core.exception.TransactionException;
import com.xbctechnologies.xcrypto.util.ByteUtil;
import io.swagger.annotations.ApiParam;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.math.BigInteger;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@AllArgsConstructor
public class TxMakeXChainBody extends TxParentBody {
    @ApiParam(value = "Set how many child chains can be created", required = true)
    @JsonSerialize(using = NumberToStringSerializer.class)
    private long depth;
    @ApiParam(value = "Whether you can have assets", required = true)
    private boolean hasAsset;
    @ApiParam(value = "Whether the child chain can have assets", required = true)
    private boolean enableSubAsset;
    @ApiParam(value = "Enable exchange", required = true)
    private boolean nonExchangeChain;

    @ApiParam(value = "Airdrop rate", defaultValue = "0")
    @JsonSerialize(using = NumberToStringSerializer.class)
    private long airdropRate;
    @ApiParam(value = "Airdrop holders")
    private List<AssetHolder> assetHolders;
    @ApiParam(value = "Seeds")
    private List<Seed> seeds;

    @ApiParam(value = "Validator list when creating a chain with assets")
    private List<Validator> validators;
    @ApiParam(value = "Chain description")
    private String customDesc;
    @ApiParam(value = "Coin Name")
    private String coinName;

    public static Builder builder() {
        return new Builder();
    }

    @Override
    public void validate() throws TransactionException {
        if (!hasAsset && !nonExchangeChain) {
            throw new TransactionException("If the hasAsset field is false, the nonExchangeChain field must be true.");
        }

        if (!hasAsset && coinName != null) {
            throw new TransactionException("If the hasAsset field value is false, the coinName field value should not be set.");
        }

        if (airdropRate > 100) {
            throw new TransactionException("The AirdropRate field should be equal to or less than 100.");
        }

        if (assetHolders != null) {
            for (AssetHolder assetHolder : assetHolders) {
                if (assetHolder.amount == null || "0".equals(assetHolder.amount.toString())) {
                    throw new TransactionException(String.format("The amount of %s account must be greater than zero.", assetHolder.accountAddr));
                }
            }
        }
    }

    @Override
    @ApiParam(hidden = true)
    public String getMethod() {
        return METHOD;
    }

    @Override
    public ApiEnum.PayloadType getPayloadType() {
        return ApiEnum.PayloadType.MakeXChainType;
    }

    @Override
    public Object marshalProto() {
        MakeXChainTxProto.MakeXChainTx.Builder builder = MakeXChainTxProto.MakeXChainTx.newBuilder()
                .setDepth(depth)
                .setHasAsset(hasAsset)
                .setEnableSubAsset(enableSubAsset)
                .setNonExchangeChain(nonExchangeChain)
                .setAirdropRate(airdropRate);
        if (customDesc != null) {
            builder.setCustomDesc(customDesc);
        }
        if (coinName != null) {
            builder.setCoinName(coinName);
        }

        if (validators != null) {
            int[] idx = {0};
            validators.forEach(item -> {
                MakeXChainTxProto.ValidatorType.Builder validatorBuild = MakeXChainTxProto.ValidatorType.newBuilder()
                        .setPubKey(MakeXChainTxProto.PubKey.newBuilder().setType(item.pubKey.type).setValue(item.pubKey.value).build())
                        .setPower(item.power);

                if (item.companyName != null) {
                    validatorBuild.setCompanyName(item.companyName);
                }
                if (item.companyDesc != null) {
                    validatorBuild.setCompanyDesc(item.companyDesc);
                }
                if (item.companyUrl != null) {
                    validatorBuild.setCompanyUrl(item.companyUrl);
                }
                if (item.companyLogoUrl != null) {
                    validatorBuild.setCompanyLogoUrl(item.companyLogoUrl);
                }
                if (item.companyLat != null) {
                    validatorBuild.setCompanyLat(item.companyLat);
                }
                if (item.companyLon != null) {
                    validatorBuild.setCompanyLon(item.companyLon);
                }

                builder.addValidators(
                        idx[0]++,
                        validatorBuild.build()
                );
            });
        }
        if (seeds != null) {
            int[] idx = {0};
            seeds.forEach(item -> {
                builder.addSeeds(
                        idx[0]++,
                        MakeXChainTxProto.Seed.newBuilder()
                                .setId(item.id)
                                .setIp(item.ip)
                                .setPort(item.port)
                                .build()
                );
            });
        }
        if (assetHolders != null) {
            int[] idx = {0};
            assetHolders.forEach(item -> {
                builder.addAssetHolders(
                        idx[0]++,
                        MakeXChainTxProto.AssetHolder.newBuilder()
                                .setAddress(ByteUtil.toNoPriFixHexString(item.accountAddr))
                                .setAmount(item.amount.toString()).build()
                );
            });
        }
        return builder.build();
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AssetHolder {
        private String accountAddr;
        @JsonSerialize(using = BigIntSerializer.class)
        private BigInteger amount;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Seed {
        private String id;
        private String ip;
        @JsonSerialize(using = NumberToStringSerializer.class)
        private long port;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Validator {
        @JsonProperty(value = "pub_key")
        private PubKey pubKey;
        private String power;

        private String companyName;
        private String companyDesc;
        private String companyUrl;
        private String companyLogoUrl;
        private String companyLat;
        private String companyLon;

        @Data
        @NoArgsConstructor
        @AllArgsConstructor
        public static class PubKey {
            private String type;
            private String value;
        }
    }

    public static final class Builder {
        private long depth;
        private boolean hasAsset;
        private boolean enableSubAsset;
        private boolean nonExchangeChain;
        private long airdropRate;
        private List<AssetHolder> assetHolders;
        private List<Seed> seeds;
        private List<Validator> validators;
        private String customDesc;
        private String coinName;

        private Builder() {
        }

        public Builder withDepth(long depth) {
            this.depth = depth;
            return this;
        }

        public Builder withHasAsset(boolean hasAsset) {
            this.hasAsset = hasAsset;
            return this;
        }

        public Builder withEnableSubAsset(boolean enableSubAsset) {
            this.enableSubAsset = enableSubAsset;
            return this;
        }

        public Builder withNonExchangeChain(boolean nonExchangeChain) {
            this.nonExchangeChain = nonExchangeChain;
            return this;
        }

        public Builder withAirdropRate(long airdropRate) {
            this.airdropRate = airdropRate;
            return this;
        }

        public Builder withAssetHolders(List<AssetHolder> assetHolders) {
            this.assetHolders = assetHolders;
            return this;
        }

        public Builder withSeeds(List<Seed> seeds) {
            this.seeds = seeds;
            return this;
        }

        public Builder withValidators(List<Validator> validators) {
            this.validators = validators;
            return this;
        }

        public Builder withCustomDesc(String customDesc) {
            this.customDesc = customDesc;
            return this;
        }

        public Builder withCoinName(String coinName) {
            this.coinName = coinName;
            return this;
        }

        public TxMakeXChainBody build() {
            return new TxMakeXChainBody(depth, hasAsset, enableSubAsset, nonExchangeChain, airdropRate, assetHolders, seeds, validators, customDesc, coinName);
        }
    }
}
