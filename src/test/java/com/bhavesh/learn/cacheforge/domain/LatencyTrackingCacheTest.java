package com.bhavesh.learn.cacheforge.domain;

import com.bhavesh.learn.cacheforge.model.LRUCache;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class LatencyTrackingCacheTest {

    private LatencyTrackingCache<Integer, String> cache;
    private LRUCache<Integer, String> delegate;

    @BeforeEach
    void setUp() {
        delegate = new LRUCache<>(3);
        cache = new LatencyTrackingCache<>(delegate);
    }

    // --- Basic Delegation ---

    @Test
    void shouldDelegatePutAndGet() {
        cache.put(1, "Apple");
        assertEquals("Apple", cache.get(1));
    }

    @Test
    void shouldDelegateRemove() {
        cache.put(1, "Apple");
        cache.remove(1);
        assertNull(cache.get(1));
    }

    @Test
    void shouldDelegateSize() {
        cache.put(1, "A");
        cache.put(2, "B");
        assertEquals(2, cache.getSize());
    }

    @Test
    void shouldDelegateCapacity() {
        assertEquals(3, cache.getCapacity());
    }

    @Test
    void shouldDelegateHitCount() {
        cache.put(1, "A");
        cache.get(1); // Hit
        assertEquals(1, cache.getHitCount());
    }

    @Test
    void shouldDelegateMissCount() {
        cache.get(99); // Miss
        assertEquals(1, cache.getMissCount());
    }

    @Test
    void shouldDelegateEvictionCount() {
        cache.put(1, "A");
        cache.put(2, "B");
        cache.put(3, "C");
        cache.put(4, "D"); // Evicts 1
        assertEquals(1, cache.getEvictionCount());
    }

    // --- Latency Tracking ---

    @Test
    void shouldTrackLatencyOnPut() {
        cache.put(1, "A");
        assertTrue(cache.getTotalLatency() > 0);
    }

    @Test
    void shouldTrackLatencyOnGet() {
        cache.put(1, "A");
        long latencyAfterPut = cache.getTotalLatency();

        cache.get(1);
        assertTrue(cache.getTotalLatency() > latencyAfterPut);
    }

    @Test
    void shouldTrackLatencyOnRemove() {
        cache.put(1, "A");
        long latencyAfterPut = cache.getTotalLatency();

        cache.remove(1);
        assertTrue(cache.getTotalLatency() > latencyAfterPut);
    }

    @Test
    void shouldAccumulateLatency() {
        cache.put(1, "A");
        cache.put(2, "B");
        cache.put(3, "C");
        cache.get(1);
        cache.get(2);

        // After 5 operations, latency should be meaningful
        assertTrue(cache.getTotalLatency() > 0);
    }

    // --- Reset ---

    @Test
    void shouldResetLatencyOnResetStats() {
        cache.put(1, "A");
        cache.get(1);
        assertTrue(cache.getTotalLatency() > 0);

        cache.resetStats();
        assertEquals(0, cache.getTotalLatency());
    }

    @Test
    void shouldClearAndResetLatency() {
        cache.put(1, "A");
        cache.get(1);
        cache.clear();

        assertEquals(0, cache.getTotalLatency());
        assertEquals(0, cache.getSize());
    }

    // --- CacheName ---

    @Test
    void shouldDelegateCacheName() {
        assertEquals("LRUCache", cache.getCacheName());
    }
}
