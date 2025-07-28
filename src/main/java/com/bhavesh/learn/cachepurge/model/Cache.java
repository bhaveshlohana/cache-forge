package com.bhavesh.learn.cachepurge.model;

public interface Cache<K, V> {
    V get(K key);

    void put(K key, V value);

    void remove(K key);

    int getSize();

    long getCapacity();

    void clear();

    long getHitCount();

    long getMissCount();

    long getEvictionCount();

    void resetStats();

    long getTotalLatency();

    String getCacheName();
}
