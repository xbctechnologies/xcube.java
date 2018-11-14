package com.xbctechnologies.core.apis.dto.res.account;

import com.xbctechnologies.core.apis.dto.res.Response;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
public class AccountExportResponse extends Response<AccountExportResponse.Result> {
    @Data
    public static class Result {
        private String address;
        private Crypto crypto;
        private String id;
        private int version;
    }

    @Data
    public static class Crypto {
        private String cipher;
        private String ciphertext;
        private Cipherparams cipherparams;
        private String kdf;
        private KDFParams kdfparams;
//        private int n;
//        private int r;
//        private int p;
//        private int dklen;
        private int c;
        private String prf;
//        private String salt;
        private String mac;
    }

    @Data
    public static class Cipherparams {
        private String iv;
    }

    @Data
    public static class KDFParams {
        private int n;
        private int r;
        private int p;
        private int dklen;
        private String salt;
    }

}
