package com.xbctechnologies.xcrypto.api;

import com.xbctechnologies.xcrypto.ec.ECKey;

import java.math.BigInteger;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;

public class XbSignUtil {

    public XbSignUtil() {

    }

    // ECC Key Generate Method
    public ECKey ecKeyGen() {
        // 1. ecc Key Generate
        ECKey ecKey = new ECKey();
        // 2. return ECKey ( include Private Key, Public Key ) Object
        return ecKey;
    }

    // ECDSA/SHA256 Sign Method
    public ECKey.ECDSASignature sign(String priKeyHexStr, byte[] msg) throws NoSuchAlgorithmException, SignatureException {
        if (msg == null) {
            throw new SignatureException("Please enter the original text to be digitally signed.");
        }

        // 1. Private Key Hex -> ECKey Object Convert
        BigInteger privateKey = new BigInteger(priKeyHexStr, 16);
        ECKey key = ECKey.fromPrivate(privateKey);

        // 2. SHA256 Hash(msg)
        String algorithm = "sha256";
        XbHashUtil xbHashUtil = new XbHashUtil(algorithm);
        xbHashUtil.update(msg);
        byte[] hash = xbHashUtil.getHash();
        // 3. ECDSA Sign( hash256(msg) )
        ECKey.ECDSASignature signature = key.sign(hash);
        return signature;
    }

    // ECDSA/SHA256 Verify Method
    public boolean verify(ECKey.ECDSASignature signature, byte[] msg) throws NoSuchAlgorithmException {
        // 1. SHA256 Hash(msg)
        String algorithm = "sha256";
        XbHashUtil xbHashUtil = new XbHashUtil(algorithm);
        xbHashUtil.update(msg);
        byte[] hash = xbHashUtil.getHash();

        // 2. ECC Public Key Recovery
        ECKey key = null;
        try {
            key = ECKey.signatureToKey(hash, signature.toBase64());
        } catch (SignatureException e) {
            return false;
        }

        // 3. ECDSA Verify( hash256(msg) )
        return key.verify(hash, signature, false);
    }

    public ECKey getRecoveryKey(ECKey.ECDSASignature signature, byte[] msg) throws NoSuchAlgorithmException, SignatureException {
        // 1. SHA256 Hash(msg)
        String algorithm = "sha256";
        XbHashUtil xbHashUtil = new XbHashUtil(algorithm);
        xbHashUtil.update(msg);
        byte[] hash = xbHashUtil.getHash();

        // 2. ECC Public Key Recovery
        ECKey key = null;
        try {
            key = ECKey.signatureToKey(hash, signature.toBase64());
        } catch (SignatureException e) {
            throw e;
        }
        return key;
    }
}
