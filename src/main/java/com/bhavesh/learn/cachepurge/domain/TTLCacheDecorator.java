package com.bhavesh.learn.cachepurge.domain;

import com.bhavesh.learn.cachepurge.model.Cache;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class TTLCacheDecorator<K, V> implements Cache<K, V> {
    private final Cache<K, TimedValue<V>> delegate;
    private final long ttlMillis;


    private long missCount;
    private long evictionCount;


    public TTLCacheDecorator(Cache<K, TimedValue<V>> cache, long ttlMillis, TimeUnit timeUnit) {
        if (ttlMillis <= 0) {
            throw new IllegalArgumentException("TTL must be a positive integer value");
        }
        this.delegate = cache;
        this.ttlMillis = timeUnit.toMillis(ttlMillis);
        this.missCount = 0;
        this.evictionCount = 0;
    }

    @Override
    public V get(K key) {
        TimedValue<V> timedValue = delegate.get(key);
        if (timedValue == null) {
            return null; // Return null if the value is not present
        }
        if (timedValue.isExpired()) {
            delegate.remove(key);
            this.missCount++;
            this.evictionCount++;
            return null; // Return null if the value is not present or has expired
        }
        return timedValue.getValue(); // Return the valid TimedValue
    }

    @Override
    public void put(K key, V value) {
        delegate.put(key, new TimedValue<>(value, ttlMillis));
    }

    @Override
    public void remove(K key) {
        delegate.remove(key);
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
        resetStats();
    }

    @Override
    public long getHitCount() {
        return delegate.getHitCount() - missCount; // Adjust hit count to exclude expired entries
    }

    @Override
    public long getMissCount() {
        return delegate.getMissCount() + missCount; // Include misses from expired entries
    }

    @Override
    public long getEvictionCount() {
        return delegate.getEvictionCount() + evictionCount; // Include evictions from expired entries
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
