package com.bhavesh.learn.cacheforge.exception;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void shouldReturn400ForInvalidSimulationConfig() {
        InvalidSimulationConfigException ex = new InvalidSimulationConfigException("cacheSize must be > 0");
        ResponseEntity<Map<String, Object>> response = handler.handleInvalidConfig(ex);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals(400, response.getBody().get("status"));
        assertEquals("Invalid Simulation Configuration", response.getBody().get("error"));
        assertEquals("cacheSize must be > 0", response.getBody().get("message"));
        assertNotNull(response.getBody().get("timestamp"));
    }

    @Test
    void shouldReturn500ForSimulationException() {
        SimulationException ex = new SimulationException("Thread pool exhausted");
        ResponseEntity<Map<String, Object>> response = handler.handleSimulationError(ex);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals(500, response.getBody().get("status"));
        assertEquals("Simulation Failure", response.getBody().get("error"));
        assertEquals("Thread pool exhausted", response.getBody().get("message"));
    }

    @Test
    void shouldReturn400ForIllegalArgument() {
        IllegalArgumentException ex = new IllegalArgumentException("TTL must be positive");
        ResponseEntity<Map<String, Object>> response = handler.handleIllegalArgument(ex);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Bad Request", response.getBody().get("error"));
    }

    @Test
    void shouldReturn500ForGenericException() {
        Exception ex = new RuntimeException("Something went wrong");
        ResponseEntity<Map<String, Object>> response = handler.handleGenericError(ex);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals("Internal Server Error", response.getBody().get("error"));
    }

    @Test
    void shouldHandleNullMessageInGenericException() {
        Exception ex = new RuntimeException();
        ResponseEntity<Map<String, Object>> response = handler.handleGenericError(ex);

        assertEquals("An unexpected error occurred", response.getBody().get("message"));
    }

    @Test
    void shouldIncludeTimestampInAllResponses() {
        ResponseEntity<Map<String, Object>> response = handler.handleInvalidConfig(
                new InvalidSimulationConfigException("test")
        );
        assertNotNull(response.getBody().get("timestamp"));
        assertTrue(response.getBody().get("timestamp").toString().length() > 0);
    }
}
