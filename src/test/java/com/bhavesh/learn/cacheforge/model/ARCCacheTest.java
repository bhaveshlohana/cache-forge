package com.bhavesh.learn.cacheforge.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ARCCacheTest {

    private ARCCache<Integer, String> cache;

    @BeforeEach
    void setUp() {
        cache = new ARCCache<>(3);
    }

    @Test
    void shouldPutAndGet() {
        cache.put(1, "A");
        assertEquals("A", cache.get(1));
    }

    @Test
    void shouldReturnNullForMiss() {
        assertNull(cache.get(99));
        assertEquals(1, cache.getMissCount());
    }

    @Test
    void shouldTrackHitsAndMisses() {
        cache.put(1, "A");
        cache.get(1); // hit — promotes to T2
        cache.get(2); // miss
        assertEquals(1, cache.getHitCount());
        assertEquals(1, cache.getMissCount());
    }

    @Test
    void shouldEvictWhenFull() {
        cache.put(1, "A");
        cache.put(2, "B");
        cache.put(3, "C");
        assertEquals(3, cache.getSize());

        cache.put(4, "D"); // Triggers eviction
        assertEquals(3, cache.getSize());
        assertTrue(cache.getEvictionCount() > 0);
    }

    @Test
    void shouldPromoteToT2OnSecondAccess() {
        cache.put(1, "A"); // T1
        cache.get(1);      // Promote to T2
        cache.put(2, "B"); // T1
        cache.put(3, "C"); // T1

        // Key 1 is in T2 (frequent), so it should survive eviction
        cache.put(4, "D"); // Evicts from T1
        assertEquals("A", cache.get(1)); // Still accessible
    }

    @Test
    void shouldAdaptToFrequencyBias() {
        // Access pattern that should make ARC favor frequency
        cache.put(1, "A");
        cache.get(1); // Promote to T2
        cache.put(2, "B");
        cache.get(2); // Promote to T2
        cache.put(3, "C");
        cache.get(3); // Promote to T2

        // Now cache has 3 items in T2
        cache.put(4, "D"); // Should adapt and handle gracefully
        assertEquals(3, cache.getSize());
    }

    @Test
    void shouldUpdateExistingKeyInT1() {
        cache.put(1, "A");
        cache.put(1, "A2"); // Update should promote to T2
        assertEquals("A2", cache.get(1));
    }

    @Test
    void shouldUpdateExistingKeyInT2() {
        cache.put(1, "A");
        cache.get(1); // Promote to T2
        cache.put(1, "A3"); // Update in T2
        assertEquals("A3", cache.get(1));
    }

    @Test
    void shouldClearAll() {
        cache.put(1, "A");
        cache.put(2, "B");
        cache.get(1);
        cache.clear();
        assertEquals(0, cache.getSize());
        assertNull(cache.get(1));
        assertNull(cache.get(2));
    }

    @Test
    void shouldRemoveFromCache() {
        cache.put(1, "A");
        cache.remove(1);
        assertNull(cache.get(1));
    }

    @Test
    void shouldReportCacheName() {
        assertEquals("ARCCache", cache.getCacheName());
    }

    @Test
    void shouldRejectInvalidCapacity() {
        assertThrows(IllegalArgumentException.class, () -> new ARCCache<>(0));
        assertThrows(IllegalArgumentException.class, () -> new ARCCache<>(-1));
    }

    @Test
    void shouldMaintainCapacityUnderLoad() {
        for (int i = 0; i < 100; i++) {
            cache.put(i, "V" + i);
        }
        assertEquals(3, cache.getSize());
    }

    @Test
    void shouldResetStats() {
        cache.put(1, "A");
        cache.get(1);
        cache.resetStats();
        assertEquals(0, cache.getHitCount());
        assertEquals(0, cache.getMissCount());
        assertEquals(0, cache.getEvictionCount());
    }

    @Test
    void shouldHandleGhostListHits() {
        // Fill cache
        cache.put(1, "A");
        cache.put(2, "B");
        cache.put(3, "C");

        // Evict 1 (should go to B1 ghost list)
        cache.put(4, "D");

        // Re-insert 1 — this should hit B1 ghost and adapt p
        cache.put(1, "A");
        assertNotNull(cache.get(1));
    }
}
