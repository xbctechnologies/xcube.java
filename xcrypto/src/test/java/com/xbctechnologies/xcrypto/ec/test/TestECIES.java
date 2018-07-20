package com.xbctechnologies.xcrypto.ec.test;

import com.xbctechnologies.xcrypto.api.XbECIESUtil;
import com.xbctechnologies.xcrypto.api.XbSignUtil;
import com.xbctechnologies.xcrypto.ec.ECKey;
import org.junit.Test;
import org.spongycastle.crypto.InvalidCipherTextException;
import org.spongycastle.util.encoders.Base64;

import java.io.IOException;

public class TestECIES {

    @Test
    public void eciesEncAndDec() throws IOException, InvalidCipherTextException {
        String plaintext = "hello world!네트웍";
        String encoding = "UTF-8";
        byte[] pb = plaintext.getBytes(encoding);

        // 1. ECC Key generate
        XbSignUtil xbSignUtil = new XbSignUtil();
        ECKey key = xbSignUtil.ecKeyGen();
        // 2. ECIES Encryption
        byte[] encbytes = XbECIESUtil.encryptECIES(key.getPubKey(false), pb);
        System.out.println("enc base64: " + Base64.toBase64String(encbytes));
        // 3. ECIES Decryption
        byte[] decbytes = XbECIESUtil.decryptECIES(key.getPrivKey(), encbytes);
        System.out.println("dec String: " + new String(decbytes, encoding));
    }
}
