package com.xbctechnologies.core.utils;

import java.util.concurrent.TimeUnit;

public class DateUtil {
    public static long getMicroSecond() {
        //Last three digit is dirty data (*= 1000)
        return TimeUnit.MILLISECONDS.toMicros(System.currentTimeMillis());
    }
}
