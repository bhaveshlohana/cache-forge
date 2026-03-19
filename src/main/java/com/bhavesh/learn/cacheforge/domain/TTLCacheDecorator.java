package com.bhavesh.learn.cacheforge.domain;

import com.bhavesh.learn.cacheforge.model.Cache;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * A decorator that adds TTL (Time-To-Live) expiration to any Cache implementation.
 * <p>
 * Unlike the previous design that required Cache<K, TimedValue<V>> as the delegate,
 * this decorator wraps a standard Cache<K, V> and manages expiration timestamps
 * separately in an internal map. This allows TTL to be applied to ANY cache strategy
 * without changing its type signature.
 */
public class TTLCacheDecorator<K, V> implements Cache<K, V> {
    private final Cache<K, V> delegate;
    private final long ttlMillis;
    private final Map<K, Long> expirationMap;

    private long missCount;
    private long evictionCount;

    public TTLCacheDecorator(Cache<K, V> cache, long ttlDuration, TimeUnit timeUnit) {
        if (ttlDuration <= 0) {
            throw new IllegalArgumentException("TTL must be a positive integer value");
        }
        this.delegate = cache;
        this.ttlMillis = timeUnit.toMillis(ttlDuration);
        this.expirationMap = new ConcurrentHashMap<>();
        this.missCount = 0;
        this.evictionCount = 0;
    }

    @Override
    public V get(K key) {
        Long expiresAt = expirationMap.get(key);
        if (expiresAt != null && System.currentTimeMillis() > expiresAt) {
            // Entry has expired — remove from both the delegate and our map
            delegate.remove(key);
            expirationMap.remove(key);
            this.missCount++;
            this.evictionCount++;
            return null;
        }

        V value = delegate.get(key);
        if (value == null) {
            // Not found in delegate — also clean up our map if stale
            expirationMap.remove(key);
        }
        return value;
    }

    @Override
    public void put(K key, V value) {
        delegate.put(key, value);
        expirationMap.put(key, System.currentTimeMillis() + ttlMillis);
    }

    @Override
    public void remove(K key) {
        delegate.remove(key);
        expirationMap.remove(key);
    }

    @Override
    public int getSize() {
        return delegate.getSize();
    }

    @Override
    public long getCapacity() {
        return delegate.getCapacity();
    }

    @Override
    public void clear() {
        delegate.clear();
        expirationMap.clear();
        resetStats();
    }

    @Override
    public long getHitCount() {
        return delegate.getHitCount() - missCount;
    }

    @Override
    public long getMissCount() {
        return delegate.getMissCount() + missCount;
    }

    @Override
    public long getEvictionCount() {
        return delegate.getEvictionCount() + evictionCount;
    }

    @Override
    public void resetStats() {
        delegate.resetStats();
        this.missCount = 0;
        this.evictionCount = 0;
    }

    @Override
    public long getTotalLatency() {
        return delegate.getTotalLatency();
    }

    @Override
    public String getCacheName() {
        return "TTL-" + delegate.getCacheName();
    }

    public String toString() {
        return delegate.toString();
    }
}
