package com.javaprep.backend.config;

import com.javaprep.backend.enums.Priority;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.ReadingConverter;
import org.springframework.data.convert.WritingConverter;
import org.springframework.data.mongodb.core.convert.MongoCustomConversions;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;

@Configuration
public class MongoConfig {

    @Bean
    public MongoCustomConversions customConversions() {
        return new MongoCustomConversions(Arrays.asList(
                new PriorityReadConverter(),
                new PriorityWriteConverter()
        ));
    }

    // Tells MongoDB how to read "Must Know" into Priority.MUST_KNOW
    @ReadingConverter
    public static class PriorityReadConverter implements Converter<String, Priority> {
        @Override
        public Priority convert(String source) {
            switch (source) {
                case "Must Know": return Priority.MUST_KNOW;
                case "Important": return Priority.IMPORTANT;
                case "Nice to Know": return Priority.NICE_TO_KNOW;
                case "Advanced": return Priority.ADVANCED;
                default: throw new IllegalArgumentException("Unknown priority: " + source);
            }
        }
    }

    // Tells MongoDB how to save Priority.MUST_KNOW as "Must Know"
    @WritingConverter
    public static class PriorityWriteConverter implements Converter<Priority, String> {
        @Override
        public String convert(Priority source) {
            switch (source) {
                case MUST_KNOW: return "Must Know";
                case IMPORTANT: return "Important";
                case NICE_TO_KNOW: return "Nice to Know";
                case ADVANCED: return "Advanced";
                default: throw new IllegalArgumentException("Unknown priority: " + source);
            }
        }
    }
}