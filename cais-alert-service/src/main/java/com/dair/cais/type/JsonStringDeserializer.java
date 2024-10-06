package com.dair.cais.type;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public class JsonStringDeserializer extends JsonDeserializer<List<Map<String, Object>>> {
    private static final ObjectMapper mapper = new ObjectMapper();

    @Override
    public List<Map<String, Object>> deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JsonProcessingException {
        String jsonString = p.getValueAsString();
        System.out.println("Deserializing JSON String: " + jsonString); // Add this line for logging
        return mapper.readValue(jsonString, List.class);
    }
}
