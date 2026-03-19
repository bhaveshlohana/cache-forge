package com.bhavesh.learn.cacheforge.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MRUCacheTest {

    private MRUCache<Integer, String> cache;

    @BeforeEach
    void setUp() {
        cache = new MRUCache<>(3);
    }

    // --- Constructor ---

    @Test
    void shouldThrowOnZeroCapacity() {
        assertThrows(IllegalArgumentException.class, () -> new MRUCache<>(0));
    }

    @Test
    void shouldThrowOnNegativeCapacity() {
        assertThrows(IllegalArgumentException.class, () -> new MRUCache<>(-1));
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
    void shouldUpdateExistingKey() {
        cache.put(1, "Apple");
        cache.put(1, "Orange");

        assertEquals("Orange", cache.get(1));
        assertEquals(1, cache.getSize());
    }

    // --- MRU Eviction ---

    @Test
    void shouldEvictMostRecentlyUsedOnOverflow() {
        cache.put(1, "A");
        cache.put(2, "B");
        cache.put(3, "C"); // head: C -> B -> A

        cache.put(4, "D"); // Evicts 3 (MRU = head)

        assertNull(cache.get(3));
        assertEquals("A", cache.get(1));
        assertEquals("B", cache.get(2));
        assertEquals("D", cache.get(4));
        assertEquals(1, cache.getEvictionCount());
    }

    @Test
    void shouldEvictCorrectlyAfterAccess() {
        cache.put(1, "A");
        cache.put(2, "B");
        cache.put(3, "C");

        cache.get(1); // Moves 1 to head, so head is now 1

        cache.put(4, "D"); // Evicts 1 (new MRU = head)

        assertNull(cache.get(1));
        assertEquals("B", cache.get(2));
    }

    @Test
    void shouldEvictMultipleTimesCorrectly() {
        cache.put(1, "A");
        cache.put(2, "B");
        cache.put(3, "C");

        cache.put(4, "D"); // Evicts 3 (MRU)
        cache.put(5, "E"); // Evicts 4 (new MRU)

        assertNull(cache.get(3));
        assertNull(cache.get(4));
        assertEquals(2, cache.getEvictionCount());
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
    }

    // --- Edge Cases ---

    @Test
    void shouldWorkWithCapacityOne() {
        MRUCache<Integer, String> smallCache = new MRUCache<>(1);

        smallCache.put(1, "A");
        assertEquals("A", smallCache.get(1));

        smallCache.put(2, "B"); // Evicts 1 (only entry = MRU)
        assertNull(smallCache.get(1));
        assertEquals("B", smallCache.get(2));
    }

    @Test
    void shouldReturnZeroLatency() {
        assertEquals(0, cache.getTotalLatency());
    }

    @Test
    void shouldReturnCacheName() {
        assertEquals("MRUCache", cache.getCacheName());
    }

    @Test
    void shouldReturnToString() {
        cache.put(1, "A");
        String result = cache.toString();
        assertTrue(result.contains("Cache State"));
    }
}
