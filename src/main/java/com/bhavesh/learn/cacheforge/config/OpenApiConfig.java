package com.bhavesh.learn.cacheforge.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.context.annotation.Configuration;

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
}
