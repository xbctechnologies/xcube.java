package com.xbctechnologies.core.utils;

import java.math.BigInteger;
import java.text.NumberFormat;
import java.util.Locale;

public class NumberUtil {
    public static String comma(BigInteger val) {
        if (val == null) {
            return "";
        }
        return NumberFormat.getNumberInstance(Locale.US).format(val);
    }

    public static BigInteger generateStringToBigInteger(String val) {
        return new BigInteger(val.replaceAll(",", ""));
    }
}
