package com.bhavesh.learn.cacheforge.domain;

import com.bhavesh.learn.cacheforge.model.LRUCache;
import org.junit.jupiter.api.Test;

import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

class TTLCacheDecoratorTest {

    // --- Constructor ---

    @Test
    void shouldThrowOnZeroTTL() {
        LRUCache<Integer, String> delegate = new LRUCache<>(3);
        assertThrows(IllegalArgumentException.class,
                () -> new TTLCacheDecorator<>(delegate, 0, TimeUnit.MILLISECONDS));
    }

    @Test
    void shouldThrowOnNegativeTTL() {
        LRUCache<Integer, String> delegate = new LRUCache<>(3);
        assertThrows(IllegalArgumentException.class,
                () -> new TTLCacheDecorator<>(delegate, -5, TimeUnit.MILLISECONDS));
    }

    // --- Basic Put and Get ---

    @Test
    void shouldPutAndGetBeforeExpiry() {
        LRUCache<Integer, String> delegate = new LRUCache<>(3);
        TTLCacheDecorator<Integer, String> cache = new TTLCacheDecorator<>(delegate, 10, TimeUnit.SECONDS);

        cache.put(1, "Apple");
        assertEquals("Apple", cache.get(1));
    }

    @Test
    void shouldReturnNullForMissingKey() {
        LRUCache<Integer, String> delegate = new LRUCache<>(3);
        TTLCacheDecorator<Integer, String> cache = new TTLCacheDecorator<>(delegate, 10, TimeUnit.SECONDS);

        assertNull(cache.get(99));
    }

    // --- TTL Expiry ---

    @Test
    void shouldReturnNullAfterTTLExpires() throws InterruptedException {
        LRUCache<Integer, String> delegate = new LRUCache<>(3);
        TTLCacheDecorator<Integer, String> cache = new TTLCacheDecorator<>(delegate, 50, TimeUnit.MILLISECONDS);

        cache.put(1, "Apple");
        assertEquals("Apple", cache.get(1));

        Thread.sleep(100); // Wait for TTL to expire

        assertNull(cache.get(1));
    }

    @Test
    void shouldTrackMissCountForExpiredEntries() throws InterruptedException {
        LRUCache<Integer, String> delegate = new LRUCache<>(3);
        TTLCacheDecorator<Integer, String> cache = new TTLCacheDecorator<>(delegate, 50, TimeUnit.MILLISECONDS);

        cache.put(1, "Apple");
        cache.get(1); // Hit

        Thread.sleep(100);

        cache.get(1); // Should be a miss now (expired)

        // getMissCount includes expired entries
        assertTrue(cache.getMissCount() > 0);
    }

    @Test
    void shouldTrackEvictionCountForExpiredEntries() throws InterruptedException {
        LRUCache<Integer, String> delegate = new LRUCache<>(3);
        TTLCacheDecorator<Integer, String> cache = new TTLCacheDecorator<>(delegate, 50, TimeUnit.MILLISECONDS);

        cache.put(1, "Apple");

        Thread.sleep(100);

        cache.get(1); // Triggers TTL eviction

        assertTrue(cache.getEvictionCount() > 0);
    }

    // --- Delegation ---

    @Test
    void shouldDelegateSize() {
        LRUCache<Integer, String> delegate = new LRUCache<>(3);
        TTLCacheDecorator<Integer, String> cache = new TTLCacheDecorator<>(delegate, 10, TimeUnit.SECONDS);

        cache.put(1, "A");
        cache.put(2, "B");

        assertEquals(2, cache.getSize());
    }

    @Test
    void shouldDelegateCapacity() {
        LRUCache<Integer, String> delegate = new LRUCache<>(5);
        TTLCacheDecorator<Integer, String> cache = new TTLCacheDecorator<>(delegate, 10, TimeUnit.SECONDS);

        assertEquals(5, cache.getCapacity());
    }

    @Test
    void shouldDelegateRemove() {
        LRUCache<Integer, String> delegate = new LRUCache<>(3);
        TTLCacheDecorator<Integer, String> cache = new TTLCacheDecorator<>(delegate, 10, TimeUnit.SECONDS);

        cache.put(1, "A");
        cache.remove(1);

        assertNull(cache.get(1));
    }

    // --- Clear and Reset ---

    @Test
    void shouldClearAndResetStats() {
        LRUCache<Integer, String> delegate = new LRUCache<>(3);
        TTLCacheDecorator<Integer, String> cache = new TTLCacheDecorator<>(delegate, 10, TimeUnit.SECONDS);

        cache.put(1, "A");
        cache.put(2, "B");
        cache.clear();

        assertEquals(0, cache.getSize());
        assertEquals(0, cache.getHitCount());
        assertEquals(0, cache.getMissCount());
    }

    @Test
    void shouldResetStats() {
        LRUCache<Integer, String> delegate = new LRUCache<>(3);
        TTLCacheDecorator<Integer, String> cache = new TTLCacheDecorator<>(delegate, 10, TimeUnit.SECONDS);

        cache.put(1, "A");
        cache.get(1);
        cache.resetStats();

        assertEquals(0, cache.getHitCount());
        assertEquals(0, cache.getMissCount());
        assertEquals(0, cache.getEvictionCount());
    }

    // --- CacheName ---

    @Test
    void shouldReturnTTLPrefixedCacheName() {
        LRUCache<Integer, String> delegate = new LRUCache<>(3);
        TTLCacheDecorator<Integer, String> cache = new TTLCacheDecorator<>(delegate, 10, TimeUnit.SECONDS);

        assertEquals("TTL-LRUCache", cache.getCacheName());
    }

    @Test
    void shouldDelegateTotalLatency() {
        LRUCache<Integer, String> delegate = new LRUCache<>(3);
        TTLCacheDecorator<Integer, String> cache = new TTLCacheDecorator<>(delegate, 10, TimeUnit.SECONDS);

        assertEquals(0, cache.getTotalLatency());
    }

    @Test
    void shouldReturnToString() {
        LRUCache<Integer, String> delegate = new LRUCache<>(3);
        TTLCacheDecorator<Integer, String> cache = new TTLCacheDecorator<>(delegate, 10, TimeUnit.SECONDS);

        cache.put(1, "A");
        assertNotNull(cache.toString());
    }
}
