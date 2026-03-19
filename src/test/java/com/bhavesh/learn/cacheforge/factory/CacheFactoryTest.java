package com.bhavesh.learn.cacheforge.factory;

import com.bhavesh.learn.cacheforge.domain.SimulationConfig;
import com.bhavesh.learn.cacheforge.domain.enums.CacheStrategy;
import com.bhavesh.learn.cacheforge.domain.enums.WorkloadPattern;
import com.bhavesh.learn.cacheforge.model.Cache;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

class CacheFactoryTest {

    private CacheFactory cacheFactory;

    @BeforeEach
    void setUp() {
        cacheFactory = new CacheFactory();
    }

    @ParameterizedTest
    @EnumSource(CacheStrategy.class)
    void shouldCreateBaseCacheForAllStrategies(CacheStrategy strategy) {
        Cache<Integer, String> cache = cacheFactory.createBaseCache(strategy, 50);

        assertNotNull(cache);
        assertEquals(50, cache.getCapacity());
    }

    @Test
    void shouldCreateLRUCache() {
        Cache<Integer, String> cache = cacheFactory.createBaseCache(CacheStrategy.LRU, 100);
        assertEquals("LRUCache", cache.getCacheName());
    }

    @Test
    void shouldCreateLFUCache() {
        Cache<Integer, String> cache = cacheFactory.createBaseCache(CacheStrategy.LFU, 100);
        assertEquals("LFUCache", cache.getCacheName());
    }

    @Test
    void shouldCreateFIFOCache() {
        Cache<Integer, String> cache = cacheFactory.createBaseCache(CacheStrategy.FIFO, 100);
        assertEquals("FIFOCache", cache.getCacheName());
    }

    @Test
    void shouldCreateMRUCache() {
        Cache<Integer, String> cache = cacheFactory.createBaseCache(CacheStrategy.MRU, 100);
        assertEquals("MRUCache", cache.getCacheName());
    }

    @Test
    void shouldCreateRandomCache() {
        Cache<Integer, String> cache = cacheFactory.createBaseCache(CacheStrategy.RANDOM, 100);
        assertEquals("RandomCache", cache.getCacheName());
    }

    @Test
    void shouldCreateARCCache() {
        Cache<Integer, String> cache = cacheFactory.createBaseCache(CacheStrategy.ARC, 100);
        assertEquals("ARCCache", cache.getCacheName());
    }

    @Test
    void shouldCreateClockCache() {
        Cache<Integer, String> cache = cacheFactory.createBaseCache(CacheStrategy.CLOCK, 100);
        assertEquals("ClockCache", cache.getCacheName());
    }

    @Test
    void shouldCreateCacheWithLatencyTracking() {
        SimulationConfig config = new SimulationConfig(CacheStrategy.LRU, 100, WorkloadPattern.random, 10, 5, 0.5);
        Cache<Integer, String> cache = cacheFactory.createCache(config);

        assertNotNull(cache);
        // Latency tracking wraps the cache, so getCacheName returns the inner cache name
        assertEquals("LRUCache", cache.getCacheName());
    }

    @Test
    void shouldCreateCacheWithTTLAndLatencyTracking() {
        SimulationConfig config = new SimulationConfig(
                CacheStrategy.FIFO, 100, WorkloadPattern.random, 10, 5, 0.5,
                true, 5000, TimeUnit.MILLISECONDS
        );
        Cache<Integer, String> cache = cacheFactory.createCache(config);

        assertNotNull(cache);
        assertTrue(cache.getCacheName().contains("TTL"));
    }

    @Test
    void shouldNotApplyTTLWhenDisabled() {
        SimulationConfig config = new SimulationConfig(
                CacheStrategy.LFU, 100, WorkloadPattern.random, 10, 5, 0.5,
                false, 0, TimeUnit.MILLISECONDS
        );
        Cache<Integer, String> cache = cacheFactory.createCache(config);

        assertNotNull(cache);
        assertFalse(cache.getCacheName().contains("TTL"));
    }
}
