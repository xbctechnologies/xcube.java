package com.xbctechnologies.tcmanager.config;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.collect.Ordering;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import springfox.documentation.annotations.ApiIgnore;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.ApiListingReference;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

@Slf4j
@Configuration
@EnableSwagger2
public class SwaggerConfig {
    @Bean
    public Docket api() {
        return new Docket(DocumentationType.SWAGGER_2)
                .pathMapping("/")
                .apiListingReferenceOrdering(new Ordering<ApiListingReference>() {
                    @Override
                    public int compare(ApiListingReference left, ApiListingReference right) {
                        return 0;
                    }
                })
                .apiInfo(apiInfo())
                .ignoredParameterTypes(ApiIgnore.class, JsonIgnore.class)
                .enable(true)
                .select()
                .apis(RequestHandlerSelectors.basePackage("com.xbctechnologies.tcmanager"))
                .paths(PathSelectors.any())
                .build();
    }

    /*@Bean
    public UiConfiguration uiConfig() {
        return UiConfigurationBuilder.builder()
                .deepLinking(true)
                .displayOperationId(false)
                .defaultModelsExpandDepth(0)
                .defaultModelExpandDepth(0)
                .defaultModelRendering(ModelRendering.EXAMPLE)
                .displayRequestDuration(false)
                .docExpansion(DocExpansion.LIST)
                .filter(false)
                .maxDisplayedTags(null)
                .operationsSorter(OperationsSorter.METHOD)
                .showExtensions(true)
                .supportedSubmitMethods(UiConfiguration.Constants.DEFAULT_SUBMIT_METHODS)
                .validatorUrl(null)
                .build();
    }*/

    private ApiInfo apiInfo() {
        return new ApiInfoBuilder()
                .title("Official2 golang implementation of the XBlockchain.")
                .version("1.0")
                .build();
    }
}
