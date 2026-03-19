package com.bhavesh.learn.cacheforge.service;

import com.bhavesh.learn.cacheforge.domain.*;
import com.bhavesh.learn.cacheforge.domain.enums.CacheStrategy;
import com.bhavesh.learn.cacheforge.domain.enums.OperationType;
import com.bhavesh.learn.cacheforge.domain.enums.WorkloadPattern;
import com.bhavesh.learn.cacheforge.factory.CacheFactory;
import com.bhavesh.learn.cacheforge.factory.WorkloadGeneratorFactory;
import com.bhavesh.learn.cacheforge.model.*;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class SimulatorServiceTest {

    private SimulatorService simulatorService;
    private WorkloadGeneratorService workloadGeneratorService;
    private MeterRegistry meterRegistry;

    @BeforeEach
    void setUp() {
        simulatorService = new SimulatorService();
        workloadGeneratorService = new WorkloadGeneratorService();
        meterRegistry = new SimpleMeterRegistry();

        // Inject factory dependencies
        CacheFactory cacheFactory = new CacheFactory();
        WorkloadGeneratorFactory generatorFactory = new WorkloadGeneratorFactory();

        simulatorService.cacheFactory = cacheFactory;
        simulatorService.workloadGeneratorService = workloadGeneratorService;
        simulatorService.meterRegistry = meterRegistry;
        workloadGeneratorService.generatorFactory = generatorFactory;
    }

    // --- getCacheBasedOnStrategy ---

    @Test
    void shouldReturnLatencyTrackingLRUCache() {
        SimulationConfig config = new SimulationConfig(CacheStrategy.LRU, 100, WorkloadPattern.random, 10, 5, 0.5);
        Cache<Integer, String> cache = simulatorService.getCacheBasedOnStrategy(config);

        assertNotNull(cache);
        assertEquals("LRUCache", cache.getCacheName());
        assertTrue(cache instanceof LatencyTrackingCache);
    }

    @Test
    void shouldReturnLatencyTrackingLFUCache() {
        SimulationConfig config = new SimulationConfig(CacheStrategy.LFU, 100, WorkloadPattern.random, 10, 5, 0.5);
        Cache<Integer, String> cache = simulatorService.getCacheBasedOnStrategy(config);

        assertNotNull(cache);
        assertEquals("LFUCache", cache.getCacheName());
    }

    @Test
    void shouldReturnLatencyTrackingFIFOCache() {
        SimulationConfig config = new SimulationConfig(CacheStrategy.FIFO, 100, WorkloadPattern.random, 10, 5, 0.5);
        Cache<Integer, String> cache = simulatorService.getCacheBasedOnStrategy(config);

        assertNotNull(cache);
        assertEquals("FIFOCache", cache.getCacheName());
    }

    @Test
    void shouldReturnLatencyTrackingMRUCache() {
        SimulationConfig config = new SimulationConfig(CacheStrategy.MRU, 100, WorkloadPattern.random, 10, 5, 0.5);
        Cache<Integer, String> cache = simulatorService.getCacheBasedOnStrategy(config);

        assertNotNull(cache);
        assertEquals("MRUCache", cache.getCacheName());
    }

    @Test
    void shouldReturnTTLWrappedCacheWhenTTLEnabled() {
        SimulationConfig config = new SimulationConfig(
                CacheStrategy.LRU, 100, WorkloadPattern.random, 10, 5, 0.5,
                true, 5000, java.util.concurrent.TimeUnit.MILLISECONDS
        );
        Cache<Integer, String> cache = simulatorService.getCacheBasedOnStrategy(config);

        assertNotNull(cache);
        assertTrue(cache.getCacheName().contains("TTL"));
    }

    @Test
    void shouldReturnNonTTLCacheWhenTTLDisabled() {
        SimulationConfig config = new SimulationConfig(
                CacheStrategy.LRU, 100, WorkloadPattern.random, 10, 5, 0.5,
                false, 0, java.util.concurrent.TimeUnit.MILLISECONDS
        );
        Cache<Integer, String> cache = simulatorService.getCacheBasedOnStrategy(config);

        assertNotNull(cache);
        assertFalse(cache.getCacheName().contains("TTL"));
        assertEquals("LRUCache", cache.getCacheName());
    }

    @ParameterizedTest
    @EnumSource(CacheStrategy.class)
    void shouldReturnNonNullCacheForAllStrategies(CacheStrategy strategy) {
        SimulationConfig config = new SimulationConfig(strategy, 100, WorkloadPattern.random, 10, 5, 0.5);
        Cache<Integer, String> cache = simulatorService.getCacheBasedOnStrategy(config);

        assertNotNull(cache);
        assertEquals(100, cache.getCapacity());
    }

    // --- getStats ---

    @Test
    void shouldReturnStatsMapWithExpectedKeys() {
        Cache<Integer, String> cache = new LRUCache<>(3);
        cache.put(1, "A");
        cache.get(1); // Hit
        cache.get(2); // Miss

        Map<String, Object> stats = simulatorService.getStats(cache);

        assertTrue(stats.containsKey("Cache Type"));
        assertTrue(stats.containsKey("Capacity"));
        assertTrue(stats.containsKey("Final Size"));
        assertTrue(stats.containsKey("Hit Count"));
        assertTrue(stats.containsKey("Miss Count"));
        assertTrue(stats.containsKey("Eviction Count"));
        assertTrue(stats.containsKey("Total Latency (ms)"));
        assertTrue(stats.containsKey("Average Latency (ms/op)"));
        assertTrue(stats.containsKey("Hit Rate"));
    }

    @Test
    void shouldCalculateHitRateCorrectly() {
        Cache<Integer, String> cache = new LRUCache<>(3);
        cache.put(1, "A");
        cache.get(1); // Hit
        cache.get(2); // Miss

        Map<String, Object> stats = simulatorService.getStats(cache);

        assertEquals(1L, stats.get("Hit Count"));
        assertEquals(1L, stats.get("Miss Count"));
        assertEquals("50.00%", stats.get("Hit Rate"));
    }

    @Test
    void shouldHandleZeroAccessesInStats() {
        Cache<Integer, String> cache = new LRUCache<>(3);

        Map<String, Object> stats = simulatorService.getStats(cache);

        assertEquals("N/A", stats.get("Average Latency (ms/op)"));
        assertEquals("N/A", stats.get("Hit Rate"));
    }

    // --- runSimulationForCache ---

    @Test
    void shouldProcessGetOperations() {
        Cache<Integer, String> cache = new LRUCache<>(3);
        cache.put(1, "A");

        List<CacheRequest> requests = List.of(
                new CacheRequest(OperationType.GET, 1, "A"),
                new CacheRequest(OperationType.GET, 2, "B")
        );

        SimulationConfig config = new SimulationConfig(CacheStrategy.LRU, 3, WorkloadPattern.random, 2, 5, 0.5);
        simulatorService.runSimulationForCache(cache, requests, config);

        assertEquals(1, cache.getHitCount());  // key 1 exists
        assertEquals(1, cache.getMissCount()); // key 2 doesn't exist
    }

    @Test
    void shouldProcessPutOperations() {
        Cache<Integer, String> cache = new LRUCache<>(3);

        List<CacheRequest> requests = List.of(
                new CacheRequest(OperationType.PUT, 1, "A"),
                new CacheRequest(OperationType.PUT, 2, "B")
        );

        SimulationConfig config = new SimulationConfig(CacheStrategy.LRU, 3, WorkloadPattern.random, 2, 5, 0.5);
        simulatorService.runSimulationForCache(cache, requests, config);

        assertEquals(2, cache.getSize());
    }

    // --- runSimulation ---

    @Test
    void shouldRunSimulationAndReturnResults() {
        SimulationRequest request = new SimulationRequest(
                List.of(CacheStrategy.LRU),
                10,
                List.of(WorkloadPattern.random),
                100,
                20,
                0.5
        );

        Map<String, Object> results = simulatorService.runSimulation(request);

        assertNotNull(results);
        assertFalse(results.isEmpty());
        assertTrue(results.containsKey("random-LRU"));
    }

    @Test
    void shouldRunSimulationWithMultipleStrategiesAndPatterns() {
        SimulationRequest request = new SimulationRequest(
                List.of(CacheStrategy.LRU, CacheStrategy.LFU),
                10,
                List.of(WorkloadPattern.random, WorkloadPattern.sequential),
                100,
                20,
                0.5
        );

        Map<String, Object> results = simulatorService.runSimulation(request);

        // 2 patterns x 2 strategies = 4 results
        assertEquals(4, results.size());
        assertTrue(results.containsKey("random-LRU"));
        assertTrue(results.containsKey("random-LFU"));
        assertTrue(results.containsKey("sequential-LRU"));
        assertTrue(results.containsKey("sequential-LFU"));
    }

    // --- registerMetrics ---

    @Test
    void shouldRegisterMetricsWithoutError() {
        Cache<Integer, String> cache = new LRUCache<>(3);
        cache.put(1, "A");
        cache.get(1);

        Map<String, Object> stats = simulatorService.getStats(cache);

        assertDoesNotThrow(() -> simulatorService.registerMetrics("random", "LRU", stats));
    }
}
