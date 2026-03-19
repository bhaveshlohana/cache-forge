package com.bhavesh.learn.cacheforge.domain;

import com.bhavesh.learn.cacheforge.model.Cache;

import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Thread-safe decorator for any Cache implementation.
 * Uses ReentrantReadWriteLock for fine-grained concurrency:
 * - Read lock for get, getSize, getCapacity, stats
 * - Write lock for put, remove, clear
 */
public class ConcurrentCacheDecorator<K, V> implements Cache<K, V> {

    private final Cache<K, V> delegate;
    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
    private final ReentrantReadWriteLock.ReadLock readLock = lock.readLock();
    private final ReentrantReadWriteLock.WriteLock writeLock = lock.writeLock();

    public ConcurrentCacheDecorator(Cache<K, V> cache) {
        this.delegate = cache;
    }

    @Override
    public V get(K key) {
        // get() modifies stats (hitCount/missCount), so needs write lock
        writeLock.lock();
        try {
            return delegate.get(key);
        } finally {
            writeLock.unlock();
        }
    }

    @Override
    public void put(K key, V value) {
        writeLock.lock();
        try {
            delegate.put(key, value);
        } finally {
            writeLock.unlock();
        }
    }

    @Override
    public void remove(K key) {
        writeLock.lock();
        try {
            delegate.remove(key);
        } finally {
            writeLock.unlock();
        }
    }

    @Override
    public int getSize() {
        readLock.lock();
        try {
            return delegate.getSize();
        } finally {
            readLock.unlock();
        }
    }

    @Override
    public long getCapacity() {
        readLock.lock();
        try {
            return delegate.getCapacity();
        } finally {
            readLock.unlock();
        }
    }

    @Override
    public void clear() {
        writeLock.lock();
        try {
            delegate.clear();
        } finally {
            writeLock.unlock();
        }
    }

    @Override
    public long getHitCount() {
        readLock.lock();
        try {
            return delegate.getHitCount();
        } finally {
            readLock.unlock();
        }
    }

    @Override
    public long getMissCount() {
        readLock.lock();
        try {
            return delegate.getMissCount();
        } finally {
            readLock.unlock();
        }
    }

    @Override
    public long getEvictionCount() {
        readLock.lock();
        try {
            return delegate.getEvictionCount();
        } finally {
            readLock.unlock();
        }
    }

    @Override
    public void resetStats() {
        writeLock.lock();
        try {
            delegate.resetStats();
        } finally {
            writeLock.unlock();
        }
    }

    @Override
    public long getTotalLatency() {
        readLock.lock();
        try {
            return delegate.getTotalLatency();
        } finally {
            readLock.unlock();
        }
    }

    @Override
    public String getCacheName() {
        return delegate.getCacheName();
    }

    @Override
    public String toString() {
        readLock.lock();
        try {
            return delegate.toString();
        } finally {
            readLock.unlock();
        }
    }
}
