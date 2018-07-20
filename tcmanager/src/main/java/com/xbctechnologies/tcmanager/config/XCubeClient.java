package com.xbctechnologies.tcmanager.config;

import com.xbctechnologies.core.apis.XCube;
import com.xbctechnologies.core.component.rest.RestHttpClient;
import com.xbctechnologies.core.component.rest.RestHttpConfig;
import lombok.Data;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

@Data
@Component
public class XCubeClient {
    public XCube xCube;
    private String host = "106.251.231.226";
    private int port = 6710;

    @PostConstruct
    public void connect() {
        xCube = new XCube(new RestHttpClient(
                RestHttpConfig.builder()
                        .withXNodeUrl(String.format("http://%s:%s", host, port))
                        .withMaxConnection(100)
                        .withDefaultTimeout(30000)
                        .build()
        ));
    }
}
