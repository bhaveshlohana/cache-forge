package com.bhavesh.learn.cacheforge.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class LRUCacheTest {

    private LRUCache<Integer, String> cache;

    @BeforeEach
    void setUp() {
        cache = new LRUCache<>(3);
    }

    // --- Constructor ---

    @Test
    void shouldThrowOnZeroCapacity() {
        assertThrows(IllegalArgumentException.class, () -> new LRUCache<>(0));
    }

    @Test
    void shouldThrowOnNegativeCapacity() {
        assertThrows(IllegalArgumentException.class, () -> new LRUCache<>(-1));
    }

    @Test
    void shouldCreateEmptyCache() {
        assertEquals(0, cache.getSize());
        assertEquals(3, cache.getCapacity());
        assertEquals(0, cache.getHitCount());
        assertEquals(0, cache.getMissCount());
        assertEquals(0, cache.getEvictionCount());
    }

    // --- Basic Put and Get ---

    @Test
    void shouldPutAndGetSingleEntry() {
        cache.put(1, "Apple");

        assertEquals("Apple", cache.get(1));
        assertEquals(1, cache.getSize());
    }

    @Test
    void shouldReturnNullForMissingKey() {
        assertNull(cache.get(99));
        assertEquals(1, cache.getMissCount());
    }

    @Test
    void shouldUpdateExistingKey() {
        cache.put(1, "Apple");
        cache.put(1, "Orange");

        assertEquals("Orange", cache.get(1));
        assertEquals(1, cache.getSize());
    }

    @Test
    void shouldPutMultipleEntries() {
        cache.put(1, "A");
        cache.put(2, "B");
        cache.put(3, "C");

        assertEquals(3, cache.getSize());
        assertEquals("A", cache.get(1));
        assertEquals("B", cache.get(2));
        assertEquals("C", cache.get(3));
    }

    // --- LRU Eviction ---

    @Test
    void shouldEvictLeastRecentlyUsedOnOverflow() {
        cache.put(1, "A");
        cache.put(2, "B");
        cache.put(3, "C");
        cache.put(4, "D"); // Should evict 1 (LRU)

        assertNull(cache.get(1));
        assertEquals("D", cache.get(4));
        assertEquals(3, cache.getSize());
        assertEquals(1, cache.getEvictionCount());
    }

    @Test
    void shouldEvictCorrectlyAfterAccess() {
        cache.put(1, "A");
        cache.put(2, "B");
        cache.put(3, "C");

        cache.get(1); // Access 1, so 2 becomes LRU

        cache.put(4, "D"); // Should evict 2 (LRU)

        assertNull(cache.get(2));
        assertEquals("A", cache.get(1));
        assertEquals("D", cache.get(4));
    }

    @Test
    void shouldEvictMultipleTimesCorrectly() {
        cache.put(1, "A");
        cache.put(2, "B");
        cache.put(3, "C");
        cache.put(4, "D"); // Evicts 1
        cache.put(5, "E"); // Evicts 2

        assertNull(cache.get(1));
        assertNull(cache.get(2));
        assertEquals("C", cache.get(3));
        assertEquals("D", cache.get(4));
        assertEquals("E", cache.get(5));
        assertEquals(2, cache.getEvictionCount());
    }

    @Test
    void shouldUpdateMoveToHeadPreventingEviction() {
        cache.put(1, "A");
        cache.put(2, "B");
        cache.put(3, "C");

        cache.put(1, "A-updated"); // Updates and moves 1 to head, so 2 is now LRU

        cache.put(4, "D"); // Evicts 2

        assertNull(cache.get(2));
        assertEquals("A-updated", cache.get(1));
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
        cache.remove(99); // No-op

        assertEquals(1, cache.getSize());
    }

    // --- Stats ---

    @Test
    void shouldTrackHitsAndMisses() {
        cache.put(1, "A");

        cache.get(1); // Hit
        cache.get(1); // Hit
        cache.get(2); // Miss

        assertEquals(2, cache.getHitCount());
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
        cache.put(3, "C");

        cache.clear();

        assertEquals(0, cache.getSize());
        assertNull(cache.get(1));
        assertNull(cache.get(2));
        assertNull(cache.get(3));
    }

    @Test
    void shouldClearResetsStats() {
        cache.put(1, "A");
        cache.get(1);
        cache.get(2);
        cache.clear();

        assertEquals(0, cache.getHitCount());
        assertEquals(0, cache.getMissCount());
    }

    // --- Edge Cases ---

    @Test
    void shouldWorkWithCapacityOne() {
        LRUCache<Integer, String> smallCache = new LRUCache<>(1);

        smallCache.put(1, "A");
        assertEquals("A", smallCache.get(1));

        smallCache.put(2, "B"); // Evicts 1
        assertNull(smallCache.get(1));
        assertEquals("B", smallCache.get(2));
        assertEquals(1, smallCache.getEvictionCount());
    }

    @Test
    void shouldReturnZeroLatency() {
        assertEquals(0, cache.getTotalLatency());
    }

    @Test
    void shouldReturnCacheName() {
        assertEquals("LRUCache", cache.getCacheName());
    }

    @Test
    void shouldReturnToString() {
        cache.put(1, "A");
        String result = cache.toString();
        assertTrue(result.contains("Cache State"));
        assertTrue(result.contains("Hits:"));
        assertTrue(result.contains("Misses:"));
    }
}
