package com.xbctechnologies.core.utils;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

public class Base64Util {
    private static final Logger logger = LoggerFactory.getLogger(Base64Util.class);

    public static String encode(String val) {
        return new String(Base64.encodeBase64(val.getBytes()));
    }

    public static String encode(byte[] val) {
        return new String(Base64.encodeBase64(val));
    }

    public static String encode(File file) {
        try {
            return new String(Base64.encodeBase64(FileUtils.readFileToByteArray(file)));
        } catch (Exception e) {
            logger.error(e.getMessage());
            return null;
        }
    }

    public static String decodeStr(String val) {
        byte[] valueDecoded = Base64.decodeBase64(val);
        return new String(valueDecoded);
    }

    public static byte[] decode(String val) {
        byte[] valueDecoded = Base64.decodeBase64(val);
        return valueDecoded;
    }
}
