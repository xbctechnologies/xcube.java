package com.xbctechnologies.core.utils;

/**
 * Created by sv506 on 2016-10-12.
 */

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class JsonUtil {
    private static final Logger logger = LoggerFactory.getLogger(JsonUtil.class);

    private static final ObjectMapper mapper;

    static {
        mapper = new ObjectMapper() {
            {
                configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            }
        };
        mapper.setVisibility(mapper.getSerializationConfig().getDefaultVisibilityChecker()
                .withFieldVisibility(JsonAutoDetect.Visibility.ANY)
                .withGetterVisibility(JsonAutoDetect.Visibility.NONE)
                .withSetterVisibility(JsonAutoDetect.Visibility.NONE)
                .withCreatorVisibility(JsonAutoDetect.Visibility.NONE)
        );

        mapper.enable(DeserializationFeature.USE_BIG_DECIMAL_FOR_FLOATS);
        mapper.enable(DeserializationFeature.USE_BIG_INTEGER_FOR_INTS);
    }

    public static String generateClassToJson(Object obj) {
        if (obj == null) {
            return null;
        }
        String json = null;
        try {
            json = mapper.writeValueAsString(obj);
        } catch (IOException e) {
            logger.error("generateClassToJson json Exception error : " + e.getMessage() + ", toString : " + obj.toString());
            return null;
        }
        return json;
    }


    public static <T> T generateJsonToClass(String jsonData, TypeReference valueTypeRef) {
        if (jsonData == null) {
            return null;
        }
        T object = null;
        try {
            object = mapper.readValue(jsonData, valueTypeRef);
        } catch (IOException e) {
            logger.error("Json Util Parsing Error : " + e.getMessage());
            return null;
        }
        return object;
    }

    public static <T> T generateJsonToClass(String jsonData, Class<T> valueTypeRef) {
        if (jsonData == null) {
            return null;
        }
        try {
            return mapper.readValue(jsonData, valueTypeRef);
        } catch (IOException e) {
            e.printStackTrace();
            logger.error("Json Util Parsing Error : " + e.getMessage());
            return null;
        }
    }
}
