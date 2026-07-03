package com.javaprep.backend.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public enum Priority {
    @JsonProperty("Must Know")
    MUST_KNOW,
    
    @JsonProperty("Important")
    IMPORTANT,
    
    @JsonProperty("Nice to Know")
    NICE_TO_KNOW,

    @JsonProperty("Advanced")
    ADVANCED;

    @JsonCreator
    public static Priority fromString(String value) {
        for (Priority p : Priority.values()) {
            // Check if it matches the @JsonProperty OR the enum constant name
            if (p.name().equalsIgnoreCase(value) ||
                    getJsonPropertyValue(p).equalsIgnoreCase(value)) {
                return p;
            }
        }
        throw new IllegalArgumentException("Unknown priority: " + value);
    }

    private static String getJsonPropertyValue(Priority p) {
        try {
            return Priority.class.getField(p.name()).getAnnotation(JsonProperty.class).value();
        } catch (Exception e) {
            return p.name();
        }
    }
}