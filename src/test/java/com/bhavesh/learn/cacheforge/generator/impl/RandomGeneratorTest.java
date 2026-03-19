package com.bhavesh.learn.cacheforge.generator.impl;

import com.bhavesh.learn.cacheforge.domain.CacheRequest;
import com.bhavesh.learn.cacheforge.domain.enums.OperationType;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class RandomGeneratorTest {

    private final RandomGenerator generator = new RandomGenerator();

    @Test
    void shouldGenerateCorrectNumberOfRequests() {
        List<CacheRequest> requests = generator.generate(100, 50, 0.7);
        assertEquals(100, requests.size());
    }

    @Test
    void shouldGenerateKeysWithinKeySpace() {
        List<CacheRequest> requests = generator.generate(1000, 10, 0.5);

        for (CacheRequest request : requests) {
            assertTrue(request.key() >= 0 && request.key() < 10,
                    "Key " + request.key() + " is outside key space [0, 10)");
        }
    }

    @Test
    void shouldContainBothGetAndPutOperations() {
        List<CacheRequest> requests = generator.generate(1000, 50, 0.5);

        boolean hasGet = requests.stream().anyMatch(r -> r.operationType() == OperationType.GET);
        boolean hasPut = requests.stream().anyMatch(r -> r.operationType() == OperationType.PUT);

        assertTrue(hasGet, "Should have GET operations");
        assertTrue(hasPut, "Should have PUT operations");
    }

    @Test
    void shouldRespectReadWriteRatio_HighRead() {
        List<CacheRequest> requests = generator.generate(10000, 50, 0.9);

        long getCount = requests.stream().filter(r -> r.operationType() == OperationType.GET).count();
        double getRatio = (double) getCount / requests.size();

        // With ratio 0.9, about 90% should be GETs (allow some variance)
        assertTrue(getRatio > 0.8, "GET ratio should be roughly 0.9, was " + getRatio);
    }

    @Test
    void shouldRespectReadWriteRatio_HighWrite() {
        List<CacheRequest> requests = generator.generate(10000, 50, 0.1);

        long putCount = requests.stream().filter(r -> r.operationType() == OperationType.PUT).count();
        double putRatio = (double) putCount / requests.size();

        // With ratio 0.1, about 90% should be PUTs
        assertTrue(putRatio > 0.8, "PUT ratio should be roughly 0.9, was " + putRatio);
    }

    @Test
    void shouldGenerateValuesMatchingKeys() {
        List<CacheRequest> requests = generator.generate(100, 50, 0.5);

        for (CacheRequest request : requests) {
            assertEquals("V" + request.key(), request.value());
        }
    }

    @Test
    void shouldHandleZeroIterations() {
        List<CacheRequest> requests = generator.generate(0, 50, 0.5);
        assertTrue(requests.isEmpty());
    }
}
