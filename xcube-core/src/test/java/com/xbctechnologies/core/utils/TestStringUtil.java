package com.xbctechnologies.core.utils;

import org.junit.Test;

import java.math.BigInteger;

public class TestStringUtil {
    @Test
    public void convertStringFloatingPoint() {
        BigInteger val = CurrencyUtil.generateXTO(CurrencyUtil.CurrencyType.XTOType, 1);

        String xto = CurrencyUtil.generateStringCurrencyUnitToCurrencyUnit(CurrencyUtil.CurrencyType.XTOType, CurrencyUtil.CurrencyType.XTOType, val);
        System.out.println(xto);

        xto = CurrencyUtil.generateStringCurrencyUnitToCurrencyUnit(CurrencyUtil.CurrencyType.XTOType, CurrencyUtil.CurrencyType.KXTOType, val);
        System.out.println(xto);

        xto = CurrencyUtil.generateStringCurrencyUnitToCurrencyUnit(CurrencyUtil.CurrencyType.XTOType, CurrencyUtil.CurrencyType.MXTOType, val);
        System.out.println(xto);

        xto = CurrencyUtil.generateStringCurrencyUnitToCurrencyUnit(CurrencyUtil.CurrencyType.XTOType, CurrencyUtil.CurrencyType.GXTOType, val);
        System.out.println(xto);

        xto = CurrencyUtil.generateStringCurrencyUnitToCurrencyUnit(CurrencyUtil.CurrencyType.XTOType, CurrencyUtil.CurrencyType.MICROCoinType, val);
        System.out.println(xto);

        xto = CurrencyUtil.generateStringCurrencyUnitToCurrencyUnit(CurrencyUtil.CurrencyType.XTOType, CurrencyUtil.CurrencyType.MILLICoinType, val);
        System.out.println(xto);

        xto = CurrencyUtil.generateStringCurrencyUnitToCurrencyUnit(CurrencyUtil.CurrencyType.XTOType, CurrencyUtil.CurrencyType.CoinType, val);
        System.out.println(xto);
    }
}
