package com.xbctechnologies.xcrypto.ec.test;

import com.xbctechnologies.xcrypto.api.XbCipherUtil;
import com.xbctechnologies.xcrypto.util.FileUtil;
import org.junit.Test;
import org.spongycastle.crypto.InvalidCipherTextException;
import org.spongycastle.util.encoders.Base64;
import org.spongycastle.util.encoders.Hex;

import java.io.UnsupportedEncodingException;

public class TestCipher {

    @Test
    public void keyGen() {
        int size = 16;
        XbCipherUtil xbCipherUtil = new XbCipherUtil();

        for (int i = 0; i < 10; i++) {
            byte[] keybytes = xbCipherUtil.keyGen(size);
            System.out.println(Hex.toHexString(keybytes));
        }
    }

    @Test
    public void aesCTREncAndDec() throws UnsupportedEncodingException {
        String msg = "hello world!네트웍";
        String encoding = "UTF-8";
        byte[] mb = msg.getBytes(encoding);

        int keysize = 16;
        XbCipherUtil xbCipherUtil = new XbCipherUtil();
        byte[] keyBytes = xbCipherUtil.keyGen(keysize);

        byte[] ciphertext = null;
        try {
            ciphertext = xbCipherUtil.aesCTRPKCS7PaddingEncryption(keyBytes, mb);
        } catch (InvalidCipherTextException e) {
            System.out.println(e.toString());
            return;
        }
        System.out.println("AES/CTR/PKCS7Padding ciphertext base64: " + Base64.toBase64String(ciphertext));

        byte[] dectext = null;
        try {
            dectext = xbCipherUtil.aesCTRPKCS7PaddingDecryption(keyBytes, ciphertext);
        } catch (InvalidCipherTextException e) {
            System.out.println(e.toString());
            return;
        }
        System.out.println("AES/CTR/PKCS7Padding dectext string: " + new String(dectext, encoding));
    }

    @Test
    public void aesCBCEncAndDec() throws UnsupportedEncodingException {
        String msg = "hello world!네트웍";
        String encoding = "UTF-8";
        byte[] mb = msg.getBytes(encoding);

        int keysize = 16;
        int ivsize = 16;
        XbCipherUtil xbCipherUtil = new XbCipherUtil();
        byte[] keyBytes = xbCipherUtil.keyGen(keysize);
        byte[] ivBytes = xbCipherUtil.keyGen(ivsize);

        byte[] ciphertext = null;
        try {
            ciphertext = xbCipherUtil.aesCBCPKCS7PaddingEncryption(keyBytes, ivBytes, mb);
        } catch (InvalidCipherTextException e) {
            System.out.println(e.toString());
            return;
        }
        System.out.println("AES/CBC/PKCS7Padding ciphertext base64: " + Base64.toBase64String(ciphertext));

        byte[] dectext = null;
        try {
            dectext = xbCipherUtil.aesCBCPKCS7PaddingDecryption(keyBytes, ivBytes, ciphertext);
        } catch (InvalidCipherTextException e) {
            System.out.println(e.toString());
            return;
        }
        System.out.println("AES/CBC/PKCS7Padding dectext string: " + new String(dectext, encoding));
    }

    @Test
    public void encryptKeyJson() throws IllegalStateException {
        String password = "1111";
        XbCipherUtil xbCipherUtil = new XbCipherUtil();
        String[] result = xbCipherUtil.newAccountJson(password);
        String path = "/tmp";
        FileUtil.writeEncKeyJsonFile(path, result);
    }
}
