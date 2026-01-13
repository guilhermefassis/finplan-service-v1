package io.finplan.service.mapper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Map;

public abstract class BaseMapper {

    @Autowired
    protected ObjectMapper objectMapper;

    /**
     * Converte Map para JSON String (útil para campos JSONB)
     */
    protected String mapToJson(Map<String, Object> map) {
        if (map == null) return null;
        try {
            return objectMapper.writeValueAsString(map);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error converting map to JSON", e);
        }
    }

    /**
     * Converte JSON String para Map
     */
    protected Map<String, Object> jsonToMap(String json) {
        if (json == null) return null;
        try {
            return objectMapper.readValue(json, Map.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error parsing JSON", e);
        }
    }
}