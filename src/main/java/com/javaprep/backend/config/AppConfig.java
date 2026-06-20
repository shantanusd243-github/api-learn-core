package com.javaprep.backend.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.config.EnableMongoAuditing;

@Configuration
@EnableConfigurationProperties({JwtProperties.class, CorsProperties.class, AdminBootstrapProperties.class})
@EnableMongoAuditing // enables @CreatedDate / @LastModifiedDate on documents
public class AppConfig {
}
