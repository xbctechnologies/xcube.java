package com.xbctechnologies.core.utils;

import com.xbctechnologies.xcrypto.api.XbHashUtil;

import java.security.NoSuchAlgorithmException;

public class CryptoUtil {
    private static final String ENCODING = "UTF-8";

    public static byte[] sha256(String msg) {
        try {
            return sha256(msg.getBytes(ENCODING));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static byte[] sha256(byte[] msg) {
        byte[] result = null;
        XbHashUtil xbHashUtil = null;
        try {
            xbHashUtil = new XbHashUtil("sha256");
            xbHashUtil.update(msg);
            result = xbHashUtil.getHash();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
        return result;
    }

    public static byte[] keccak256(String msg) {
        try {
            return keccak256(msg.getBytes(ENCODING));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static byte[] keccak256(byte[] msg) {
        byte[] result = null;
        XbHashUtil xbHashUtil = null;
        try {
            xbHashUtil = new XbHashUtil("keccak256");
            xbHashUtil.update(msg);
            result = xbHashUtil.getHash();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
        return result;
    }
}
