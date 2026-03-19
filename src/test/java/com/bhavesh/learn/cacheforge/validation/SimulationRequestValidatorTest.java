package com.bhavesh.learn.cacheforge.validation;

import com.bhavesh.learn.cacheforge.domain.SimulationCommonParams;
import com.bhavesh.learn.cacheforge.domain.SimulationRequest;
import com.bhavesh.learn.cacheforge.domain.enums.CacheStrategy;
import com.bhavesh.learn.cacheforge.domain.enums.WorkloadPattern;
import com.bhavesh.learn.cacheforge.exception.InvalidSimulationConfigException;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

class SimulationRequestValidatorTest {

    // --- Valid inputs ---

    @Test
    void shouldPassForValidSimulationRequest() {
        SimulationRequest request = new SimulationRequest(
                List.of(CacheStrategy.LRU), 100, List.of(WorkloadPattern.random),
                1000, 200, 0.7
        );
        assertDoesNotThrow(() -> SimulationRequestValidator.validate(request));
    }

    @Test
    void shouldPassForValidCommonParams() {
        SimulationCommonParams params = new SimulationCommonParams(100, 1000, 200, 0.7);
        assertDoesNotThrow(() -> SimulationRequestValidator.validate(params));
    }

    @Test
    void shouldPassForValidTTLRequest() {
        SimulationRequest request = new SimulationRequest(
                List.of(CacheStrategy.LRU), 100, List.of(WorkloadPattern.random),
                1000, 200, 0.7,
                true, 5000, TimeUnit.MILLISECONDS
        );
        assertDoesNotThrow(() -> SimulationRequestValidator.validate(request));
    }

    // --- Invalid cacheSize ---

    @Test
    void shouldFailForZeroCacheSize() {
        SimulationRequest request = new SimulationRequest(
                List.of(CacheStrategy.LRU), 0, List.of(WorkloadPattern.random),
                1000, 200, 0.7
        );
        InvalidSimulationConfigException ex = assertThrows(
                InvalidSimulationConfigException.class,
                () -> SimulationRequestValidator.validate(request)
        );
        assertTrue(ex.getMessage().contains("cacheSize"));
    }

    @Test
    void shouldFailForNegativeCacheSize() {
        SimulationRequest request = new SimulationRequest(
                List.of(CacheStrategy.LRU), -10, List.of(WorkloadPattern.random),
                1000, 200, 0.7
        );
        assertThrows(InvalidSimulationConfigException.class,
                () -> SimulationRequestValidator.validate(request));
    }

    // --- Invalid iterations ---

    @Test
    void shouldFailForZeroIterations() {
        SimulationRequest request = new SimulationRequest(
                List.of(CacheStrategy.LRU), 100, List.of(WorkloadPattern.random),
                0, 200, 0.7
        );
        InvalidSimulationConfigException ex = assertThrows(
                InvalidSimulationConfigException.class,
                () -> SimulationRequestValidator.validate(request)
        );
        assertTrue(ex.getMessage().contains("iterations"));
    }

    // --- Invalid keySpaceSize ---

    @Test
    void shouldFailForZeroKeySpaceSize() {
        SimulationRequest request = new SimulationRequest(
                List.of(CacheStrategy.LRU), 100, List.of(WorkloadPattern.random),
                1000, 0, 0.7
        );
        assertThrows(InvalidSimulationConfigException.class,
                () -> SimulationRequestValidator.validate(request));
    }

    // --- Invalid readWriteRatio ---

    @Test
    void shouldFailForNegativeReadWriteRatio() {
        SimulationRequest request = new SimulationRequest(
                List.of(CacheStrategy.LRU), 100, List.of(WorkloadPattern.random),
                1000, 200, -0.1
        );
        assertThrows(InvalidSimulationConfigException.class,
                () -> SimulationRequestValidator.validate(request));
    }

    @Test
    void shouldFailForReadWriteRatioAboveOne() {
        SimulationRequest request = new SimulationRequest(
                List.of(CacheStrategy.LRU), 100, List.of(WorkloadPattern.random),
                1000, 200, 1.5
        );
        assertThrows(InvalidSimulationConfigException.class,
                () -> SimulationRequestValidator.validate(request));
    }

    // --- Empty strategies/patterns ---

    @Test
    void shouldFailForEmptyStrategies() {
        SimulationRequest request = new SimulationRequest(
                List.of(), 100, List.of(WorkloadPattern.random),
                1000, 200, 0.7
        );
        InvalidSimulationConfigException ex = assertThrows(
                InvalidSimulationConfigException.class,
                () -> SimulationRequestValidator.validate(request)
        );
        assertTrue(ex.getMessage().contains("strategy"));
    }

    @Test
    void shouldFailForEmptyPatterns() {
        SimulationRequest request = new SimulationRequest(
                List.of(CacheStrategy.LRU), 100, List.of(),
                1000, 200, 0.7
        );
        assertThrows(InvalidSimulationConfigException.class,
                () -> SimulationRequestValidator.validate(request));
    }

    // --- TTL validation ---

    @Test
    void shouldFailForTTLEnabledWithZeroDuration() {
        SimulationRequest request = new SimulationRequest(
                List.of(CacheStrategy.LRU), 100, List.of(WorkloadPattern.random),
                1000, 200, 0.7,
                true, 0, TimeUnit.MILLISECONDS
        );
        InvalidSimulationConfigException ex = assertThrows(
                InvalidSimulationConfigException.class,
                () -> SimulationRequestValidator.validate(request)
        );
        assertTrue(ex.getMessage().contains("TTL duration"));
    }

    // --- Multiple errors ---

    @Test
    void shouldCollectMultipleErrors() {
        SimulationRequest request = new SimulationRequest(
                List.of(), -1, List.of(),
                0, 0, 2.0
        );
        InvalidSimulationConfigException ex = assertThrows(
                InvalidSimulationConfigException.class,
                () -> SimulationRequestValidator.validate(request)
        );
        String msg = ex.getMessage();
        assertTrue(msg.contains("cacheSize"));
        assertTrue(msg.contains("iterations"));
        assertTrue(msg.contains("keySpaceSize"));
        assertTrue(msg.contains("readWriteRatio"));
        assertTrue(msg.contains("strategy"));
        assertTrue(msg.contains("pattern"));
    }

    // --- CommonParams validation ---

    @Test
    void shouldFailCommonParamsForInvalidValues() {
        SimulationCommonParams params = new SimulationCommonParams(0, -1, 0, 1.5);
        assertThrows(InvalidSimulationConfigException.class,
                () -> SimulationRequestValidator.validate(params));
    }

    @Test
    void shouldFailCommonParamsForTTLEnabledWithZeroDuration() {
        SimulationCommonParams params = new SimulationCommonParams(100, 1000, 200, 0.7, true, 0, TimeUnit.MILLISECONDS);
        assertThrows(InvalidSimulationConfigException.class,
                () -> SimulationRequestValidator.validate(params));
    }
}
