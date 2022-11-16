package de.wuespace;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.vertx.core.json.JsonObject;

import java.util.Optional;
import java.util.function.Consumer;

public class JsonHelper {
    private static final ObjectMapper objectMapper = new ObjectMapper();

    static {
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        objectMapper.configure(DeserializationFeature.FAIL_ON_NULL_CREATOR_PROPERTIES, false);
        objectMapper.configure(DeserializationFeature.ACCEPT_FLOAT_AS_INT, true);
    }

    /**
     * @return the object mapper used by this class
     */
    public static ObjectMapper getObjectMapper() {
        return objectMapper;
    }

    /**
     * Returns the value of the given field as an {@link Optional} of the given type.
     * @param json The JSON node to get the value from.
     * @param clazz The type of the value.
     * @return The value of the given field as an {@link Optional} of the given type.
     * @param <T> The type of the value.
     */
    public static <T> Optional<T> readValue(String json, Class<T> clazz) {
        try {
            return Optional.of(objectMapper.readValue(json, clazz));
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    /**
     * Calls the given consumer with the value of the given json as the given type (clazz).
     * @param json The JSON string to get the value from.
     * @param clazz The type of the value.
     * @param consumer The consumer to call with the value.
     * @param <T> The type of the value.
     */
    public static <T> void on(String json, Class<T> clazz, Consumer<T> consumer) {
        readValue(json, clazz).ifPresent(consumer);
    }

    /**
     * @see #on(String, Class, Consumer)
     */
    public static <T> void on(JsonNode json, Class<T> clazz, Consumer<T> consumer) {
        readValue(json.toString(), clazz).ifPresent(consumer);
    }

    /**
     * @see #on(String, Class, Consumer)
     */
    public static <T> void on(JsonObject json, Class<T> clazz, Consumer<T> consumer) {
        readValue(json.toString(), clazz).ifPresent(consumer);
    }

    /**
     * Returns the JSON encoded string of the given object. Logs an error and returns an empty object if the object
     * couldn't be encoded.
     * @param object The object to encode.
     * @return The JSON encoded string of the given object.
     */
    public static JsonObject encode(Object object) {
        try {
            return new JsonObject(objectMapper.writeValueAsString(object));
        } catch (JsonProcessingException e) {
            System.err.printf("Could not encode object %s%n", object);
            return new JsonObject();
        }
    }
}
