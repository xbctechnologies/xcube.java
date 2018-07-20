package com.xbctechnologies.core.component.rest.dto;

import lombok.Data;
import org.apache.http.Header;

/**
 * Created by alex on 2017. 1. 18..
 */
@Data
public class RestResDto {
    private int statusCode;
    private String responseBody;

    private Header[] headers;
}
