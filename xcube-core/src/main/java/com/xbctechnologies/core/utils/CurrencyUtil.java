package com.xbctechnologies.core.utils;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import java.math.BigInteger;

public class CurrencyUtil {
    private static final String XTO = "1";
    private static final String KXTO = "1000";
    private static final String MXTO = "1000000";
    private static final String GXTO = "1000000000";
    private static final String MICROCoin = "1000000000000";
    private static final String MILLICoin = "1000000000000000";
    private static final String Coin = "1000000000000000000";

    public enum CurrencyType {
        XTOType(1),
        KXTOType(2),
        MXTOType(3),
        GXTOType(4),
        MICROCoinType(5),
        MILLICoinType(6),
        CoinType(7);

        private int value;

        CurrencyType(int value) {
            this.value = value;
        }

        @JsonCreator
        public static CurrencyType fromValue(int value) {
            switch (value) {
                case 1:
                    return XTOType;
                case 2:
                    return KXTOType;
                case 3:
                    return MXTOType;
                case 4:
                    return GXTOType;
                case 5:
                    return MICROCoinType;
                case 6:
                    return MILLICoinType;
                case 7:
                    return CoinType;
            }
            return XTOType;
        }


        @JsonValue
        public int toValue() {
            return this.value;
        }
    }

    public static BigInteger generateXTO(CurrencyType currencyType, long amount) {
        return generateXTO(currencyType, new BigInteger(String.valueOf(amount)));
    }

    public static BigInteger generateXTO(CurrencyType currencyType, BigInteger amount) {
        BigInteger xto = new BigInteger(amount.toString());
        switch (currencyType) {
            case XTOType:
                return xto.multiply(new BigInteger(XTO));
            case KXTOType:
                return xto.multiply(new BigInteger(KXTO));
            case MXTOType:
                return xto.multiply(new BigInteger(MXTO));
            case GXTOType:
                return xto.multiply(new BigInteger(GXTO));
            case MICROCoinType:
                return xto.multiply(new BigInteger(MICROCoin));
            case MILLICoinType:
                return xto.multiply(new BigInteger(MILLICoin));
            case CoinType:
                return xto.multiply(new BigInteger(Coin));
        }

        return null;
    }

    public static BigInteger generateCurrencyUnitToCurrencyUnit(CurrencyType sourceType, CurrencyType targetType, BigInteger sourceAmount) {
        BigInteger defaultAmount = generateXTO(sourceType, sourceAmount);
        BigInteger divValue = new BigInteger("1");

        switch (targetType) {
            case XTOType:
                divValue = divValue.multiply(new BigInteger(XTO));
                defaultAmount = defaultAmount.divide(divValue);
                break;
            case KXTOType:
                divValue = divValue.multiply(new BigInteger(KXTO));
                defaultAmount = defaultAmount.divide(divValue);
                break;
            case MXTOType:
                divValue = divValue.multiply(new BigInteger(MXTO));
                defaultAmount = defaultAmount.divide(divValue);
                break;
            case GXTOType:
                divValue = divValue.multiply(new BigInteger(GXTO));
                defaultAmount = defaultAmount.divide(divValue);
                break;
            case MICROCoinType:
                divValue = divValue.multiply(new BigInteger(MICROCoin));
                defaultAmount = defaultAmount.divide(divValue);
                break;
            case MILLICoinType:
                divValue = divValue.multiply(new BigInteger(MILLICoin));
                defaultAmount = defaultAmount.divide(divValue);
                break;
            case CoinType:
                divValue = divValue.multiply(new BigInteger(Coin));
                defaultAmount = defaultAmount.divide(divValue);
                break;
        }
        return defaultAmount;
    }

    public static String generateStringCurrencyUnitToCurrencyUnit(CurrencyType sourceType, CurrencyType targetType, BigInteger sourceAmount) {
        BigInteger defaultAmount = generateXTO(sourceType, sourceAmount);

        String targetAmount = "";
        switch (targetType) {
            case XTOType:
                targetAmount = StringUtil.convertStringFloatingPoint(defaultAmount.toString(), 0);
                break;
            case KXTOType:
                targetAmount = StringUtil.convertStringFloatingPoint(defaultAmount.toString(), 3);
                break;
            case MXTOType:
                targetAmount = StringUtil.convertStringFloatingPoint(defaultAmount.toString(), 6);
                break;
            case GXTOType:
                targetAmount = StringUtil.convertStringFloatingPoint(defaultAmount.toString(), 9);
                break;
            case MICROCoinType:
                targetAmount = StringUtil.convertStringFloatingPoint(defaultAmount.toString(), 12);
                break;
            case MILLICoinType:
                targetAmount = StringUtil.convertStringFloatingPoint(defaultAmount.toString(), 15);
                break;
            case CoinType:
                targetAmount = StringUtil.convertStringFloatingPoint(defaultAmount.toString(), 18);
                break;
        }

        return targetAmount;
    }
}