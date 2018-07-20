package com.xbctechnologies.xcrypto.ec.test;

import com.xbctechnologies.xcrypto.api.XbSignUtil;
import com.xbctechnologies.xcrypto.ec.ECKey;
import org.junit.Test;
import org.spongycastle.util.encoders.Hex;

import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;

public class TestSign {

   /* static {
        Security.insertProviderAt(new org.spongycastle.jce.provider.BouncyCastleProvider(), 1);
    }*/

    @Test
    public void ecdsaSha256SignAndVerify() throws UnsupportedEncodingException {
        String msgstr = "hello world!";
        String encoding = "UTF-8";
        byte[] mb = msgstr.getBytes(encoding);

        //String prikeyHexstr = "0b4fe73acbc6cfbfc38cc379d738f1061a4849c68b57ee8247ace8470b187dad";

        XbSignUtil xbSignUtil = new XbSignUtil();

        ECKey key = xbSignUtil.ecKeyGen();

        System.out.println(Hex.toHexString(key.getPub(key.getPubKey(false)).getEncoded(false)));
        //System.out.println( Hex.toHexString( key.getPubKeyPoint().getEncoded(false) ) );

        ECKey.ECDSASignature signature = null;
        try {
            //signature = xbSignUtil.sign(prikeyHexstr, mb);
            signature = xbSignUtil.sign(key.getPrivKeyHexString(), mb);
        } catch (NoSuchAlgorithmException e) {
            System.out.println(e.toString());
            return;
        } catch (SignatureException e) {
            System.out.println(e.toString());
            return;
        }
        String signatureBase64 = signature.toBase64();
        System.out.println("Signtr: " + signatureBase64 + " (Base64, length: " + signatureBase64.length() + ")");
        System.out.println("R Hex string: " + signature.getRHexString());
        System.out.println("S Hex string: " + signature.getSHexString());
        System.out.println("V int:        " + signature.getVInt());

        ECKey.ECDSASignature verifysignature = ECKey.ECDSASignature.fromComponents(signature.r.toByteArray(), signature.s.toByteArray(), signature.v);
        boolean isok = false;
        try {
            isok = xbSignUtil.verify(verifysignature, mb);
        } catch (NoSuchAlgorithmException e) {
            System.out.println(e.toString());
            return;
        }
        System.out.println("isok: " + isok);

        ECKey recoverkey = null;
        try {
            recoverkey = xbSignUtil.getRecoveryKey(verifysignature, mb);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return;
        } catch (SignatureException e) {
            e.printStackTrace();
            return;
        }
        System.out.println(Hex.toHexString(key.getPubKey(false)));
        System.out.println(Hex.toHexString(recoverkey.getPubKey(false)));
    }

    @Test
    public void eccSecp256k1GenKeyPair() {
        XbSignUtil xbSignUtil = new XbSignUtil();
        ECKey key = xbSignUtil.ecKeyGen();
        System.out.println("priKey hex: " + Hex.toHexString(key.getPrivKeyBytes()));
        System.out.println("pubKey hex: " + Hex.toHexString(key.getPubKey(true)));
        System.out.println("Address hex: " + Hex.toHexString(key.getAddress()));

        ECKey testkey = ECKey.fromPublicOnly(key.getPubKey(true));
        System.out.println("Address hex: " + Hex.toHexString(testkey.getAddress()));
    }

    @Test
    public void accountAddress() {
        String pubKeyHexStr = "03c3c3f4aa12dd61993a585e7b6da70469535e77fb6d9197e7c3e6b9477bb58cdd";
        ECKey key = ECKey.fromPublicOnly(Hex.decode(pubKeyHexStr));
        System.out.println("Address hex: " + Hex.toHexString(key.getAddress()) + "size " + Hex.decode(pubKeyHexStr).length);
    }
}
