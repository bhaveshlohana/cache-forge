package com.bhavesh.learn.cacheforge.model;

import java.util.*;

public class RandomCache<K, V> implements Cache<K, V> {
    private final Map<K, V> cacheMap;
    private final List<K> keys;
    private final Random random;
    private final long capacity;
    private long hitCount;
    private long missCount;
    private long evictionCount;

    public RandomCache(long capacity) {
        if (capacity <= 0) {
            throw new IllegalArgumentException("Capacity must be a positive integer value");
        }
        this.cacheMap = new HashMap<>();
        this.keys = new ArrayList<>();
        this.random = new Random();
        this.capacity = capacity;
        this.hitCount = 0;
        this.missCount = 0;
        this.evictionCount = 0;
    }

    @Override
    public V get(K key) {
        if (!cacheMap.containsKey(key)) {
            missCount++;
            return null;
        }
        hitCount++;
        return cacheMap.get(key);
    }

    @Override
    public void put(K key, V value) {
        if (cacheMap.containsKey(key)) {
            cacheMap.put(key, value);
        } else {
            if (keys.size() == capacity) {
                // Evict a random entry
                int randomIndex = random.nextInt(keys.size());
                K evictedKey = keys.get(randomIndex);
                // Swap with last for O(1) removal from ArrayList
                int lastIndex = keys.size() - 1;
                keys.set(randomIndex, keys.get(lastIndex));
                keys.remove(lastIndex);
                cacheMap.remove(evictedKey);
                evictionCount++;
            }
            keys.add(key);
            cacheMap.put(key, value);
        }
    }

    @Override
    public void remove(K key) {
        if (cacheMap.containsKey(key)) {
            cacheMap.remove(key);
            keys.remove(key);
        }
    }

    @Override
    public int getSize() {
        return cacheMap.size();
    }

    @Override
    public long getCapacity() {
        return capacity;
    }

    @Override
    public void clear() {
        cacheMap.clear();
        keys.clear();
        resetStats();
    }

    @Override
    public long getHitCount() {
        return hitCount;
    }

    @Override
    public long getMissCount() {
        return missCount;
    }

    @Override
    public long getEvictionCount() {
        return evictionCount;
    }

    @Override
    public void resetStats() {
        hitCount = 0;
        missCount = 0;
        evictionCount = 0;
    }

    @Override
    public long getTotalLatency() {
        return 0;
    }

    @Override
    public String getCacheName() {
        return this.getClass().getSimpleName();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("RandomCache State (Size: ").append(cacheMap.size()).append("/").append(capacity).append(")\n");
        sb.append("  Keys: ").append(keys).append("\n");
        sb.append("  Hits: ").append(hitCount).append(", Misses: ").append(missCount);
        return sb.toString();
    }
}
