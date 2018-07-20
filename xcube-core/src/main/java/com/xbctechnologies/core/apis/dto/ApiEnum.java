package com.xbctechnologies.core.apis.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public class ApiEnum {
    public enum PayloadType {
        CommonType(1),
        FileType(2),
        BondingType(3),
        UnbondingType(4),
        DelegatingType(5),
        UndelegatingType(6),
        GRProposalType(7),
        GRVoteType(8),
        RecoverValidatorType(9),
        MakeXChainType(10);

        private int value;

        PayloadType(int value) {
            this.value = value;
        }

        /**
         * Use deserialize
         *
         * @param value
         * @return
         */
        @JsonCreator
        public static PayloadType fromValue(int value) {
            switch (value) {
                case 1:
                    return CommonType;
                case 2:
                    return FileType;
                case 3:
                    return BondingType;
                case 4:
                    return UnbondingType;
                case 5:
                    return DelegatingType;
                case 6:
                    return UndelegatingType;
                case 7:
                    return GRProposalType;
                case 8:
                    return GRVoteType;
                case 9:
                    return RecoverValidatorType;
                case 10:
                    return MakeXChainType;
            }
            return CommonType;
        }

        /**
         * Use serialize
         *
         * @return
         */
        @JsonValue
        public int toValue() {
            return this.value;
        }
    }

    public enum OpType {
        RegisterType(1),
        UpdateType(2);

        private int value;

        OpType(int value) {
            this.value = value;
        }

        @JsonCreator
        public static OpType fromValue(int value) {
            switch (value) {
                case 1:
                    return RegisterType;
                case 2:
                    return UpdateType;
            }

            return RegisterType;
        }

        @JsonValue
        public int toValue() {
            return this.value;
        }
    }

    public enum FreezingType {
        NONE(0),
        Byzantine(1),
        Disconnected(2);

        private int value;

        FreezingType(int value) {
            this.value = value;
        }

        @JsonCreator
        public static FreezingType fromValue(int value) {
            switch (value) {
                case 1:
                    return Byzantine;
                case 2:
                    return Disconnected;
            }

            return NONE;
        }

        @JsonValue
        public int toValue() {
            return this.value;
        }
    }
}
