package com.bhavesh.learn.cacheforge.domain;

import com.bhavesh.learn.cacheforge.domain.enums.CacheStrategy;
import com.bhavesh.learn.cacheforge.domain.enums.WorkloadPattern;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SimulationRequestTest {

    // --- Canonical Constructor ---

    @Test
    void shouldCreateWithCanonicalConstructor() {
        SimulationRequest request = new SimulationRequest(
                List.of(CacheStrategy.LRU),
                100,
                List.of(WorkloadPattern.random),
                1000,
                50,
                0.7
        );

        assertEquals(List.of(CacheStrategy.LRU), request.cacheStrategies());
        assertEquals(100, request.cacheSize());
        assertEquals(List.of(WorkloadPattern.random), request.workloadPatterns());
        assertEquals(1000, request.iterations());
        assertEquals(50, request.keySpaceSize());
        assertEquals(0.7, request.readWriteRatio());
    }

    // --- withPatternAndStrategy ---

    @Test
    void shouldCreateSimulationConfigFromPatternAndStrategy() {
        SimulationRequest request = new SimulationRequest(
                List.of(CacheStrategy.LRU, CacheStrategy.LFU),
                100,
                List.of(WorkloadPattern.random),
                1000,
                50,
                0.7
        );

        SimulationConfig config = request.withPatternAndStrategy(WorkloadPattern.zipfian, CacheStrategy.FIFO);

        assertEquals(CacheStrategy.FIFO, config.strategy());
        assertEquals(100, config.cacheSize());
        assertEquals(WorkloadPattern.zipfian, config.pattern());
        assertEquals(1000, config.iterations());
        assertEquals(50, config.keySpaceSize());
        assertEquals(0.7, config.readWriteRatio());
    }

    // --- Convenience Constructors ---

    @Test
    void shouldCreateFromSimulationCommonParams() {
        SimulationCommonParams params = new SimulationCommonParams(100, 1000, 50, 0.7);

        SimulationRequest request = new SimulationRequest(params);

        assertEquals(List.of(CacheStrategy.values()), request.cacheStrategies());
        assertEquals(100, request.cacheSize());
        assertEquals(List.of(WorkloadPattern.values()), request.workloadPatterns());
        assertEquals(1000, request.iterations());
        assertEquals(50, request.keySpaceSize());
        assertEquals(0.7, request.readWriteRatio());
    }

    @Test
    void shouldCreateFromWorkloadPatternAndCommonParams() {
        SimulationCommonParams params = new SimulationCommonParams(100, 1000, 50, 0.7);

        SimulationRequest request = new SimulationRequest(WorkloadPattern.hotspot, params);

        assertEquals(List.of(CacheStrategy.values()), request.cacheStrategies());
        assertEquals(List.of(WorkloadPattern.hotspot), request.workloadPatterns());
        assertEquals(100, request.cacheSize());
    }

    @Test
    void shouldCreateFromCacheStrategyAndCommonParams() {
        SimulationCommonParams params = new SimulationCommonParams(100, 1000, 50, 0.7);

        SimulationRequest request = new SimulationRequest(CacheStrategy.LFU, params);

        assertEquals(List.of(CacheStrategy.LFU), request.cacheStrategies());
        assertEquals(List.of(WorkloadPattern.values()), request.workloadPatterns());
        assertEquals(100, request.cacheSize());
    }

    // --- SimulationConfig Record ---

    @Test
    void shouldCreateSimulationConfig() {
        SimulationConfig config = new SimulationConfig(
                CacheStrategy.LRU, 100, WorkloadPattern.random, 1000, 50, 0.7
        );

        assertEquals(CacheStrategy.LRU, config.strategy());
        assertEquals(100, config.cacheSize());
        assertEquals(WorkloadPattern.random, config.pattern());
        assertEquals(1000, config.iterations());
        assertEquals(50, config.keySpaceSize());
        assertEquals(0.7, config.readWriteRatio());
    }

    // --- SimulationCommonParams Record ---

    @Test
    void shouldCreateSimulationCommonParams() {
        SimulationCommonParams params = new SimulationCommonParams(100, 1000, 50, 0.7);

        assertEquals(100, params.cacheSize());
        assertEquals(1000, params.iterations());
        assertEquals(50, params.keySpaceSize());
        assertEquals(0.7, params.readWriteRatio());
    }

    // --- CacheRequest Record ---

    @Test
    void shouldCreateCacheRequest() {
        CacheRequest request = new CacheRequest(
                com.bhavesh.learn.cacheforge.domain.enums.OperationType.PUT, 1, "value"
        );

        assertEquals(com.bhavesh.learn.cacheforge.domain.enums.OperationType.PUT, request.operationType());
        assertEquals(1, request.key());
        assertEquals("value", request.value());
    }
}
