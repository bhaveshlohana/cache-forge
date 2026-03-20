package com.bhavesh.learn.cacheforge.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.models.media.Schema;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


import java.util.Map;

@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "CacheForge API",
                version = "1.0.0",
                description = "Interactive cache eviction strategy benchmarking API. "
                        + "Simulate and compare LRU, LFU, FIFO, MRU, RANDOM, ARC, and CLOCK "
                        + "strategies across various workload patterns with optional TTL and threading modes.",
                contact = @Contact(name = "Bhavesh Lohana")
        )
)
public class OpenApiConfig {

        @Bean
        public OpenApiCustomizer globalLongFormatCustomizer() {
                return openApi -> {
                        // Iterate through all schemas (DTOs) in your project
                        if (openApi.getComponents() != null && openApi.getComponents().getSchemas() != null) {
                                openApi.getComponents().getSchemas().values().forEach(schema -> {
                                        Map<String, Schema> properties = schema.getProperties();
                                        if (properties != null) {
                                                properties.forEach((name, prop) -> {
                                                        // Check if the property is an integer type (int32 or int64/long)
                                                        // int64 format indicates a long field - set example/default to 0
                                                        if ("int64".equals(prop.getFormat()) || "int32".equals(prop.getFormat())) {
                                                                prop.setExample(0L);
                                                                prop.setDefault(0L);
                                                        }
                                                });
                                        }
                                });
                        }
                };
        }
}
