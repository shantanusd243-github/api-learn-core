package com.javaprep.backend.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "app.admin")
public class AdminBootstrapProperties {
    private String bootstrapEmail;
    private String bootstrapPassword;
}
