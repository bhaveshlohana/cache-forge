package com.bhavesh.learn.cacheforge.generator.impl;

import com.bhavesh.learn.cacheforge.domain.CacheRequest;
import com.bhavesh.learn.cacheforge.domain.enums.OperationType;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ZipfianGeneratorTest {

    private final ZipfianGenerator generator = new ZipfianGenerator();

    @Test
    void shouldGenerateCorrectNumberOfRequests() {
        List<CacheRequest> requests = generator.generate(100, 50, 0.7);
        assertEquals(100, requests.size());
    }

    @Test
    void shouldGenerateKeysWithinKeySpace() {
        int keySpace = 100;
        List<CacheRequest> requests = generator.generate(1000, keySpace, 0.5);

        for (CacheRequest request : requests) {
            assertTrue(request.key() >= 0 && request.key() < keySpace,
                    "Key " + request.key() + " is outside key space [0, " + keySpace + ")");
        }
    }

    @Test
    void shouldShowSkewTowardLowKeys() {
        int keySpace = 100;
        List<CacheRequest> requests = generator.generate(10000, keySpace, 0.5);

        // Count accesses to the first 10% of keys
        long topKeyAccesses = requests.stream()
                .filter(r -> r.key() < keySpace / 10)
                .count();

        double topRatio = (double) topKeyAccesses / requests.size();

        // Zipfian distribution should heavily favor low-index keys
        assertTrue(topRatio > 0.3,
                "Top 10% keys should have significantly more accesses in Zipfian, ratio was " + topRatio);
    }

    @Test
    void shouldContainBothGetAndPutOperations() {
        List<CacheRequest> requests = generator.generate(1000, 50, 0.5);

        boolean hasGet = requests.stream().anyMatch(r -> r.operationType() == OperationType.GET);
        boolean hasPut = requests.stream().anyMatch(r -> r.operationType() == OperationType.PUT);

        assertTrue(hasGet);
        assertTrue(hasPut);
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

    @Test
    void shouldHandleKeySpaceOfOne() {
        List<CacheRequest> requests = generator.generate(100, 1, 0.5);

        assertEquals(100, requests.size());
        for (CacheRequest request : requests) {
            assertEquals(0, request.key());
        }
    }
}
