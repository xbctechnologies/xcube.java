package com.xbctechnologies.xcrypto.api;

import org.spongycastle.crypto.Digest;
import org.spongycastle.crypto.digests.KeccakDigest;
import org.spongycastle.crypto.digests.SHA256Digest;

import java.security.NoSuchAlgorithmException;

public class XbHashUtil {

    private Digest digest = null;

    // Keccak256 or SHA256 Hash algorithm Setting Constructor
    public XbHashUtil(String algorithm) throws NoSuchAlgorithmException {
        // 1. Keccak256 or SHA256 algorithm Check
        // 2. return Digest Object
        if (algorithm.toLowerCase().equals("keccak256")) {
            digest = new KeccakDigest(256);
        } else if (algorithm.toLowerCase().equals("sha256")) {
            digest = new SHA256Digest();
        } else {
            throw new NoSuchAlgorithmException("No Such Algorithm " + algorithm + " ex) keccak256, sha256 ");
        }
    }

    // Hash Target message update Method
    public void update(byte[] msg) {
        // 1. message update
        digest.update(msg, 0, msg.length);
    }

    // return Hash byte[] Method
    public byte[] getHash() {
        // 1. new has byte[]
        byte[] hash = new byte[digest.getDigestSize()];
        // 2. return Hash byte[]
        digest.doFinal(hash, 0);
        return hash;
    }
}
