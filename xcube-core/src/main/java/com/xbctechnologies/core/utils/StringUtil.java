package com.xbctechnologies.core.utils;

public class StringUtil {
    public static String convertStringFloatingPoint(String val, int point) {
        String returnVal;
        if (point == 0) {
            return val;
        }
        if (val.length() <= point) {
            int remain = point - val.length();
            returnVal = "0.";
            for (int i = 0; i < remain; i++) {
                returnVal += "0";
            }
            returnVal += val;
        } else {
            returnVal = val.replaceAll("(\\d{3})$", ".$1");
        }
        returnVal = returnVal.replaceAll("(0*)$", "");
        returnVal = returnVal.replaceAll("\\.$", "");
        return returnVal;
    }
}
