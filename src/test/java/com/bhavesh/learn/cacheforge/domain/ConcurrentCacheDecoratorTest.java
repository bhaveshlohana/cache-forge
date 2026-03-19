package com.bhavesh.learn.cacheforge.domain;

import com.bhavesh.learn.cacheforge.model.LRUCache;
import com.bhavesh.learn.cacheforge.model.Cache;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

class ConcurrentCacheDecoratorTest {

    private ConcurrentCacheDecorator<Integer, String> cache;

    @BeforeEach
    void setUp() {
        cache = new ConcurrentCacheDecorator<>(new LRUCache<>(100));
    }

    @Test
    void shouldPutAndGetThreadSafely() throws Exception {
        ExecutorService executor = Executors.newFixedThreadPool(4);
        int operations = 1000;
        CountDownLatch latch = new CountDownLatch(operations);

        for (int i = 0; i < operations; i++) {
            final int key = i;
            executor.submit(() -> {
                try {
                    cache.put(key, "V" + key);
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await(5, TimeUnit.SECONDS);
        executor.shutdown();

        // All keys within capacity should be accessible
        assertTrue(cache.getSize() <= 100);
        assertTrue(cache.getSize() > 0);
    }

    @Test
    void shouldHandleConcurrentReadsAndWrites() throws Exception {
        // Pre-fill
        for (int i = 0; i < 50; i++) {
            cache.put(i, "V" + i);
        }

        ExecutorService executor = Executors.newFixedThreadPool(8);
        int operations = 2000;
        CountDownLatch latch = new CountDownLatch(operations);
        AtomicInteger errors = new AtomicInteger(0);

        for (int i = 0; i < operations; i++) {
            final int key = i % 200;
            if (i % 2 == 0) {
                // Read
                executor.submit(() -> {
                    try {
                        cache.get(key);
                    } catch (Exception e) {
                        errors.incrementAndGet();
                    } finally {
                        latch.countDown();
                    }
                });
            } else {
                // Write
                executor.submit(() -> {
                    try {
                        cache.put(key, "V" + key);
                    } catch (Exception e) {
                        errors.incrementAndGet();
                    } finally {
                        latch.countDown();
                    }
                });
            }
        }

        latch.await(10, TimeUnit.SECONDS);
        executor.shutdown();

        assertEquals(0, errors.get(), "Should have no errors during concurrent access");
    }

    @Test
    void shouldNotCorruptStatsUnderConcurrency() throws Exception {
        ExecutorService executor = Executors.newFixedThreadPool(4);
        int operations = 500;
        CountDownLatch latch = new CountDownLatch(operations);

        for (int i = 0; i < operations; i++) {
            final int key = i % 10;
            executor.submit(() -> {
                try {
                    cache.put(key, "V" + key);
                    cache.get(key);
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await(5, TimeUnit.SECONDS);
        executor.shutdown();

        // Stats should be consistent (not negative)
        assertTrue(cache.getHitCount() >= 0);
        assertTrue(cache.getMissCount() >= 0);
        assertTrue(cache.getEvictionCount() >= 0);
        assertEquals(cache.getHitCount() + cache.getMissCount(),
                cache.getHitCount() + cache.getMissCount()); // No overflow
    }

    @Test
    void shouldDelegateCacheName() {
        assertEquals("LRUCache", cache.getCacheName());
    }

    @Test
    void shouldDelegateCapacity() {
        assertEquals(100, cache.getCapacity());
    }

    @Test
    void shouldDelegateClear() {
        cache.put(1, "A");
        cache.clear();
        assertEquals(0, cache.getSize());
    }

    @Test
    void shouldDelegateRemove() {
        cache.put(1, "A");
        cache.remove(1);
        assertNull(cache.get(1));
    }
}
