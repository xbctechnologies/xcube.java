package com.xbctechnologies.xcrypto.ec.test;

import com.xbctechnologies.xcrypto.api.XbHashUtil;
import org.junit.Test;
import org.spongycastle.util.encoders.Hex;

import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;

public class TestHash {

    /*static {
        Security.insertProviderAt(new org.spongycastle.jce.provider.BouncyCastleProvider(), 1);
    }*/

    @Test
    public void keccak256() throws UnsupportedEncodingException {
        String algorithm = "keccak256";
        String msgstr = "keccak256 테스트";
        String encoding = "UTF-8";

        XbHashUtil xbHashUtil = null;
        try {
            xbHashUtil = new XbHashUtil(algorithm);
        } catch (NoSuchAlgorithmException e) {
            System.out.println(e.toString());
            return;
        }
        xbHashUtil.update(msgstr.getBytes(encoding));
        byte[] hash = xbHashUtil.getHash();

        System.out.println("Keccak256 Hash hex: " + Hex.toHexString(hash));
        System.out.println("Keccak256 Hash byte len: " + hash.length);
    }

    @Test
    public void sha256() throws UnsupportedEncodingException {
        String algorithm = "sha256";
        String msgstr = "Hello world!";
        String encoding = "UTF-8";
        byte[] mb = msgstr.getBytes(encoding);

        XbHashUtil xbHashUtil = null;
        try {
            xbHashUtil = new XbHashUtil(algorithm);
        } catch (NoSuchAlgorithmException e) {
            System.out.println(e.toString());
            return;
        }
        xbHashUtil.update(msgstr.getBytes(encoding));
        byte[] hash = xbHashUtil.getHash();

        System.out.println("Sha256 Hash hex: " + Hex.toHexString(hash));
        System.out.println("Sha256 Hash byte len: " + hash.length);
    }
}
