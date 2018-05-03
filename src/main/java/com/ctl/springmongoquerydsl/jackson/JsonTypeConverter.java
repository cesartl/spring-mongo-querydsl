package com.ctl.springmongoquerydsl.jackson;

import com.github.drapostolos.typeparser.DynamicParser;
import com.github.drapostolos.typeparser.TypeParser;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.UUID;

/**
 * Permit to transform an a string or integer value into the specified type.
 */
public class JsonTypeConverter {

    private TypeParser parser;

    /**
     * Default constructor of {@link JsonTypeConverter}
     */
    public JsonTypeConverter() {
        // create a TypeParser instance with default settings.
        parser = TypeParser.newBuilder()
                .build();
    }

    /**
     * Convert the specified string into the specified type
     *
     * @param value the string value
     * @param type  expected type
     * @return the converted object
     */
    public <T> T convert(String value, Class<T> type) {
        return doConvert(value, type);
    }

    /**
     * Convert the specified integer into the specified type
     *
     * @param value the integer value
     * @param type  expected type
     * @return the converted object
     */
    public <T> T convert(Integer value, Class<T> type) {
        return doConvert(value, type);
    }

    /**
     * Convert the specified object into the specified type
     *
     * @param value the object value
     * @param type  expected type
     * @return the converted object
     */
    private <T> T doConvert(Object value, Class<T> type) {
        // nothing to do - it is the good type
        if (value.getClass().equals(type)) {
            return (T) value;
        }
        // date converter
        if (value instanceof String) {
            if (type.equals(Date.class)) {
                return (T) new Date(Instant.parse((CharSequence) value).toEpochMilli());
            }
        }
        return parser.parse(value.toString(), type);
    }
}