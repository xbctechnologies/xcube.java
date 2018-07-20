package com.xbctechnologies.core.apis.serializer;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.xbctechnologies.core.utils.Base64Util;
import com.xbctechnologies.core.utils.JsonUtil;
import com.xbctechnologies.core.apis.dto.xtypes.TxFileBody;

import java.io.IOException;

public class PayloadBodySerializer extends JsonSerializer {
    @Override
    public void serialize(Object value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        if (TxFileBody.class.isInstance(value)) {
            TxFileBody txFileBody = (TxFileBody) value;
            String val = txFileBody.getFile() == null ? "" : Base64Util.encode(txFileBody.getFile());
            txFileBody.setDataHash(val);
            gen.writeString(Base64Util.encode(JsonUtil.generateClassToJson(value)));
        } else {
            gen.writeString(Base64Util.encode(JsonUtil.generateClassToJson(value)));
        }
    }
}
