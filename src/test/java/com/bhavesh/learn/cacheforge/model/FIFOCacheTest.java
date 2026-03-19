package com.bhavesh.learn.cacheforge.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class FIFOCacheTest {

    private FIFOCache<Integer, String> cache;

    @BeforeEach
    void setUp() {
        cache = new FIFOCache<>(3);
    }

    // --- Constructor ---

    @Test
    void shouldThrowOnZeroCapacity() {
        assertThrows(IllegalArgumentException.class, () -> new FIFOCache<>(0));
    }

    @Test
    void shouldThrowOnNegativeCapacity() {
        assertThrows(IllegalArgumentException.class, () -> new FIFOCache<>(-1));
    }

    @Test
    void shouldCreateEmptyCache() {
        assertEquals(0, cache.getSize());
        assertEquals(3, cache.getCapacity());
    }

    // --- Basic Put and Get ---

    @Test
    void shouldPutAndGetSingleEntry() {
        cache.put(1, "Apple");
        assertEquals("Apple", cache.get(1));
    }

    @Test
    void shouldReturnNullForMissingKey() {
        assertNull(cache.get(99));
        assertEquals(1, cache.getMissCount());
    }

    @Test
    void shouldUpdateExistingKeyWithoutChangingPosition() {
        cache.put(1, "A");
        cache.put(2, "B");
        cache.put(3, "C");

        cache.put(1, "A-updated"); // Update existing, no position change

        // Add new entry, should evict 1 (FIFO) since 1 was inserted first
        cache.put(4, "D");

        assertNull(cache.get(1)); // 1 was first in, so evicted
        assertEquals("B", cache.get(2));
    }

    // --- FIFO Eviction ---

    @Test
    void shouldEvictFirstInsertedOnOverflow() {
        cache.put(1, "A");
        cache.put(2, "B");
        cache.put(3, "C");

        cache.put(4, "D"); // Evicts 1 (first in)

        assertNull(cache.get(1));
        assertEquals("B", cache.get(2));
        assertEquals("C", cache.get(3));
        assertEquals("D", cache.get(4));
        assertEquals(1, cache.getEvictionCount());
    }

    @Test
    void shouldEvictInFIFOOrder() {
        cache.put(1, "A");
        cache.put(2, "B");
        cache.put(3, "C");

        cache.put(4, "D"); // Evicts 1
        cache.put(5, "E"); // Evicts 2
        cache.put(6, "F"); // Evicts 3

        assertNull(cache.get(1));
        assertNull(cache.get(2));
        assertNull(cache.get(3));
        assertEquals("D", cache.get(4));
        assertEquals("E", cache.get(5));
        assertEquals("F", cache.get(6));
        assertEquals(3, cache.getEvictionCount());
    }

    @Test
    void shouldNotChangeOrderOnGet() {
        cache.put(1, "A");
        cache.put(2, "B");
        cache.put(3, "C");

        cache.get(1); // Access 1, but FIFO doesn't change order

        cache.put(4, "D"); // Still evicts 1 (first in, despite recent access)

        assertNull(cache.get(1));
    }

    // --- Remove ---

    @Test
    void shouldRemoveExistingKey() {
        cache.put(1, "A");
        cache.put(2, "B");

        cache.remove(1);

        assertNull(cache.get(1));
        assertEquals(1, cache.getSize());
    }

    @Test
    void shouldHandleRemoveNonExistentKey() {
        cache.put(1, "A");
        cache.remove(99);
        assertEquals(1, cache.getSize());
    }

    // --- Stats ---

    @Test
    void shouldTrackHitsAndMisses() {
        cache.put(1, "A");
        cache.get(1); // Hit
        cache.get(2); // Miss

        assertEquals(1, cache.getHitCount());
        assertEquals(1, cache.getMissCount());
    }

    @Test
    void shouldResetStats() {
        cache.put(1, "A");
        cache.get(1);
        cache.get(2);
        cache.resetStats();

        assertEquals(0, cache.getHitCount());
        assertEquals(0, cache.getMissCount());
        assertEquals(0, cache.getEvictionCount());
    }

    // --- Clear ---

    @Test
    void shouldClearAllEntries() {
        cache.put(1, "A");
        cache.put(2, "B");
        cache.clear();

        assertEquals(0, cache.getSize());
        assertNull(cache.get(1));
    }

    // --- Edge Cases ---

    @Test
    void shouldWorkWithCapacityOne() {
        FIFOCache<Integer, String> smallCache = new FIFOCache<>(1);

        smallCache.put(1, "A");
        assertEquals("A", smallCache.get(1));

        smallCache.put(2, "B"); // Evicts 1
        assertNull(smallCache.get(1));
        assertEquals("B", smallCache.get(2));
    }

    @Test
    void shouldReturnZeroLatency() {
        assertEquals(0, cache.getTotalLatency());
    }

    @Test
    void shouldReturnCacheName() {
        assertEquals("FIFOCache", cache.getCacheName());
    }

    @Test
    void shouldReturnToString() {
        cache.put(1, "A");
        String result = cache.toString();
        assertTrue(result.contains("Cache State"));
    }
}
