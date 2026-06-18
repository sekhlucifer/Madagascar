package com.framework.utils;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import java.io.File;
import java.io.IOException;
import java.util.Map;

/**
 * Utility class for JSON serialisation and deserialisation using Jackson.
 *
 * <pre>
 *   // Read a JSON file into a Map
 *   Map&lt;String,Object&gt; data = JsonUtil.readFile("testdata/payload.json");
 *
 *   // Convert a POJO to JSON string
 *   String json = JsonUtil.toJson(myObject);
 * </pre>
 */
public final class JsonUtil {

    private static final ObjectMapper MAPPER = new ObjectMapper()
        .enable(SerializationFeature.INDENT_OUTPUT);

    private JsonUtil() {}

    /** Deserialises a JSON file into the given type. */
    public static <T> T readFile(String filePath, Class<T> type) {
        try {
            return MAPPER.readValue(new File(filePath), type);
        } catch (IOException e) {
            throw new RuntimeException("Failed to read JSON file: " + filePath, e);
        }
    }

    /** Deserialises a JSON file into a {@code Map<String, Object>}. */
    public static Map<String, Object> readFile(String filePath) {
        return readFile(filePath, new TypeReference<>() {});
    }

    /** Deserialises a JSON file using a {@link TypeReference} (useful for generics). */
    public static <T> T readFile(String filePath, TypeReference<T> ref) {
        try {
            return MAPPER.readValue(new File(filePath), ref);
        } catch (IOException e) {
            throw new RuntimeException("Failed to read JSON file: " + filePath, e);
        }
    }

    /** Serialises an object to a formatted JSON string. */
    public static String toJson(Object obj) {
        try {
            return MAPPER.writeValueAsString(obj);
        } catch (IOException e) {
            throw new RuntimeException("JSON serialisation failed", e);
        }
    }

    /** Deserialises a JSON string into the given type. */
    public static <T> T fromJson(String json, Class<T> type) {
        try {
            return MAPPER.readValue(json, type);
        } catch (IOException e) {
            throw new RuntimeException("JSON deserialisation failed", e);
        }
    }

    /** Converts an object to a {@code Map<String,Object>} (field introspection). */
    @SuppressWarnings("unchecked")
    public static Map<String, Object> toMap(Object obj) {
        return MAPPER.convertValue(obj, Map.class);
    }
}
