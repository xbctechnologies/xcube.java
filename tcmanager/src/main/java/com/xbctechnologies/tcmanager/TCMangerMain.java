package com.xbctechnologies.tcmanager;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@Slf4j
@EnableCaching(proxyTargetClass = true)
@SpringBootApplication(scanBasePackages = {"com.xbctechnologies.tcmanager"})
public class TCMangerMain {
    public static void main(String[] args) {
        SpringApplication.run(TCMangerMain.class, args);
        log.info("Start TCMangerMain");
    }
}
