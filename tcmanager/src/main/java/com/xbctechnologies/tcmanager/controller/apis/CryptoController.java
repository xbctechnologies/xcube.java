package com.xbctechnologies.tcmanager.controller.apis;

import com.xbctechnologies.xcrypto.api.XbCipherUtil;
import com.xbctechnologies.xcrypto.api.XbECIESUtil;
import com.xbctechnologies.xcrypto.api.XbHashUtil;
import com.xbctechnologies.xcrypto.api.XbSignUtil;
import com.xbctechnologies.xcrypto.ec.ECKey;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.spongycastle.crypto.InvalidCipherTextException;
import org.spongycastle.util.encoders.DecoderException;
import org.spongycastle.util.encoders.Hex;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;

@Api(tags = {"J. Crypto API Test"}, description = "Crypto test")
@RestController
public class CryptoController {

    public byte[] getHash(String algorithm, byte[] mb) throws NoSuchAlgorithmException {
        if (!algorithm.equals("keccak256") && !algorithm.equals("sha256")) {
            throw new NoSuchAlgorithmException("The algorithm is not supported.[" + algorithm + "]");
        }
        XbHashUtil xbHashUtil = null;
        try {
            xbHashUtil = new XbHashUtil(algorithm);
        } catch (NoSuchAlgorithmException e) {
            throw e;
        }
        xbHashUtil.update(mb);
        byte[] hash = xbHashUtil.getHash();
        return hash;
    }

    @ApiOperation(value = "Keccak256 Hash")
    @RequestMapping(value = "/getKeccak256", method = {RequestMethod.GET})
    @ResponseBody
    // msg -> Keccak256 Hash
    public String getKeccak256(@RequestParam(value = "message", required = true) String message)
            throws NoSuchAlgorithmException, UnsupportedEncodingException {
        String algorithm = "keccak256";
        String encoding = "UTF-8";
        byte[] mb = null;
        try {
            mb = message.getBytes(encoding);
        } catch (UnsupportedEncodingException e) {
            throw e;
        }
        byte[] hb = getHash(algorithm, mb);
        return "Keccak256 Hash Hex: " + Hex.toHexString(hb);
    }

    @ApiOperation(value = "Message Hex decode -> Keccak256 Hash")
    @RequestMapping(value = "/getHexMsgKeccak256", method = {RequestMethod.GET})
    @ResponseBody
    // msg hex decode -> Keccak256 Hash
    public String getHexMsgKeccak256(@RequestParam(value = "hexmessage", required = true) String hexmessage)
            throws IllegalArgumentException, NoSuchAlgorithmException {
        String algorithm = "keccak256";
        byte[] mb = null;
        try {
            mb = Hex.decode(hexmessage);
        } catch (DecoderException e) {
            throw new IllegalArgumentException("This is not a hex message format. " + e.toString());
        }
        byte[] hb = getHash(algorithm, mb);
        return "Keccak256 Hash Hex: " + Hex.toHexString(hb);
    }

    @ApiOperation(value = "SHA256 Hash")
    @RequestMapping(value = "/getSha256", method = {RequestMethod.GET})
    @ResponseBody
    // msg -> SHA256 Hash
    public String getSha256(@RequestParam(value = "message", required = true) String message)
            throws NoSuchAlgorithmException, UnsupportedEncodingException {
        String algorithm = "sha256";
        String encoding = "UTF-8";
        byte[] mb = null;
        try {
            mb = message.getBytes(encoding);
        } catch (UnsupportedEncodingException e) {
            throw e;
        }
        byte[] hb = getHash(algorithm, mb);
        return "SHA256 Hash Hex: " + Hex.toHexString(hb);
    }

    @ApiOperation(value = "Message Hex decode -> SHA256 Hash")
    @RequestMapping(value = "/getHexMsgSHA256", method = {RequestMethod.GET})
    @ResponseBody
    // msg hex decode -> SHA256 Hash
    public String getHexMsgSHA256(@RequestParam(value = "hexmessage", required = true) String hexmessage)
            throws IllegalArgumentException, NoSuchAlgorithmException {
        String algorithm = "sha256";
        byte[] mb = null;
        try {
            mb = Hex.decode(hexmessage);
        } catch (DecoderException e) {
            throw new IllegalArgumentException("This is not a hex message format. " + e.toString());
        }
        byte[] hb = getHash(algorithm, mb);
        return "SHA256 Hash Hex: " + Hex.toHexString(hb);
    }

    @ApiOperation(value = "ECC KeyPair Generate")
    @RequestMapping(value = "/eccSecp256k1GenKeyPair", method = {RequestMethod.GET})
    @ResponseBody
    // ECC KeyPair Generate
    public String[] eccSecp256k1GenKeyPair() {
        String[] keylist = new String[2];
        XbSignUtil xbSignUtil = new XbSignUtil();
        ECKey key = xbSignUtil.ecKeyGen();

        keylist[0] = "priKey hex: " + Hex.toHexString(key.getPrivKeyBytes());
        keylist[1] = "pubKey hex: " + Hex.toHexString(key.getPubKey(true));
        return keylist;
    }

    @ApiOperation(value = "Symmetric Key Generate")
    @RequestMapping(value = "/keyGen", method = {RequestMethod.GET})
    @ResponseBody
    // Symmetric Key(Aes, SEED, ARIA ... algorithm key), Iv (initial vector)
    public String keyGen(@RequestParam(value = "keysize", required = true) int keysize)
            throws IllegalArgumentException {
        if (keysize <= 7) {
            throw new IllegalArgumentException("Set keysize to greater than 7. input keysize[" + keysize + "]");
        }
        XbCipherUtil xbCipherUtil = new XbCipherUtil();
        byte[] keybytes = xbCipherUtil.keyGen(keysize);
        return "key or iv hex: " + Hex.toHexString(keybytes);
    }

    @ApiOperation(value = "Account Address Generate")
    @RequestMapping(value = "/accountAddress", method = {RequestMethod.GET})
    @ResponseBody
    // public key -> Account Address Generate
    public String accountAddress(@RequestParam(value = "keyhexmessage", required = true) String keyhexmessage,
                                 @RequestParam(value = "isPublicKey", required = true) boolean isPublicKey)
            throws IllegalArgumentException {
        byte[] kb = null;
        try {
            kb = Hex.decode(keyhexmessage);
        } catch (DecoderException e) {
            throw new IllegalArgumentException("This is not a hex message format. " + e.toString());
        }
        ECKey key = null;
        if (isPublicKey) {
            key = ECKey.fromPublicOnly(kb);

        } else {
            key = ECKey.fromPrivate(kb);
        }
        return "Address Hex: " + Hex.toHexString(key.getAddress());
    }

    @ApiOperation(value = "ECDSA/SHA256 Sign and Verify")
    @RequestMapping(value = "/ecdsaSha256SignAndVerify", method = {RequestMethod.GET})
    @ResponseBody
    // ECDSA/SHA256 Sign -> ECDSA/SHA256 Verify
    public String[] ecdsaSha256SignAndVerify(@RequestParam(value = "message", required = true) String message,
                                             @RequestParam(value = "isHexMessage", required = true) boolean isHexMessage,
                                             @RequestParam(value = "priKeyhexmessage", required = true) String priKeyhexmessage)
            throws NoSuchAlgorithmException, SignatureException, UnsupportedEncodingException, IllegalArgumentException {
        String[] resultlist = new String[6];
        byte[] mb = null;
        try {
            if (isHexMessage) {
                mb = Hex.decode(message);
            } else {
                mb = message.getBytes("UTF-8");
            }
        } catch (DecoderException e) {
            throw new IllegalArgumentException("This is not a hex message format. " + e.toString());
        }

        byte[] kb = null;
        try {
            kb = Hex.decode(priKeyhexmessage);
        } catch (DecoderException e) {
            throw new IllegalArgumentException("This is not a hex message format. " + e.toString());
        }
        ECKey key = ECKey.fromPrivate(kb);
        XbSignUtil xbSignUtil = new XbSignUtil();
        ECKey.ECDSASignature signature = null;
        try {
            signature = xbSignUtil.sign(key.getPrivKeyHexString(), mb);
        } catch (NoSuchAlgorithmException e) {
            throw e;
        } catch (SignatureException e) {
            throw e;
        }

        ECKey.ECDSASignature verifysignature = ECKey.ECDSASignature.fromComponents(signature.r.toByteArray(), signature.s.toByteArray(), signature.v);
        boolean isok = false;
        try {
            isok = xbSignUtil.verify(verifysignature, mb);
        } catch (NoSuchAlgorithmException e) {
            throw e;
        }
        resultlist[0] = "Sign Hex string: " + signature.toHex();
        resultlist[1] = "R Hex string: " + signature.getRHexString();
        resultlist[2] = "S Hex string: " + signature.getSHexString();
        resultlist[3] = "V int: " + signature.getVInt();
        resultlist[4] = "Account Address: " + Hex.toHexString(key.getAddress());
        resultlist[5] = "Verify result: " + isok;
        return resultlist;
    }

    @ApiOperation(value = "Recovery Key")
    @RequestMapping(value = "/getRecoveryKey", method = {RequestMethod.GET})
    @ResponseBody
    // R, S, V -> Recovery Key -> Account Addres same check.
    public String[] getRecoveryKey(@RequestParam(value = "message", required = true) String message,
                                   @RequestParam(value = "isHexMessage", required = true) boolean isHexMessage,
                                   @RequestParam(value = "Rhex", required = true) String Rhex,
                                   @RequestParam(value = "Shex", required = true) String Shex,
                                   @RequestParam(value = "V", required = true) int V,
                                   @RequestParam(value = "AccountAddress", required = true) String AccountAddress)
            throws NoSuchAlgorithmException, SignatureException, UnsupportedEncodingException, IllegalArgumentException {
        String[] resultlist = new String[2];
        byte[] mb = null;
        try {
            if (isHexMessage) {
                mb = Hex.decode(message);
            } else {
                mb = message.getBytes("UTF-8");
            }
        } catch (DecoderException e) {
            throw new IllegalArgumentException("This is not a hex message format. " + e.toString());
        }

        XbSignUtil xbSignUtil = new XbSignUtil();
        Byte b = new Byte(V + "");
        ECKey.ECDSASignature verifysignature = ECKey.ECDSASignature.fromComponents(Hex.decode(Rhex), Hex.decode(Shex), b.byteValue());
        boolean isok = false;
        try {
            isok = xbSignUtil.verify(verifysignature, mb);
        } catch (NoSuchAlgorithmException e) {
            throw e;
        }

        ECKey recoverkey = null;
        try {
            recoverkey = xbSignUtil.getRecoveryKey(verifysignature, mb);
        } catch (NoSuchAlgorithmException e) {
            throw e;
        } catch (SignatureException e) {
            throw e;
        }

        if (!AccountAddress.equals(Hex.toHexString(recoverkey.getAddress()))) {
            throw new IllegalArgumentException("Account Address, recover Key Address is misMatch");
        }
        resultlist[0] = "recover Key Hex String: " + Hex.toHexString(recoverkey.getPubKey(false));
        resultlist[1] = "recover Key Address:    " + Hex.toHexString(recoverkey.getAddress());
        return resultlist;
    }

    @ApiOperation(value = "AES/CTR/PKCS7Padding Encryption")
    @RequestMapping(value = "/aesCTREnc", method = {RequestMethod.GET})
    @ResponseBody
    // AES/CTR/PKCS7Padding Encryption
    public String[] aesCTREnc(@RequestParam(value = "message", required = true) String message,
                              @RequestParam(value = "isHexMessage", required = true) boolean isHexMessage)
            throws UnsupportedEncodingException, IllegalArgumentException, InvalidCipherTextException {
        String[] resultlist = new String[2];
        String encoding = "UTF-8";
        byte[] mb = null;
        try {
            if (isHexMessage) {
                mb = Hex.decode(message);
            } else {
                mb = message.getBytes(encoding);
            }
        } catch (DecoderException e) {
            throw new IllegalArgumentException("This is not a hex message format. " + e.toString());
        }

        int keysize = 16;
        XbCipherUtil xbCipherUtil = new XbCipherUtil();
        byte[] keyBytes = xbCipherUtil.keyGen(keysize);

        byte[] ciphertext = null;
        try {
            ciphertext = xbCipherUtil.aesCTRPKCS7PaddingEncryption(keyBytes, mb);
        } catch (InvalidCipherTextException e) {
            throw e;
        }

        resultlist[0] = "key hex: " + Hex.toHexString(keyBytes);
        resultlist[1] = "encrypted hex: " + Hex.toHexString(ciphertext);
        return resultlist;
    }

    @ApiOperation(value = "AES/CTR/PKCS7Padding Decryption")
    @RequestMapping(value = "/aesCTRDec", method = {RequestMethod.GET})
    @ResponseBody
    // AES/CTR/PKCS7Padding Decryption
    public String aesCTRDec(@RequestParam(value = "keyhex", required = true) String keyhex,
                            @RequestParam(value = "hexciphermessage", required = true) String hexciphermessage)
            throws InvalidCipherTextException, IllegalArgumentException, UnsupportedEncodingException {
        String encoding = "UTF-8";
        byte[] keyBytes = null;
        byte[] ciphertext = null;
        try {
            keyBytes = Hex.decode(keyhex);
        } catch (DecoderException e) {
            throw new IllegalArgumentException("This is not a hex key message format. " + e.toString());
        }

        try {
            ciphertext = Hex.decode(hexciphermessage);
        } catch (DecoderException e) {
            throw new IllegalArgumentException("This is not a hex message format. " + e.toString());
        }

        XbCipherUtil xbCipherUtil = new XbCipherUtil();
        byte[] dectext = null;
        try {
            dectext = xbCipherUtil.aesCTRPKCS7PaddingDecryption(keyBytes, ciphertext);
        } catch (InvalidCipherTextException e) {
            throw e;
        }
        return "decrypted data: " + new String(dectext, encoding);
    }

    @ApiOperation(value = "AES/CBC/PKCS7Padding Encryption")
    @RequestMapping(value = "/aesCBCEnc", method = {RequestMethod.GET})
    @ResponseBody
    // AES/CBC/PKCS7Padding Encryption
    public String[] aesCBCEnc(@RequestParam(value = "message", required = true) String message,
                              @RequestParam(value = "isHexMessage", required = true) boolean isHexMessage)
            throws IllegalArgumentException, UnsupportedEncodingException, InvalidCipherTextException {
        String[] resultlist = new String[3];
        String encoding = "UTF-8";
        byte[] mb = null;
        try {
            if (isHexMessage) {
                mb = Hex.decode(message);
            } else {
                mb = message.getBytes(encoding);
            }
        } catch (DecoderException e) {
            throw new IllegalArgumentException("This is not a hex message format. " + e.toString());
        }

        int keysize = 16;
        int ivsize = 16;
        XbCipherUtil xbCipherUtil = new XbCipherUtil();
        byte[] keyBytes = xbCipherUtil.keyGen(keysize);
        byte[] ivBytes = xbCipherUtil.keyGen(ivsize);

        byte[] ciphertext = null;
        try {
            ciphertext = xbCipherUtil.aesCBCPKCS7PaddingEncryption(keyBytes, ivBytes, mb);
        } catch (InvalidCipherTextException e) {
            throw e;
        }

        resultlist[0] = "key hex: " + Hex.toHexString(keyBytes);
        resultlist[1] = "iv hex: " + Hex.toHexString(ivBytes);
        resultlist[2] = "encrypted data hex: " + Hex.toHexString(ciphertext);
        return resultlist;
    }

    @ApiOperation(value = "AES/CBC/PKCS7Padding Decryption")
    @RequestMapping(value = "/aesCBCDec", method = {RequestMethod.GET})
    @ResponseBody
    // AES/CBC/PKCS7Padding Decryption
    public String aesCBCDec(@RequestParam(value = "keyhex", required = true) String keyhex,
                            @RequestParam(value = "ivhex", required = true) String ivhex,
                            @RequestParam(value = "hexciphermessage", required = true) String hexciphermessage)
            throws InvalidCipherTextException, UnsupportedEncodingException, IllegalArgumentException {
        String encoding = "UTF-8";
        byte[] keyBytes = null;
        byte[] ivBytes = null;
        byte[] ciphertext = null;
        try {
            keyBytes = Hex.decode(keyhex);
        } catch (DecoderException e) {
            throw new IllegalArgumentException("This is not a hex key message format. " + e.toString());
        }

        try {
            ivBytes = Hex.decode(ivhex);
        } catch (DecoderException e) {
            throw new IllegalArgumentException("This is not a hex iv message format. " + e.toString());
        }

        try {
            ciphertext = Hex.decode(hexciphermessage);
        } catch (DecoderException e) {
            throw new IllegalArgumentException("This is not a hex message format. " + e.toString());
        }

        XbCipherUtil xbCipherUtil = new XbCipherUtil();
        byte[] dectext = null;
        try {
            dectext = xbCipherUtil.aesCBCPKCS7PaddingDecryption(keyBytes, ivBytes, ciphertext);
        } catch (InvalidCipherTextException e) {
            throw e;
        }
        return "decrypted data: " + new String(dectext, encoding);
    }

    @ApiOperation(value = "ECIES Encrytpion")
    @RequestMapping(value = "/eciesEnc", method = {RequestMethod.GET})
    @ResponseBody
    // ECIES Encrytpion
    public String eciesEnc(@RequestParam(value = "message", required = true) String message,
                           @RequestParam(value = "isHexMessage", required = true) boolean isHexMessage,
                           @RequestParam(value = "recipientPublicKeyHex", required = true) String recipientPublicKeyHex)
            throws IOException, InvalidCipherTextException, IllegalArgumentException {
        String encoding = "UTF-8";
        byte[] mb = null;
        byte[] recipientPublicKey = null;
        try {
            if (isHexMessage) {
                mb = Hex.decode(message);
            } else {
                mb = message.getBytes(encoding);
            }
        } catch (DecoderException e) {
            throw new IllegalArgumentException("This is not a hex message format. " + e.toString());
        }

        try {
            recipientPublicKey = Hex.decode(recipientPublicKeyHex);
        } catch (DecoderException e) {
            throw new IllegalArgumentException("This is not a recipient PublicKey hex format. " + e.toString());
        }

        byte[] encbytes = XbECIESUtil.encryptECIES(recipientPublicKey, mb);
        return "encrypted data hex: " + Hex.toHexString(encbytes);
    }

    @ApiOperation(value = "ECIES Decryption")
    @RequestMapping(value = "/eciesDec", method = {RequestMethod.GET})
    @ResponseBody
    // ECIES Decryption
    public String eciesDec(@RequestParam(value = "hexciphermessage", required = true) String hexciphermessage,
                           @RequestParam(value = "privateKeyHex", required = true) String privateKeyHex)
            throws IllegalArgumentException, IOException, InvalidCipherTextException {
        String encoding = "UTF-8";
        byte[] ciphertext = null;
        byte[] privateKey = null;
        try {
            ciphertext = Hex.decode(hexciphermessage);
        } catch (DecoderException e) {
            throw new IllegalArgumentException("This is not a hex message format. " + e.toString());
        }

        try {
            privateKey = Hex.decode(privateKeyHex);
        } catch (DecoderException e) {
            throw new IllegalArgumentException("This is not a recipient Private Key hex format. " + e.toString());
        }

        byte[] decbytes = XbECIESUtil.decryptECIES(new BigInteger(privateKey), ciphertext);
        return "decrypted data: " + new String(decbytes, encoding);
    }

    @ApiOperation(value = "file binary -> hex string")
    @RequestMapping(value = "filetoHex", method = RequestMethod.POST)
    // file binary -> hex string
    public String filetoHex(@ApiParam(value = "file select", required = true) @RequestPart(value = "file") MultipartFile file)
            throws IOException, IllegalArgumentException {
        if (file.getOriginalFilename().lastIndexOf(".exe") > 0) {
            throw new IllegalArgumentException("exe file not supported.");
        }
        return "file binary Hex: " + Hex.toHexString(file.getBytes());
    }
}
