package com.bhavesh.learn.cacheforge.factory;

import com.bhavesh.learn.cacheforge.domain.SimulationConfig;
import com.bhavesh.learn.cacheforge.domain.LatencyTrackingCache;
import com.bhavesh.learn.cacheforge.domain.TTLCacheDecorator;
import com.bhavesh.learn.cacheforge.domain.enums.CacheStrategy;
import com.bhavesh.learn.cacheforge.model.*;
import org.springframework.stereotype.Component;

/**
 * Factory for creating cache instances based on the configured strategy.
 * Centralizes cache instantiation logic and applies optional decorators
 * (TTL, latency tracking) in a consistent decorator chain.
 */
@Component
public class CacheFactory {

    /**
     * Creates a bare cache instance for the given strategy and capacity.
     * No decorators are applied.
     */
    public <K, V> Cache<K, V> createBaseCache(CacheStrategy strategy, long capacity) {
        @SuppressWarnings("unchecked")
        Cache<K, V> cache = (Cache<K, V>) switch (strategy) {
            case LRU -> new LRUCache<>(capacity);
            case LFU -> new LFUCache<>(capacity);
            case FIFO -> new FIFOCache<>(capacity);
            case MRU -> new MRUCache<>(capacity);
            case RANDOM -> new RandomCache<>(capacity);
            case ARC -> new ARCCache<>(capacity);
            case CLOCK -> new ClockCache<>(capacity);
        };
        return cache;
    }

    /**
     * Creates a fully-decorated cache from a SimulationConfig:
     * <ol>
     *   <li>Base cache from strategy</li>
     *   <li>Optional TTL decorator (when {@code config.ttlEnabled()} is true)</li>
     *   <li>Latency tracking decorator (always applied)</li>
     * </ol>
     */
    public Cache<Integer, String> createCache(SimulationConfig config) {
        Cache<Integer, String> cache = createBaseCache(config.strategy(), config.cacheSize());

        if (config.ttlEnabled()) {
            cache = new TTLCacheDecorator<>(cache, config.ttlDuration(), config.ttlUnit());
        }

        return new LatencyTrackingCache<>(cache);
    }
}
