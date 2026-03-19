package com.bhavesh.learn.cacheforge.generator.impl;

import com.bhavesh.learn.cacheforge.domain.CacheRequest;
import com.bhavesh.learn.cacheforge.domain.enums.OperationType;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SequentialGeneratorTest {

    private final SequentialGenerator generator = new SequentialGenerator();

    @Test
    void shouldGenerateCorrectNumberOfRequests() {
        List<CacheRequest> requests = generator.generate(100, 50, 0.7);
        assertEquals(100, requests.size());
    }

    @Test
    void shouldGenerateSequentialKeys() {
        List<CacheRequest> requests = generator.generate(10, 5, 0.0); // All PUTs for predictable keys

        for (int i = 0; i < requests.size(); i++) {
            assertEquals(i % 5, requests.get(i).key(),
                    "Key at index " + i + " should be " + (i % 5));
        }
    }

    @Test
    void shouldWrapAroundKeySpace() {
        int keySpace = 3;
        List<CacheRequest> requests = generator.generate(9, keySpace, 0.0);

        // Expected keys: 0, 1, 2, 0, 1, 2, 0, 1, 2
        assertEquals(0, requests.get(0).key());
        assertEquals(1, requests.get(1).key());
        assertEquals(2, requests.get(2).key());
        assertEquals(0, requests.get(3).key());
        assertEquals(1, requests.get(4).key());
        assertEquals(2, requests.get(5).key());
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
    void shouldGenerateKeysWithinKeySpace() {
        int keySpace = 10;
        List<CacheRequest> requests = generator.generate(1000, keySpace, 0.5);

        for (CacheRequest request : requests) {
            assertTrue(request.key() >= 0 && request.key() < keySpace);
        }
    }
}
