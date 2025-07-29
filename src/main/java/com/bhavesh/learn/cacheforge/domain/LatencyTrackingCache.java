package com.bhavesh.learn.cacheforge.domain;

import com.bhavesh.learn.cacheforge.model.Cache;

public class LatencyTrackingCache<K, V> implements Cache<K, V> {
    private final Cache<K, V> delegate;
    private long totalLatency;

    public LatencyTrackingCache(Cache<K, V> cache) {
        this.delegate = cache;
    }

    @Override
    public V get(K key) {
        long start = System.nanoTime();
        V result = delegate.get(key);
        totalLatency += System.nanoTime() - start;
        return result;
    }

    @Override
    public void put(K key, V value) {
        long start = System.nanoTime();
        delegate.put(key, value);
        totalLatency += System.nanoTime() - start;
    }

    @Override
    public void remove(K key) {
        long start = System.nanoTime();
        delegate.remove(key);
        totalLatency += System.nanoTime() - start;
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
        return delegate.getHitCount();
    }

    @Override
    public long getMissCount() {
        return delegate.getMissCount();
    }

    @Override
    public long getEvictionCount() {
        return delegate.getEvictionCount();
    }

    @Override
    public void resetStats() {
        totalLatency=0;
    }

    @Override
    public long getTotalLatency() {
        return totalLatency;
    }

    @Override
    public String getCacheName() {
        return delegate.getCacheName();
    }
}
