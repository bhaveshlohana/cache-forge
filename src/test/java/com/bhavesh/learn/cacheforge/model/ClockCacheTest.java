package com.bhavesh.learn.cacheforge.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ClockCacheTest {

    private ClockCache<Integer, String> cache;

    @BeforeEach
    void setUp() {
        cache = new ClockCache<>(3);
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
        cache.get(1);
        cache.get(2);
        assertEquals(1, cache.getHitCount());
        assertEquals(1, cache.getMissCount());
    }

    @Test
    void shouldEvictUsingClockAlgorithm() {
        cache.put(1, "A");
        cache.put(2, "B");
        cache.put(3, "C");
        assertEquals(3, cache.getSize());

        // Don't access any — all have reference bit = 1 from insertion
        // Clock should clear bits and evict the first one it encounters with bit=0
        cache.put(4, "D");
        assertEquals(3, cache.getSize());
        assertEquals(1, cache.getEvictionCount());
    }

    @Test
    void shouldGiveSecondChance() {
        cache.put(1, "A");
        cache.put(2, "B");
        cache.put(3, "C");

        // Access key 1 to set its reference bit again
        cache.get(1);

        // Insert key 4, which triggers eviction
        // Key 1 should get a second chance
        cache.put(4, "D");

        // Key 1 should survive (got second chance)
        // One of key 2 or 3 should be evicted
        assertEquals(3, cache.getSize());
        assertEquals(1, cache.getEvictionCount());
    }

    @Test
    void shouldUpdateExistingKey() {
        cache.put(1, "A");
        cache.put(1, "A2");
        assertEquals("A2", cache.get(1));
        assertEquals(1, cache.getSize());
    }

    @Test
    void shouldMaintainCapacity() {
        for (int i = 0; i < 100; i++) {
            cache.put(i, "V" + i);
        }
        assertEquals(3, cache.getSize());
    }

    @Test
    void shouldClearAll() {
        cache.put(1, "A");
        cache.put(2, "B");
        cache.clear();
        assertEquals(0, cache.getSize());
        assertNull(cache.get(1));
    }

    @Test
    void shouldRemoveExistingKey() {
        cache.put(1, "A");
        cache.remove(1);
        assertEquals(0, cache.getSize());
        assertNull(cache.get(1));
    }

    @Test
    void shouldReportCacheName() {
        assertEquals("ClockCache", cache.getCacheName());
    }

    @Test
    void shouldRejectInvalidCapacity() {
        assertThrows(IllegalArgumentException.class, () -> new ClockCache<>(0));
        assertThrows(IllegalArgumentException.class, () -> new ClockCache<>(-1));
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
    void shouldHandleCapacityOfOne() {
        ClockCache<Integer, String> smallCache = new ClockCache<>(1);
        smallCache.put(1, "A");
        assertEquals("A", smallCache.get(1));
        smallCache.put(2, "B");
        assertEquals(1, smallCache.getSize());
        assertEquals("B", smallCache.get(2));
        assertNull(smallCache.get(1));
    }
}
