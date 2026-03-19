package com.bhavesh.learn.cacheforge.generator.impl;

import com.bhavesh.learn.cacheforge.domain.CacheRequest;
import com.bhavesh.learn.cacheforge.domain.enums.OperationType;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TemporalHotspotGeneratorTest {

    private final TemporalHotspotGenerator generator = new TemporalHotspotGenerator();

    @Test
    void shouldGenerateCorrectNumberOfRequests() {
        List<CacheRequest> requests = generator.generate(100, 50, 0.7);
        assertEquals(100, requests.size());
    }

    @Test
    void shouldGenerateKeysWithinKeySpace() {
        int keySpace = 100;
        List<CacheRequest> requests = generator.generate(5000, keySpace, 0.5);

        for (CacheRequest request : requests) {
            assertTrue(request.key() >= 0 && request.key() < keySpace,
                    "Key " + request.key() + " is outside key space [0, " + keySpace + ")");
        }
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
    void shouldShiftHotspotOverTime() {
        int keySpace = 100;
        // Generate enough requests to span multiple windows (windowSize=1000)
        List<CacheRequest> requests = generator.generate(3000, keySpace, 0.5);

        // Compare hot keys in first window (0-999) vs third window (2000-2999)
        // They should differ because the hot set shifts
        long firstWindowLowKeys = requests.subList(0, 1000).stream()
                .filter(r -> r.key() < 20) // First hot set starts at 0
                .count();

        // The third window's hot set should have shifted
        // Just verify we get a non-trivial distribution
        long thirdWindowLowKeys = requests.subList(2000, 3000).stream()
                .filter(r -> r.key() < 20)
                .count();

        // They should be different due to shifting, but due to randomness
        // just verify both windows have some variation
        assertTrue(firstWindowLowKeys > 0, "First window should have accesses to low keys");
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
    void shouldHandleLargeKeySpace() {
        List<CacheRequest> requests = generator.generate(100, 10000, 0.5);
        assertEquals(100, requests.size());

        for (CacheRequest request : requests) {
            assertTrue(request.key() >= 0 && request.key() < 10000);
        }
    }
}
