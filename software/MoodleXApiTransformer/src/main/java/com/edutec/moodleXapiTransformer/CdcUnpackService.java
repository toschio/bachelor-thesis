package com.edutec.moodleXapiTransformer;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Optional;

/**
 * unpacks Key and Value from cdc-wrapper
 */
@Service
public class CdcUnpackService {

    private final ObjectMapper mapper = new ObjectMapper();
    private final Log logger = LogFactory.getLog(getClass());

    public Optional<Long> unpackKey(JsonNode wrapper) {
        // extract the message's key
        Long newKey = null;
        if (wrapper.has("after") && wrapper.get("after").has("id")) {
            newKey = wrapper.get("after").get("id").asLong();
        } else if (wrapper.has("before") && wrapper.get("before").has("id")) {
            newKey = wrapper.get("before").get("id").asLong();
        }
        if (newKey == null) {
            return Optional.empty();
        }
        return Optional.of(newKey);
    }

    private Optional<JsonNode> unpack(JsonNode wrapper) {
        // extract the message's payload
        JsonNode value = null;
        if (wrapper.has("after") && wrapper.get("after").has("id"))
            value = wrapper.get("after");
        else {
            // if after is null, then concent is deleted
//            value = wrapper.get("before");
//            ((ObjectNode) value).put("deleted", true);
            return Optional.empty();
        }
        if (value == null) {
            return Optional.empty();
        }
        return Optional.of(value);
    }

    public <U> U unpack(JsonNode value, Class<U> clazz) {
        U unpackedObject = null;
        try {
            final Optional<JsonNode> unpack = unpack(value);
            if (unpack.isPresent()) {
                unpackedObject = mapper.readValue(unpack.get().toString(), clazz);
            }
        } catch (IOException e) {
            logger.error(e);
        }
        logger.info(unpackedObject);
        return unpackedObject;

    }
}
