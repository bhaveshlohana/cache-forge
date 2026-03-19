package com.bhavesh.learn.cacheforge.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RandomCacheTest {

    private RandomCache<Integer, String> cache;

    @BeforeEach
    void setUp() {
        cache = new RandomCache<>(3);
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
    void shouldEvictWhenFull() {
        cache.put(1, "A");
        cache.put(2, "B");
        cache.put(3, "C");
        assertEquals(3, cache.getSize());

        cache.put(4, "D"); // Triggers random eviction
        assertEquals(3, cache.getSize());
        assertEquals(1, cache.getEvictionCount());
        assertNotNull(cache.get(4));
    }

    @Test
    void shouldUpdateExistingKey() {
        cache.put(1, "A");
        cache.put(1, "A2");
        assertEquals("A2", cache.get(1));
        assertEquals(1, cache.getSize());
        assertEquals(0, cache.getEvictionCount());
    }

    @Test
    void shouldMaintainCapacity() {
        for (int i = 0; i < 100; i++) {
            cache.put(i, "V" + i);
        }
        assertEquals(3, cache.getSize());
        assertEquals(3, cache.getCapacity());
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
        assertEquals("RandomCache", cache.getCacheName());
    }

    @Test
    void shouldRejectInvalidCapacity() {
        assertThrows(IllegalArgumentException.class, () -> new RandomCache<>(0));
        assertThrows(IllegalArgumentException.class, () -> new RandomCache<>(-1));
    }

    @Test
    void shouldResetStats() {
        cache.put(1, "A");
        cache.get(1);
        cache.resetStats();
        assertEquals(0, cache.getHitCount());
        assertEquals(0, cache.getMissCount());
    }
}
