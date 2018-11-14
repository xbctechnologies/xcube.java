package com.xbctechnologies.core.utils;

import com.xbctechnologies.core.apis.dto.req.tx.TxRequest;
import com.xbctechnologies.core.apis.dto.res.account.AccountExportResponse;
import com.xbctechnologies.xcrypto.api.XbCipherUtil;
import com.xbctechnologies.xcrypto.api.XbSignUtil;
import com.xbctechnologies.xcrypto.ec.ECKey;
import org.spongycastle.util.encoders.Hex;

import java.math.BigInteger;

public class SignUtil {
    public static void signTx(TxRequest txRequest, String password, String privKeyJson) throws Exception {
        AccountExportResponse exportResponse = JsonUtil.generateJsonToClass(privKeyJson, AccountExportResponse.class);
        signTx(
                txRequest,
                password,
                exportResponse.getResult().getCrypto().getKdfparams().getSalt(),
                exportResponse.getResult().getCrypto().getKdfparams().getN(),
                exportResponse.getResult().getCrypto().getKdfparams().getR(),
                exportResponse.getResult().getCrypto().getKdfparams().getP(),
                exportResponse.getResult().getCrypto().getKdfparams().getDklen(),
                exportResponse.getResult().getCrypto().getCiphertext(),
                exportResponse.getResult().getCrypto().getMac(),
                exportResponse.getResult().getCrypto().getCipherparams().getIv()
        );
    }

    public static void signTx(TxRequest txRequest, String password, String salt, int n, int r, int p, int dklen, String ciphertext, String mac, String iv) throws Exception {
        if (txRequest.getTime() == null || txRequest.getTime().intValue() == 0) {
            txRequest.setTime(new BigInteger(String.valueOf(DateUtil.getMicroSecond())));
        }

        XbCipherUtil cipherUtil = new XbCipherUtil();
        String priKeyHexstr = cipherUtil.pbkdfDecyrptKey(password.getBytes("UTF-8"),
                Hex.decode(salt), n, r, p, dklen, ciphertext, mac, iv);

        // 4. Tx Sign
        XbSignUtil xbSignUtil = new XbSignUtil();
        ECKey.ECDSASignature signature = xbSignUtil.sign(priKeyHexstr, txRequest.marshalProto(false).toByteArray());
        txRequest.setR(signature.r);
        txRequest.setS(signature.s);
        txRequest.setV(signature.getVInt());
    }

    public static String getAddress(TxRequest txRequest) throws Exception {
        return getAddress(txRequest.marshalProto(false).toByteArray(), txRequest.getR(), txRequest.getS(), txRequest.getV());
    }

    public static String getAddress(byte[] msg, BigInteger r, BigInteger s, int v) throws Exception {
        Byte b = new Byte(v + "");
        ECKey.ECDSASignature signature = ECKey.ECDSASignature.fromComponents(r.toByteArray(), s.toByteArray(), b.byteValue());
        XbSignUtil xbSignUtil = new XbSignUtil();
        ECKey key = xbSignUtil.getRecoveryKey(signature, msg);
        return "0x" + Hex.toHexString(key.getAddress()).toLowerCase();
    }
}
