package com.xbctechnologies.core.apis.dto.xtypes;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.xbctechnologies.core.apis.dto.ApiEnum;
import com.xbctechnologies.core.apis.dto.xtypes.proto.GrProposalTxProto;
import com.xbctechnologies.core.apis.serializer.BigIntSerializer;
import com.xbctechnologies.core.apis.serializer.NumberToStringSerializer;
import com.xbctechnologies.core.exception.TransactionException;
import io.swagger.annotations.ApiParam;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.math.BigInteger;

@Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@AllArgsConstructor
public class TxGRProposalBody extends TxParentBody {
    @JsonSerialize(using = BigIntSerializer.class)
    private BigInteger rewardXtoPerCoin;

    @JsonSerialize(using = BigIntSerializer.class)
    private BigInteger minCommonTxFee;
    @JsonSerialize(using = BigIntSerializer.class)
    private BigInteger minBondingTxFee;
    @JsonSerialize(using = BigIntSerializer.class)
    private BigInteger minGRProposalTxFee;
    @JsonSerialize(using = BigIntSerializer.class)
    private BigInteger minGRVoteTxFee;
    @JsonSerialize(using = BigIntSerializer.class)
    private BigInteger minXTxFee;

    @JsonSerialize(using = NumberToStringSerializer.class)
    private long maxBlockNumsForVoting;
    @JsonSerialize(using = NumberToStringSerializer.class)
    private long minBlockNumsToGRProposal;
    @JsonSerialize(using = NumberToStringSerializer.class)
    private long minBlockNumsUtilReflection;
    @JsonSerialize(using = NumberToStringSerializer.class)
    private long maxBlockNumsUtilReflection;

    @JsonSerialize(using = NumberToStringSerializer.class)
    private long blockNumsFreezingValidator;
    @JsonSerialize(using = NumberToStringSerializer.class)
    private long blockNumsUtilUnbonded;
    @JsonSerialize(using = NumberToStringSerializer.class)
    private long maxDelegatableValidatorNums;
    @JsonSerialize(using = NumberToStringSerializer.class)
    private long validatorNums;

    private String firstCompatibleVersion;

    private CurrentReflection currentReflection;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CurrentReflection {
        @JsonSerialize(using = NumberToStringSerializer.class)
        private long blockNumsForVoting;
        @JsonSerialize(using = NumberToStringSerializer.class)
        private long blockNumsUtilReflection;
    }

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
        return ApiEnum.PayloadType.GRProposalType;
    }

    @Override
    public Object marshalProto() {
        GrProposalTxProto.GrProposalTx.Builder builder = GrProposalTxProto.GrProposalTx.newBuilder()
                .setRewardXtoPerCoin(rewardXtoPerCoin.toString())
                .setMinCommonTxFee(minCommonTxFee.toString())
                .setMinBondingTxFee(minBondingTxFee.toString())
                .setMinGRProposalTxFee(minGRProposalTxFee.toString())
                .setMinGRVoteTxFee(minGRVoteTxFee.toString())
                .setMinXTxFee(minXTxFee.toString())
                .setMaxBlockNumsForVoting(maxBlockNumsForVoting)
                .setMinBlockNumsToGRProposal(minBlockNumsToGRProposal)
                .setMinBlockNumsUtilReflection(minBlockNumsUtilReflection)
                .setMaxBlockNumsUtilReflection(maxBlockNumsUtilReflection)
                .setBlockNumsFreezingValidator(blockNumsFreezingValidator)
                .setBlockNumsUtilUnbonded(blockNumsUtilUnbonded)
                .setMaxDelegatableValidatorNums(maxDelegatableValidatorNums)
                .setValidatorNums(validatorNums)
                .setCurrentReflection(
                        GrProposalTxProto.CurrentReflection.newBuilder()
                                .setBlockNumsForVoting(currentReflection.blockNumsForVoting)
                                .setBlockNumsUtilReflection(currentReflection.blockNumsUtilReflection)
                );

        if (firstCompatibleVersion != null) {
            builder.setFirstCompatibleVersion(firstCompatibleVersion);
        }
        return builder.build();
    }

    public static final class Builder {
        private BigInteger rewardXtoPerCoin = new BigInteger("-1");
        private BigInteger minCommonTxFee = new BigInteger("-1");
        private BigInteger minBondingTxFee = new BigInteger("-1");
        private BigInteger minGRProposalTxFee = new BigInteger("-1");
        private BigInteger minGRVoteTxFee = new BigInteger("-1");
        private BigInteger minXTxFee = new BigInteger("-1");
        private long maxBlockNumsForVoting = -1;
        private long minBlockNumsToGRProposal = -1;
        private long minBlockNumsUtilReflection = -1;
        private long maxBlockNumsUtilReflection = -1;
        private long blockNumsFreezingValidator = -1;
        private long blockNumsUtilUnbonded = -1;
        private long maxDelegatableValidatorNums = -1;
        private long validatorNums = -1;
        private String firstCompatibleVersion/* = ""*/;
        private CurrentReflection currentReflection/* = new CurrentReflection()*/;

        private Builder() {
        }

        public Builder withRewardXtoPerCoin(BigInteger rewardXtoPerCoin) {
            this.rewardXtoPerCoin = rewardXtoPerCoin;
            return this;
        }

        public Builder withMinCommonTxFee(BigInteger minCommonTxFee) {
            this.minCommonTxFee = minCommonTxFee;
            return this;
        }

        public Builder withMinBondingTxFee(BigInteger minBondingTxFee) {
            this.minBondingTxFee = minBondingTxFee;
            return this;
        }

        public Builder withMinGRProposalTxFee(BigInteger minGRProposalTxFee) {
            this.minGRProposalTxFee = minGRProposalTxFee;
            return this;
        }

        public Builder withMinGRVoteTxFee(BigInteger minGRVoteTxFee) {
            this.minGRVoteTxFee = minGRVoteTxFee;
            return this;
        }

        public Builder withMinXTxFee(BigInteger minXTxFee) {
            this.minXTxFee = minXTxFee;
            return this;
        }

        public Builder withMaxBlockNumsForVoting(long maxBlockNumsForVoting) {
            this.maxBlockNumsForVoting = maxBlockNumsForVoting;
            return this;
        }

        public Builder withMinBlockNumsToGRProposal(long minBlockNumsToGRProposal) {
            this.minBlockNumsToGRProposal = minBlockNumsToGRProposal;
            return this;
        }

        public Builder withMinBlockNumsUtilReflection(long minBlockNumsUtilReflection) {
            this.minBlockNumsUtilReflection = minBlockNumsUtilReflection;
            return this;
        }

        public Builder withMaxBlockNumsUtilReflection(long maxBlockNumsUtilReflection) {
            this.maxBlockNumsUtilReflection = maxBlockNumsUtilReflection;
            return this;
        }

        public Builder withBlockNumsFreezingValidator(long blockNumsFreezingValidator) {
            this.blockNumsFreezingValidator = blockNumsFreezingValidator;
            return this;
        }

        public Builder withBlockNumsUtilUnbonded(long blockNumsUtilUnbonded) {
            this.blockNumsUtilUnbonded = blockNumsUtilUnbonded;
            return this;
        }

        public Builder withMaxDelegatableValidatorNums(long maxDelegatableValidatorNums) {
            this.maxDelegatableValidatorNums = maxDelegatableValidatorNums;
            return this;
        }

        public Builder withValidatorNums(long validatorNums) {
            this.validatorNums = validatorNums;
            return this;
        }

        public Builder withFirstCompatibleVersion(String firstCompatibleVersion) {
            this.firstCompatibleVersion = firstCompatibleVersion;
            return this;
        }

        public Builder withCurrentReflection(CurrentReflection currentReflection) {
            this.currentReflection = currentReflection;
            return this;
        }

        public TxGRProposalBody build() {
            return new TxGRProposalBody(rewardXtoPerCoin, minCommonTxFee, minBondingTxFee, minGRProposalTxFee, minGRVoteTxFee, minXTxFee, maxBlockNumsForVoting, minBlockNumsToGRProposal, minBlockNumsUtilReflection, maxBlockNumsUtilReflection, blockNumsFreezingValidator, blockNumsUtilUnbonded, maxDelegatableValidatorNums, validatorNums, firstCompatibleVersion, currentReflection);
        }
    }
}
