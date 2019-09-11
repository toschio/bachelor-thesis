package com.edutec.AssessmentEvaluator.models.leapmotionmodels;

import com.fasterxml.jackson.core.*;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.IOException;
import java.util.*;

public class Serdes {

    public static class Vector3Deserializer extends StdDeserializer {
        public Vector3Deserializer() {
            this(null);
        }
        public Vector3Deserializer(Class<?> vc) {
            super(vc);
        }

        @Override
        public Object deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException, JsonProcessingException {
            JsonToken token;
            token = jsonParser.getCurrentToken();
            if (token != JsonToken.START_ARRAY) {
                jsonParser.nextToken();

                throw new JsonParseException(jsonParser, "Start array expected");
            }
            try {
                token = jsonParser.nextToken();
                Float first = jsonParser.getFloatValue();
                token = jsonParser.nextToken();
                Float second = jsonParser.getFloatValue();
                token = jsonParser.nextToken();
                Float third = jsonParser.getFloatValue();
                token = jsonParser.nextToken();
                if (token != JsonToken.END_ARRAY) {
                    throw new JsonParseException(jsonParser, "End array expected");
                }
                return new LeapMotionFrame.Vector3(first, second, third);
            } catch (Exception e) {
                return null;
            }
        }
    }

    public static class Vector3Serializer extends com.fasterxml.jackson.databind.JsonSerializer<LeapMotionFrame.Vector3> {
        @Override
        public void serialize(LeapMotionFrame.Vector3 value, JsonGenerator gen, SerializerProvider provider) throws IOException {
            gen.writeStartArray(3);
            gen.writeNumber(value.getFirst());
            gen.writeNumber(value.getSecond());
            gen.writeNumber(value.getThird());
            gen.writeEndArray();

        }
    }

    public static class GestureDeserializer extends StdDeserializer {

        public GestureDeserializer() {
            this(null);
        }

        public GestureDeserializer(Class<?> vc) {
            super(vc);
        }

        @Override
        public Object deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JsonProcessingException {
            Class<? extends LeapMotionFrame.Gesture> clazz = null;
            ObjectMapper mapper = (ObjectMapper) p.getCodec();
            ObjectNode obj = (ObjectNode) mapper.readTree(p);

            Iterator<Map.Entry<String, JsonNode>> elementsIterator = obj.fields();
            while (elementsIterator.hasNext()) {
                Map.Entry<String, JsonNode> element = elementsIterator.next();
                String name = element.getKey();
                if (name.equals("type")) {
                    String value = element.getValue().asText();
                    switch (value) {
                        case "circle":
                            clazz = LeapMotionFrame.CircleGesture.class;
                            break;
                        case "keyTap":
                            clazz = LeapMotionFrame.KeyTapGesture.class;
                            break;
                        case "swipe":
                            clazz = LeapMotionFrame.SwipeGesture.class;
                            break;
                        case "screenTap":
                            clazz = LeapMotionFrame.ScreenTapGesture.class;
                            break;
                            // todo other motions
                    }
                }
            }
            // missing data better than breaking application
            if (clazz == null) {
                throw new RuntimeException("Null class !");
            }
            return mapper.treeToValue(obj, clazz);
        }
    }
}
