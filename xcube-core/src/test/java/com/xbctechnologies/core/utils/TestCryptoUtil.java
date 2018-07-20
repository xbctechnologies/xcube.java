package com.xbctechnologies.core.utils;

import com.xbctechnologies.core.TestParent;
import com.xbctechnologies.core.apis.dto.xtypes.proto.StringProto;
import org.junit.Test;

import java.io.File;

public class TestCryptoUtil {
    private String testFile = "/testFile";

    @Test
    public void sha256() {
        String val = Base64Util.encode(new File(this.getClass().getResource(testFile).getFile()));

        StringProto.String value = StringProto.String.newBuilder().setValue(val).build();
        String stringData = TestParent.generateByteToString(value.toByteArray());
        System.out.println(stringData);


        byte[] data = CryptoUtil.sha256(value.toByteArray());
        String sha256 = TestParent.generateByteToString(data);
        System.out.println(sha256);
    }
}
