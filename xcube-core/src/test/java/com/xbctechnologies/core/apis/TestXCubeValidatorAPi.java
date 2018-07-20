package com.xbctechnologies.core.apis;

import com.xbctechnologies.core.apis.dto.res.validator.ValidatorSetResponse;
import com.xbctechnologies.core.component.rest.RestHttpClient;
import com.xbctechnologies.core.component.rest.RestHttpConfig;
import org.junit.Before;
import org.junit.Test;

import java.util.Iterator;

import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.junit.Assert.*;

public class TestXCubeValidatorAPi {
    private XCube xCube;
    private String targetChainId = "1T";

    @Before
    public void init() {
        String etherHost = "106.251.231.226:6710";
        String localhost = "localhost:7979";
        xCube = new XCube(new RestHttpClient(
                RestHttpConfig.builder()
                        .withXNodeUrl(String.format("http://%s", localhost))
                        .withMaxConnection(100)
                        .withDefaultTimeout(60000)
                        .build()
        ));
    }

    @Test
    public void TestGetValidatorSet() {
        int blockNo = 1;
        ValidatorSetResponse vRes = xCube.getValidatorSet(null, targetChainId, blockNo).send();
        assertNull(vRes.getError());
        assertNotNull(vRes.getValidatorSet());
        assertThat(vRes.getValidatorSet().size(), greaterThanOrEqualTo(0));

        if (vRes.getValidatorSet() != null) {
            System.out.println(vRes.getValidatorSet().size());
            Iterator it = vRes.getValidatorSet().iterator();
            while (it.hasNext()) {
                String accountAddr = (String) it.next();
                System.out.println(accountAddr);
            }
        }
    }
}
