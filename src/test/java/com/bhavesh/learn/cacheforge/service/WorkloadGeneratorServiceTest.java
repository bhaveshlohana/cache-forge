package com.bhavesh.learn.cacheforge.service;

import com.bhavesh.learn.cacheforge.domain.CacheRequest;
import com.bhavesh.learn.cacheforge.domain.SimulationConfig;
import com.bhavesh.learn.cacheforge.domain.enums.CacheStrategy;
import com.bhavesh.learn.cacheforge.domain.enums.WorkloadPattern;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class WorkloadGeneratorServiceTest {

    private WorkloadGeneratorService service;

    @BeforeEach
    void setUp() {
        service = new WorkloadGeneratorService();
    }

    @ParameterizedTest
    @EnumSource(WorkloadPattern.class)
    void shouldGenerateRequestsForEachPattern(WorkloadPattern pattern) {
        SimulationConfig config = new SimulationConfig(
                CacheStrategy.LRU, 100, pattern, 50, 20, 0.7
        );

        List<CacheRequest> requests = service.generateCacheRequest(config);

        assertNotNull(requests);
        assertEquals(50, requests.size());
    }

    @Test
    void shouldGenerateCorrectNumberOfIterations() {
        SimulationConfig config = new SimulationConfig(
                CacheStrategy.LRU, 100, WorkloadPattern.random, 200, 50, 0.5
        );

        List<CacheRequest> requests = service.generateCacheRequest(config);

        assertEquals(200, requests.size());
    }

    @Test
    void shouldPassKeySpaceToGenerator() {
        int keySpace = 10;
        SimulationConfig config = new SimulationConfig(
                CacheStrategy.LRU, 100, WorkloadPattern.random, 1000, keySpace, 0.5
        );

        List<CacheRequest> requests = service.generateCacheRequest(config);

        for (CacheRequest request : requests) {
            assertTrue(request.key() >= 0 && request.key() < keySpace);
        }
    }

    @Test
    void shouldUseRandomGeneratorForRandomPattern() {
        SimulationConfig config = new SimulationConfig(
                CacheStrategy.LRU, 100, WorkloadPattern.random, 100, 50, 0.5
        );

        List<CacheRequest> requests = service.generateCacheRequest(config);

        assertNotNull(requests);
        assertFalse(requests.isEmpty());
    }

    @Test
    void shouldUseSequentialGeneratorForSequentialPattern() {
        SimulationConfig config = new SimulationConfig(
                CacheStrategy.LRU, 100, WorkloadPattern.sequential, 10, 5, 0.0
        );

        List<CacheRequest> requests = service.generateCacheRequest(config);

        // Sequential keys: 0, 1, 2, 3, 4, 0, 1, 2, 3, 4
        for (int i = 0; i < requests.size(); i++) {
            assertEquals(i % 5, requests.get(i).key());
        }
    }

    @Test
    void shouldUseZipfianGeneratorForZipfianPattern() {
        SimulationConfig config = new SimulationConfig(
                CacheStrategy.LRU, 100, WorkloadPattern.zipfian, 100, 50, 0.5
        );

        List<CacheRequest> requests = service.generateCacheRequest(config);

        assertNotNull(requests);
        assertEquals(100, requests.size());
    }

    @Test
    void shouldUseTTLGeneratorForTTLPattern() {
        SimulationConfig config = new SimulationConfig(
                CacheStrategy.LRU, 100, WorkloadPattern.ttl, 100, 50, 0.5
        );

        List<CacheRequest> requests = service.generateCacheRequest(config);

        assertNotNull(requests);
        assertEquals(100, requests.size());
    }
}
