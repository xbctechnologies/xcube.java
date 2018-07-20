package com.xbctechnologies.xcrypto.api;

import com.xbctechnologies.xcrypto.ec.ConcatKDFBytesGenerator;
import com.xbctechnologies.xcrypto.ec.ECKey;
import com.xbctechnologies.xcrypto.ec.XbIESEngine;
import org.spongycastle.asn1.sec.SECNamedCurves;
import org.spongycastle.asn1.x9.X9ECParameters;
import org.spongycastle.crypto.AsymmetricCipherKeyPair;
import org.spongycastle.crypto.BufferedBlockCipher;
import org.spongycastle.crypto.InvalidCipherTextException;
import org.spongycastle.crypto.KeyGenerationParameters;
import org.spongycastle.crypto.agreement.ECDHBasicAgreement;
import org.spongycastle.crypto.digests.SHA256Digest;
import org.spongycastle.crypto.engines.AESEngine;
import org.spongycastle.crypto.generators.ECKeyPairGenerator;
import org.spongycastle.crypto.macs.HMac;
import org.spongycastle.crypto.modes.SICBlockCipher;
import org.spongycastle.crypto.params.*;
import org.spongycastle.math.ec.ECPoint;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.security.SecureRandom;

public class XbECIESUtil {

    /**
     * The parameters of the secp256k1 curve.
     */
    public static final ECDomainParameters CURVE;
    public static final int KEY_SIZE = 128;

    static {
        // All clients must agree on the curve to use by agreement. Ethereum uses secp256k1.
        X9ECParameters params = SECNamedCurves.getByName("secp256k1");
        CURVE = new ECDomainParameters(params.getCurve(), params.getG(), params.getN(), params.getH());
    }

    public XbECIESUtil() {

    }

    // ECIES Encryption method
    public static byte[] encryptECIES(byte[] pubbytes, byte[] plaintext) throws InvalidCipherTextException, IOException {
        // 1. Decode a point on this curve from its ASN.1 encoding.
        ECPoint toPub = CURVE.getCurve().decodePoint(pubbytes);

        // 2. temporary ECC key generate
        ECKeyPairGenerator eGen = new ECKeyPairGenerator();
        SecureRandom random = new SecureRandom();
        KeyGenerationParameters gParam = new ECKeyGenerationParameters(CURVE, random);
        eGen.init(gParam);
        byte[] IV = new byte[KEY_SIZE / 8];
        new SecureRandom().nextBytes(IV);

        AsymmetricCipherKeyPair ephemPair = eGen.generateKeyPair();
        BigInteger prv = ((ECPrivateKeyParameters) ephemPair.getPrivate()).getD();
        ECPoint pub = ((ECPublicKeyParameters) ephemPair.getPublic()).getQ();
        // 3. XbIESEngine Object create
        XbIESEngine iesEngine = makeIESEngine(true, toPub, prv, IV);

        ECKeyGenerationParameters keygenParams = new ECKeyGenerationParameters(CURVE, random);
        ECKeyPairGenerator generator = new ECKeyPairGenerator();
        generator.init(keygenParams);

        ECKeyPairGenerator gen = new ECKeyPairGenerator();
        gen.init(new ECKeyGenerationParameters(ECKey.CURVE, random));
        // 4. return temporary ECC public key || iv || enc(plaintext)
        byte[] cipher = iesEngine.processBlock(plaintext, 0, plaintext.length);
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        bos.write(pub.getEncoded(false));
        bos.write(IV);
        bos.write(cipher);
        return bos.toByteArray();
    }

    // ECIES Decryption method
    public static byte[] decryptECIES(BigInteger prikey, byte[] cipher) throws InvalidCipherTextException, IOException {
        // 1. temporary ECC public key || iv || enc(plaintext) read
        ByteArrayInputStream is = new ByteArrayInputStream(cipher);
        byte[] ephemBytes = new byte[2 * ((CURVE.getCurve().getFieldSize() + 7) / 8) + 1];
        is.read(ephemBytes);
        ECPoint ephem = CURVE.getCurve().decodePoint(ephemBytes);
        byte[] IV = new byte[KEY_SIZE / 8];
        is.read(IV);
        byte[] cipherBody = new byte[is.available()];
        is.read(cipherBody);
        // 2. XbIESEngine Object create
        XbIESEngine iesEngine = makeIESEngine(false, ephem, prikey, IV);
        // 3. decryption
        byte[] message = iesEngine.processBlock(cipherBody, 0, cipherBody.length);
        return message;
    }

    // ECIES engine method
    private static XbIESEngine makeIESEngine(boolean isEncrypt, ECPoint pub, BigInteger prv, byte[] IV) {
        AESEngine aesFastEngine = new AESEngine();

        XbIESEngine iesEngine = new XbIESEngine(
                new ECDHBasicAgreement(),
                new ConcatKDFBytesGenerator(new SHA256Digest()),
                new HMac(new SHA256Digest()),
                new SHA256Digest(),
                new BufferedBlockCipher(new SICBlockCipher(aesFastEngine)));

        byte[] d = new byte[]{};
        byte[] e = new byte[]{};

        IESParameters p = new IESWithCipherParameters(d, e, KEY_SIZE, KEY_SIZE);
        ParametersWithIV parametersWithIV = new ParametersWithIV(p, IV);

        iesEngine.init(isEncrypt, new ECPrivateKeyParameters(prv, CURVE), new ECPublicKeyParameters(pub, CURVE), parametersWithIV);
        return iesEngine;
    }
}
