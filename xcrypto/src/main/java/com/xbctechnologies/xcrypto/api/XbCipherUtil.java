package com.xbctechnologies.xcrypto.api;

import com.xbctechnologies.xcrypto.ec.ECKey;
import org.spongycastle.crypto.BufferedBlockCipher;
import org.spongycastle.crypto.InvalidCipherTextException;
import org.spongycastle.crypto.engines.AESEngine;
import org.spongycastle.crypto.generators.SCrypt;
import org.spongycastle.crypto.modes.CBCBlockCipher;
import org.spongycastle.crypto.modes.SICBlockCipher;
import org.spongycastle.crypto.paddings.BlockCipherPadding;
import org.spongycastle.crypto.paddings.PKCS7Padding;
import org.spongycastle.crypto.paddings.PaddedBufferedBlockCipher;
import org.spongycastle.crypto.params.KeyParameter;
import org.spongycastle.crypto.params.ParametersWithIV;
import org.spongycastle.util.encoders.Hex;

import java.io.UnsupportedEncodingException;
import java.security.KeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.UUID;

public class XbCipherUtil {

    public XbCipherUtil() {

    }

    // Symmetric key, iv Generate Method
    public byte[] keyGen(int size) {
        // 1. new byte[]
        byte[] rand = new byte[size];
        // 2. new SecureRandom
        SecureRandom sR = new SecureRandom();
        // 3. Secure Pseudo-Random Number Generate
        sR.nextBytes(rand);
        // 4. return Symmetric key or iv
        return rand;
    }

    // AES/CTR/PKCS7Padding(PKCS5Padding) Encryption Method
    public byte[] aesCTRPKCS7PaddingEncryption(byte[] keyBytes, byte[] msg) throws InvalidCipherTextException {
        // 1. New KeyParameter( Symmetric key )
        KeyParameter key = new KeyParameter(keyBytes);
        // 2. New ParametersWithIV ( KeyParameter, empty iv byte[] )
        ParametersWithIV params = new ParametersWithIV(key, new byte[16]);
        // 3. New AESEngine and New SICBlockCipher
        AESEngine engine = new AESEngine();
        SICBlockCipher ctrEngine = new SICBlockCipher(engine);

        // 4. PKCS7Padding Setting
        BlockCipherPadding padding = new PKCS7Padding();
        BufferedBlockCipher cipher = new PaddedBufferedBlockCipher(ctrEngine, padding);
        //BufferedBlockCipher cipher = new BufferedBlockCipher(ctrEngine);
        // 5. params init and encryption mode
        cipher.init(true, params);
        // 6. ciphertext new byte[]
        byte[] ct = new byte[cipher.getOutputSize(msg.length)];
        // 7. Encryption
        int len = cipher.processBytes(msg, 0, msg.length, ct, 0);
        len += cipher.doFinal(ct, len);
        // 8. return cipher text
        return ct;
    }

    // AES/CTR/PKCS7Padding(PKCS5Padding) Decryptio Method
    public byte[] aesCTRPKCS7PaddingDecryption(byte[] keyBytes, byte[] enc) throws InvalidCipherTextException {
        // 1. New AESEngine and New SICBlockCipher
        AESEngine engine = new AESEngine();
        SICBlockCipher ctrEngine = new SICBlockCipher(engine);
        // 2. New KeyParameter( Symmetric key )
        KeyParameter key = new KeyParameter(keyBytes);
        // 3. New ParametersWithIV ( KeyParameter, empty iv byte[] )
        ParametersWithIV params = new ParametersWithIV(key, new byte[16]);
        // 4. PKCS7Padding Setting
        BlockCipherPadding padding = new PKCS7Padding();
        BufferedBlockCipher cipher = new PaddedBufferedBlockCipher(ctrEngine, padding);
        //BufferedBlockCipher cipher = new BufferedBlockCipher(ctrEngine);
        // 5. params init and decryption mode
        cipher.init(false, params);

        // 6. plaintext new byte[]
        byte[] pt = new byte[cipher.getOutputSize(enc.length)];
        // 7. Decryption
        int len = cipher.processBytes(enc, 0, enc.length, pt, 0);
        len += cipher.doFinal(pt, len);
        // 8. return plain text
        return pt;
    }

    // AES/CBC/PKCS7Padding(PKCS5Padding) Encryption Method
    public byte[] aesCBCPKCS7PaddingEncryption(byte[] keyBytes, byte[] ivBytes, byte[] msg) throws InvalidCipherTextException {
        // 1. New KeyParameter( Symmetric key )
        KeyParameter key = new KeyParameter(keyBytes);
        // 2. New ParametersWithIV ( KeyParameter, iv )
        ParametersWithIV params = new ParametersWithIV(key, ivBytes);
        // 3. New AESEngine and New CBCBlockCipher
        AESEngine engine = new AESEngine();
        CBCBlockCipher cbcEngine = new CBCBlockCipher(engine);

        // 4. PKCS7Padding Setting
        BlockCipherPadding padding = new PKCS7Padding();
        BufferedBlockCipher cipher = new PaddedBufferedBlockCipher(cbcEngine, padding);
        //BufferedBlockCipher cipher = new BufferedBlockCipher(cbcEngine);
        // 5. params init and encryption mode
        cipher.init(true, params);

        // 6. ciphertext new byte[]
        byte[] ct = new byte[cipher.getOutputSize(msg.length)];
        // 7. Encryption
        int len = cipher.processBytes(msg, 0, msg.length, ct, 0);
        len += cipher.doFinal(ct, len);
        // 8. return plain text
        return ct;
    }

    // AES/CBC/PKCS7Padding(PKCS5Padding) Decryption Method
    public byte[] aesCBCPKCS7PaddingDecryption(byte[] keyBytes, byte[] ivBytes, byte[] enc) throws InvalidCipherTextException {
        // 1. New AESEngine and New CBCBlockCipher
        AESEngine engine = new AESEngine();
        CBCBlockCipher cbcEngine = new CBCBlockCipher(engine);
        // 2. New KeyParameter( Symmetric key )
        KeyParameter key = new KeyParameter(keyBytes);
        // 3. New ParametersWithIV ( KeyParameter, iv )
        ParametersWithIV params = new ParametersWithIV(key, ivBytes);

        // 4. PKCS7Padding Setting
        BlockCipherPadding padding = new PKCS7Padding();
        BufferedBlockCipher cipher = new PaddedBufferedBlockCipher(cbcEngine, padding);
        //BufferedBlockCipher cipher = new BufferedBlockCipher(cbcEngine);
        // 5. params init and decryption mode
        cipher.init(false, params);

        // 6. plaintext new byte[]
        byte[] pt = new byte[cipher.getOutputSize(enc.length)];
        // 7. Decryption
        int len = cipher.processBytes(enc, 0, enc.length, pt, 0);
        len += cipher.doFinal(pt, len);
        // 8. return plain text
        return pt;
    }

    // keystore key pbkdf Decryption Method
    public String pbkdfDecyrptKey(byte[] P, byte[] S, int N, int r, int p, int dkLen, String ciphertext, String macstr, String iv) throws NoSuchAlgorithmException, KeyException, InvalidCipherTextException {
        // Key derives a key from the password, salt, and cost parameters, returning
        // a byte slice of length keyLen that can be used as cryptographic key.
        // 1. derivedKey create
        byte[] derivedKey = SCrypt.generate(P, S, N, r, p, dkLen);
        int keySize = 16;
        // 2. mac byte copy
        byte[] mac = new byte[keySize];
        System.arraycopy(derivedKey, 16, mac, 0, mac.length);
        // 2. AES key copy
        byte[] encryptKey = new byte[keySize];
        System.arraycopy(derivedKey, 0, encryptKey, 0, encryptKey.length);

        // 3. mac verify
        String algorithm = "keccak256";
        XbHashUtil xbHashUtil = null;
        try {
            xbHashUtil = new XbHashUtil(algorithm);
        } catch (NoSuchAlgorithmException e) {
            throw e;
        }
        xbHashUtil.update(mac);
        xbHashUtil.update(Hex.decode(ciphertext));
        byte[] calculatedMAC = xbHashUtil.getHash();
        if (!Arrays.equals(calculatedMAC, Hex.decode(macstr))) {
            throw new KeyException("The mac value is incorrect.(password check.)");
        }
        byte[] enc = Hex.decode(ciphertext);

        // 4. AES/CTR algorithm Decryption -> ECC private Key Get
        AESEngine engine = new AESEngine();
        SICBlockCipher ctrEngine = new SICBlockCipher(engine);
        // New KeyParameter( Symmetric key )
        KeyParameter keyparam = new KeyParameter(encryptKey);
        ParametersWithIV params = new ParametersWithIV(keyparam, Hex.decode(iv));
        BufferedBlockCipher cipher = new BufferedBlockCipher(ctrEngine);
        // params init and decryption mode
        cipher.init(false, params);
        // ECC private Key new byte[]
        byte[] rawPriKeyBytes = new byte[cipher.getOutputSize(enc.length)];
        // Decryption
        int len = cipher.processBytes(enc, 0, enc.length, rawPriKeyBytes, 0);
        len += cipher.doFinal(rawPriKeyBytes, len);
        // 5. return raw ECC private Key (hex format)
        return Hex.toHexString(rawPriKeyBytes);
    }

    // keystore new Account -> key file save
    public String[] newAccountJson(String passphrase) {
        String encoding = "UTF-8";

        // 1. ECC Key Generate
        XbSignUtil xbSignUtil = new XbSignUtil();
        ECKey ecckey = xbSignUtil.ecKeyGen();

        // 2. salt Generate
        XbCipherUtil xbCipherUtil = new XbCipherUtil();
        byte[] salt = xbCipherUtil.keyGen(32);

        int version = 1;
        int N = 1 << 18;
        int r = 9;
        int p = 1;
        int dkLen = 32;
        // 3. derivedKey create
        byte[] derivedKey = null;
        try {
            derivedKey = SCrypt.generate(passphrase.getBytes(encoding), salt, N, r, p, dkLen);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return null;
        }
        // 4. AES key copy
        byte[] encryptKey = new byte[16];
        System.arraycopy(derivedKey, 0, encryptKey, 0, 16);
        // 5. ECC private Key Get
        byte[] keyBytes = ecckey.getPrivKeyBytes();
        // 6. AES iv Generate
        int aesBlockSize = 16;
        byte[] iv = xbCipherUtil.keyGen(aesBlockSize);

        // 7. AES/CTR Encryption -> enc( ECC private Key )
        AESEngine engine = new AESEngine();
        SICBlockCipher ctrEngine = new SICBlockCipher(engine);
        // New KeyParameter( Symmetric key )
        KeyParameter keyparam = new KeyParameter(encryptKey);
        ParametersWithIV params = new ParametersWithIV(keyparam, iv);
        BufferedBlockCipher cipher = new BufferedBlockCipher(ctrEngine);
        // params init and encryption mode
        cipher.init(true, params);

        // 8. ECC private Key new byte[]
        byte[] cipherText = new byte[cipher.getOutputSize(keyBytes.length)];
        // 9. Encryption
        int len = cipher.processBytes(keyBytes, 0, keyBytes.length, cipherText, 0);
        try {
            len += cipher.doFinal(cipherText, len);
        } catch (InvalidCipherTextException e) {
            e.printStackTrace();
        }
        // 10. mackey new byte[]
        byte[] macKey = new byte[16];
        System.arraycopy(derivedKey, 16, macKey, 0, 16);
        XbHashUtil xbHashUtil = null;
        String algorithm = "keccak256";
        try {
            xbHashUtil = new XbHashUtil(algorithm);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        xbHashUtil.update(macKey);
        xbHashUtil.update(cipherText);
        // 11. mac create
        byte[] mac = xbHashUtil.getHash();
        // 12. UUID create
        UUID uuid = UUID.randomUUID();
        // 13. return encrypted Key Json, Address
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        sb.append("\"address\":\"" + Hex.toHexString(ecckey.getAddress()) + "\",");
        sb.append("\"crypto\":{");
        sb.append("\"cipher\":\"aes-128-ctr\",");
        sb.append("\"ciphertext\":\"" + Hex.toHexString(cipherText) + "\",");
        sb.append("\"cipherparams\":{");
        sb.append("\"iv\":\"" + Hex.toHexString(iv) + "\"");
        sb.append("},");
        sb.append("\"kdf\":\"scrypt\",");
        sb.append("\"n\":" + N + ",");
        sb.append("\"r\":" + r + ",");
        sb.append("\"p\":" + p + ",");
        sb.append("\"dklen\":" + dkLen + ",");
        sb.append("\"c\":0,");
        sb.append("\"prf\":\"\",");
        sb.append("\"salt\":\"" + Hex.toHexString(salt) + "\",");
        sb.append("\"mac\":\"" + Hex.toHexString(mac) + "\"");
        sb.append("},");
        sb.append("\"id\":\"" + uuid.toString() + "\",");
        sb.append("\"version\":" + version);
        sb.append("}");

        String[] result = new String[2];
        result[0] = sb.toString();
        result[1] = Hex.toHexString(ecckey.getAddress());
        return result;
    }
}
