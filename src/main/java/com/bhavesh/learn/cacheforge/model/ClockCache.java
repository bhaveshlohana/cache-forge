package com.bhavesh.learn.cacheforge.model;

import java.util.HashMap;
import java.util.Map;

/**
 * CLOCK (Second-Chance) Cache.
 * <p>
 * Approximation of LRU using a circular buffer with reference bits.
 * When eviction is needed, the clock hand sweeps the buffer:
 * <ul>
 *   <li>If reference bit = 1, clear it and move on (second chance)</li>
 *   <li>If reference bit = 0, evict this entry</li>
 * </ul>
 */
public class ClockCache<K, V> implements Cache<K, V> {

    private static class ClockEntry<K, V> {
        K key;
        V value;
        boolean referenceBit;

        ClockEntry(K key, V value) {
            this.key = key;
            this.value = value;
            this.referenceBit = true; // Set on insertion
        }
    }

    private final ClockEntry<K, V>[] buffer;
    private final Map<K, Integer> keyToIndex; // key -> buffer index
    private final long capacity;
    private int hand;   // Clock hand position
    private int size;

    private long hitCount;
    private long missCount;
    private long evictionCount;

    @SuppressWarnings("unchecked")
    public ClockCache(long capacity) {
        if (capacity <= 0) {
            throw new IllegalArgumentException("Capacity must be a positive integer value");
        }
        this.capacity = capacity;
        this.buffer = new ClockEntry[(int) capacity];
        this.keyToIndex = new HashMap<>();
        this.hand = 0;
        this.size = 0;
        this.hitCount = 0;
        this.missCount = 0;
        this.evictionCount = 0;
    }

    @Override
    public V get(K key) {
        Integer index = keyToIndex.get(key);
        if (index == null) {
            missCount++;
            return null;
        }
        // Set reference bit on access (second chance)
        buffer[index].referenceBit = true;
        hitCount++;
        return buffer[index].value;
    }

    @Override
    public void put(K key, V value) {
        // Update existing
        Integer existingIndex = keyToIndex.get(key);
        if (existingIndex != null) {
            buffer[existingIndex].value = value;
            buffer[existingIndex].referenceBit = true;
            return;
        }

        // Need eviction if at capacity
        if (size == capacity) {
            evictUsingClock();
        }

        // Find the next free slot (hand points to it after eviction, or we fill sequentially)
        if (size < capacity) {
            // Find an empty slot
            int slot = findEmptySlot();
            buffer[slot] = new ClockEntry<>(key, value);
            keyToIndex.put(key, slot);
            size++;
        }
    }

    @Override
    public void remove(K key) {
        Integer index = keyToIndex.get(key);
        if (index != null) {
            buffer[index] = null;
            keyToIndex.remove(key);
            size--;
        }
    }

    /**
     * CLOCK sweep: move the hand clockwise, clearing reference bits.
     * Evict the first entry with reference bit = 0.
     */
    private void evictUsingClock() {
        while (true) {
            ClockEntry<K, V> entry = buffer[hand];
            if (entry != null) {
                if (entry.referenceBit) {
                    // Give second chance: clear bit and advance
                    entry.referenceBit = false;
                } else {
                    // Evict this entry
                    keyToIndex.remove(entry.key);
                    buffer[hand] = null;
                    size--;
                    evictionCount++;
                    // Don't advance hand — this slot is now free for the new entry
                    return;
                }
            }
            hand = (hand + 1) % (int) capacity;
        }
    }

    private int findEmptySlot() {
        // Start from hand position, find first null slot
        for (int i = 0; i < capacity; i++) {
            int idx = (hand + i) % (int) capacity;
            if (buffer[idx] == null) {
                return idx;
            }
        }
        return hand; // Should never reach here if size < capacity
    }

    @Override
    public int getSize() {
        return size;
    }

    @Override
    public long getCapacity() {
        return capacity;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void clear() {
        for (int i = 0; i < capacity; i++) {
            buffer[i] = null;
        }
        keyToIndex.clear();
        hand = 0;
        size = 0;
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
        sb.append("ClockCache State (Size: ").append(size).append("/").append(capacity).append(")\n");
        sb.append("  Hand position: ").append(hand).append("\n");
        sb.append("  Entries: [");
        for (int i = 0; i < capacity; i++) {
            if (buffer[i] != null) {
                sb.append(buffer[i].key).append(":").append(buffer[i].referenceBit ? "1" : "0");
            } else {
                sb.append("_");
            }
            if (i < capacity - 1) sb.append(", ");
        }
        sb.append("]\n");
        sb.append("  Hits: ").append(hitCount).append(", Misses: ").append(missCount);
        return sb.toString();
    }
}
