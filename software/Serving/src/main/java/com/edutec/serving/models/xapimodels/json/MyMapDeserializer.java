package com.edutec.serving.models.xapimodels.json;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class MyMapDeserializer extends JsonDeserializer<Map<String, String>> {

    private Log logger = LogFactory.getLog(MyMapDeserializer.class);

    @Override
    public Map<String, String> deserialize(JsonParser jp, DeserializationContext context)
            throws IOException {
        logger.error("HERERE WE ARE");
        ObjectMapper mapper = (ObjectMapper) jp.getCodec();
        if (jp.getCurrentToken().equals(JsonToken.START_OBJECT)) {
            return mapper.readValue(jp, new TypeReference<HashMap<String, String>>() {
            });
        } else {
            //consume this stream
            mapper.readTree(jp);
            return new HashMap<String, String>();
        }
    }
}
