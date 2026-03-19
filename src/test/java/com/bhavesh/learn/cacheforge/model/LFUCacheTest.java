package com.bhavesh.learn.cacheforge.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class LFUCacheTest {

    private LFUCache<Integer, String> cache;

    @BeforeEach
    void setUp() {
        cache = new LFUCache<>(3);
    }

    // --- Constructor ---

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

    // --- LFU Eviction ---

    @Test
    void shouldEvictLeastFrequentlyUsedOnOverflow() {
        cache.put(1, "A");
        cache.put(2, "B");
        cache.put(3, "C");

        // Access 1 and 2 to increase their frequency
        cache.get(1);
        cache.get(2);

        // Now put 4 -> evicts 3 (least frequently used, freq=1)
        cache.put(4, "D");

        assertNull(cache.get(3));
        assertEquals("A", cache.get(1));
        assertEquals("B", cache.get(2));
        assertEquals("D", cache.get(4));
        assertEquals(1, cache.getEvictionCount());
    }

    @Test
    void shouldEvictByFrequencyThenRecency() {
        cache.put(1, "A");
        cache.put(2, "B");
        cache.put(3, "C");

        // Access all once more (each at freq 2)
        cache.get(1);
        cache.get(2);

        // 3 has freq=1, so it should be evicted
        cache.put(4, "D");

        assertNull(cache.get(3));
    }

    @Test
    void shouldEvictMultipleTimesCorrectly() {
        cache.put(1, "A");
        cache.put(2, "B");
        cache.put(3, "C");

        cache.get(1); // freq 1->2
        cache.get(1); // freq 2->3

        cache.put(4, "D"); // Evicts one with min freq (2 or 3, whichever is LRU among min freq)
        cache.put(5, "E"); // Evicts another

        // 1 should still be present (highest frequency)
        assertEquals("A", cache.get(1));
        assertEquals(2, cache.getEvictionCount());
    }

    @Test
    void shouldUpdateExistingKeyIncreasesFrequency() {
        cache.put(1, "A");
        cache.put(2, "B");
        cache.put(3, "C");

        cache.put(1, "A-updated"); // Update increases frequency of 1

        cache.put(4, "D"); // Should evict 2 or 3 (both freq=1), not 1

        // 1 should still be present
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

    @Test
    void shouldRemoveAllEntries() {
        cache.put(1, "A");
        cache.put(2, "B");

        cache.remove(1);
        cache.remove(2);

        assertEquals(0, cache.getSize());
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
    }

    // --- Edge Cases ---

    @Test
    void shouldWorkWithCapacityOne() {
        LFUCache<Integer, String> smallCache = new LFUCache<>(1);

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
        assertEquals("LFUCache", cache.getCacheName());
    }

    @Test
    void shouldReturnToString() {
        cache.put(1, "A");
        String result = cache.toString();
        assertTrue(result.contains("LFUCache State"));
        assertTrue(result.contains("Min Frequency"));
    }
}
