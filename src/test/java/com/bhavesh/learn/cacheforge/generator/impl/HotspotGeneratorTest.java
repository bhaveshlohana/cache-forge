package com.bhavesh.learn.cacheforge.generator.impl;

import com.bhavesh.learn.cacheforge.domain.CacheRequest;
import com.bhavesh.learn.cacheforge.domain.enums.OperationType;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

class HotspotGeneratorTest {

    private final HotspotGenerator generator = new HotspotGenerator();

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
    void shouldShowHotspotBias() {
        int keySpace = 100;
        int hotSetSize = (int) (keySpace * 0.2); // 20 keys are "hot"

        List<CacheRequest> requests = generator.generate(10000, keySpace, 0.5);

        // Count how many accesses go to hot keys [0, hotSetSize)
        long hotKeyAccesses = requests.stream()
                .filter(r -> r.key() >= 0 && r.key() < hotSetSize)
                .count();

        double hotRatio = (double) hotKeyAccesses / requests.size();

        // ~80% of accesses should go to hot set
        assertTrue(hotRatio > 0.6,
                "Hot key access ratio should be roughly 0.8, was " + hotRatio);
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
}
