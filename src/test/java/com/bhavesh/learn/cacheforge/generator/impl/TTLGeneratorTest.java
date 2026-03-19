package com.bhavesh.learn.cacheforge.generator.impl;

import com.bhavesh.learn.cacheforge.domain.CacheRequest;
import com.bhavesh.learn.cacheforge.domain.enums.OperationType;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TTLGeneratorTest {

    private final TTLGenerator generator = new TTLGenerator();

    @Test
    void shouldGenerateCorrectNumberOfRequests() {
        List<CacheRequest> requests = generator.generate(100, 50, 0.7);
        assertEquals(100, requests.size());
    }

    @Test
    void shouldGenerateKeysWithinKeySpace() {
        int keySpace = 10;
        List<CacheRequest> requests = generator.generate(1000, keySpace, 0.5);

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
    void shouldContainOnlyValidOperationTypes() {
        List<CacheRequest> requests = generator.generate(100, 50, 0.5);

        for (CacheRequest request : requests) {
            assertTrue(
                    request.operationType() == OperationType.GET || request.operationType() == OperationType.PUT,
                    "Operation type should be GET or PUT, was " + request.operationType());
        }
    }
}
