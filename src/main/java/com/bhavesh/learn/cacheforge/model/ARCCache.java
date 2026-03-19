package com.bhavesh.learn.cacheforge.model;

import com.bhavesh.learn.cacheforge.domain.DoublyLinkedList;
import com.bhavesh.learn.cacheforge.domain.Node;

import java.util.HashMap;
import java.util.Map;

/**
 * Adaptive Replacement Cache (ARC).
 * <p>
 * Dynamically balances between recency (T1) and frequency (T2) by maintaining
 * four lists:
 * <ul>
 *   <li>T1 — pages seen recently exactly once (recency)</li>
 *   <li>T2 — pages seen recently at least twice (frequency)</li>
 *   <li>B1 — ghost entries recently evicted from T1</li>
 *   <li>B2 — ghost entries recently evicted from T2</li>
 * </ul>
 * An adaptive parameter {@code p} controls the target size of T1 vs T2,
 * adjusting based on which ghost list (B1/B2) sees more hits.
 */
public class ARCCache<K, V> implements Cache<K, V> {

    private final long capacity;

    // T1: pages that have been accessed exactly once recently
    private final Map<K, V> t1Map;
    private final DoublyLinkedList<K, V> t1;

    // T2: pages that have been accessed at least twice recently
    private final Map<K, V> t2Map;
    private final DoublyLinkedList<K, V> t2;

    // B1: ghost entries evicted from T1 (keys only, no values)
    private final Map<K, Boolean> b1Map;
    private final DoublyLinkedList<K, V> b1;

    // B2: ghost entries evicted from T2 (keys only, no values)
    private final Map<K, Boolean> b2Map;
    private final DoublyLinkedList<K, V> b2;

    // Adaptive parameter: target size for T1
    private int p;

    private long hitCount;
    private long missCount;
    private long evictionCount;

    public ARCCache(long capacity) {
        if (capacity <= 0) {
            throw new IllegalArgumentException("Capacity must be a positive integer value");
        }
        this.capacity = capacity;
        this.p = 0;

        this.t1Map = new HashMap<>();
        this.t1 = new DoublyLinkedList<>();
        this.t2Map = new HashMap<>();
        this.t2 = new DoublyLinkedList<>();
        this.b1Map = new HashMap<>();
        this.b1 = new DoublyLinkedList<>();
        this.b2Map = new HashMap<>();
        this.b2 = new DoublyLinkedList<>();

        this.hitCount = 0;
        this.missCount = 0;
        this.evictionCount = 0;
    }

    @Override
    public V get(K key) {
        // Case 1: key in T1 — move to T2 (now accessed twice)
        if (t1Map.containsKey(key)) {
            V value = t1Map.get(key);
            // Remove from T1, we need to find the node
            removeKeyFromList(t1, t1Map, key);
            // Add to T2 MRU position
            addToT2(key, value);
            hitCount++;
            return value;
        }

        // Case 2: key in T2 — move to MRU of T2
        if (t2Map.containsKey(key)) {
            V value = t2Map.get(key);
            removeKeyFromList(t2, t2Map, key);
            addToT2(key, value);
            hitCount++;
            return value;
        }

        // Miss
        missCount++;
        return null;
    }

    @Override
    public void put(K key, V value) {
        // Case 1: key in T1 — promote to T2
        if (t1Map.containsKey(key)) {
            removeKeyFromList(t1, t1Map, key);
            addToT2(key, value);
            return;
        }

        // Case 2: key in T2 — update and move to MRU of T2
        if (t2Map.containsKey(key)) {
            removeKeyFromList(t2, t2Map, key);
            addToT2(key, value);
            return;
        }

        // Case 3: key in B1 (ghost of T1) — favor recency, increase p
        if (b1Map.containsKey(key)) {
            int delta = Math.max(1, b2Map.size() / Math.max(1, b1Map.size()));
            p = (int) Math.min(p + delta, capacity);

            removeKeyFromGhostList(b1, b1Map, key);
            replace(key);
            addToT2(key, value);
            return;
        }

        // Case 4: key in B2 (ghost of T2) — favor frequency, decrease p
        if (b2Map.containsKey(key)) {
            int delta = Math.max(1, b1Map.size() / Math.max(1, b2Map.size()));
            p = Math.max(p - delta, 0);

            removeKeyFromGhostList(b2, b2Map, key);
            replace(key);
            addToT2(key, value);
            return;
        }

        // Case 5: complete miss — not in any list
        int totalT = t1Map.size() + t2Map.size();
        int totalAll = totalT + b1Map.size() + b2Map.size();

        if (t1Map.size() + b1Map.size() == capacity) {
            // B1's directory is full
            if (t1Map.size() < capacity) {
                evictLRUFromGhost(b1, b1Map);
                replace(key);
            } else {
                // T1 is full, evict from T1
                evictLRUFromCache(t1, t1Map);
            }
        } else if (totalAll >= capacity) {
            if (totalAll == 2 * capacity) {
                evictLRUFromGhost(b2, b2Map);
            }
            replace(key);
        }

        // Add to T1 (new entry, seen once)
        addToT1(key, value);
    }

    @Override
    public void remove(K key) {
        if (t1Map.containsKey(key)) {
            removeKeyFromList(t1, t1Map, key);
        } else if (t2Map.containsKey(key)) {
            removeKeyFromList(t2, t2Map, key);
        }
        // Also clean ghost lists
        if (b1Map.containsKey(key)) {
            removeKeyFromGhostList(b1, b1Map, key);
        }
        if (b2Map.containsKey(key)) {
            removeKeyFromGhostList(b2, b2Map, key);
        }
    }

    /**
     * Replace: evict from either T1 or T2 to make room, moving evicted to ghost list.
     */
    private void replace(K incomingKey) {
        int t1Size = t1Map.size();

        if (t1Size > 0 && (t1Size > p || (b2Map.containsKey(incomingKey) && t1Size == p))) {
            // Evict LRU of T1, add ghost to B1
            Node<K, V> lru = t1.getTail();
            if (lru != null) {
                t1.removeNode(lru);
                t1Map.remove(lru.key);
                addToGhostList(b1, b1Map, lru.key);
                evictionCount++;
            }
        } else {
            // Evict LRU of T2, add ghost to B2
            Node<K, V> lru = t2.getTail();
            if (lru != null) {
                t2.removeNode(lru);
                t2Map.remove(lru.key);
                addToGhostList(b2, b2Map, lru.key);
                evictionCount++;
            }
        }
    }

    private void addToT1(K key, V value) {
        Node<K, V> node = new Node<>(key, value);
        t1.addNode(node);
        t1Map.put(key, value);
    }

    private void addToT2(K key, V value) {
        Node<K, V> node = new Node<>(key, value);
        t2.addNode(node);
        t2Map.put(key, value);
    }

    private void removeKeyFromList(DoublyLinkedList<K, V> list, Map<K, ?> map, K key) {
        // Walk the list to find the node with this key
        // This is O(n) but acceptable for a learning implementation
        Node<K, V> current = list.getHead();
        while (current != null) {
            if (current.key.equals(key)) {
                list.removeNode(current);
                map.remove(key);
                return;
            }
            current = current.next;
        }
    }

    private void removeKeyFromGhostList(DoublyLinkedList<K, V> list, Map<K, Boolean> map, K key) {
        Node<K, V> current = list.getHead();
        while (current != null) {
            if (current.key.equals(key)) {
                list.removeNode(current);
                map.remove(key);
                return;
            }
            current = current.next;
        }
    }

    private void addToGhostList(DoublyLinkedList<K, V> list, Map<K, Boolean> map, K key) {
        Node<K, V> node = new Node<>(key, null);
        list.addNode(node);
        map.put(key, true);
    }

    private void evictLRUFromGhost(DoublyLinkedList<K, V> list, Map<K, Boolean> map) {
        Node<K, V> lru = list.getTail();
        if (lru != null) {
            list.removeNode(lru);
            map.remove(lru.key);
        }
    }

    private void evictLRUFromCache(DoublyLinkedList<K, V> list, Map<K, V> map) {
        Node<K, V> lru = list.getTail();
        if (lru != null) {
            list.removeNode(lru);
            map.remove(lru.key);
            evictionCount++;
        }
    }

    @Override
    public int getSize() {
        return t1Map.size() + t2Map.size();
    }

    @Override
    public long getCapacity() {
        return capacity;
    }

    @Override
    public void clear() {
        t1Map.clear();
        t2Map.clear();
        b1Map.clear();
        b2Map.clear();
        // Re-create lists by clearing internal state
        clearList(t1);
        clearList(t2);
        clearList(b1);
        clearList(b2);
        p = 0;
        resetStats();
    }

    private void clearList(DoublyLinkedList<K, V> list) {
        while (list.getHead() != null) {
            list.removeNode(list.getHead());
        }
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
        sb.append("ARCCache State (Size: ").append(getSize()).append("/").append(capacity).append(")\n");
        sb.append("  T1 size: ").append(t1Map.size()).append(", T2 size: ").append(t2Map.size()).append("\n");
        sb.append("  B1 size: ").append(b1Map.size()).append(", B2 size: ").append(b2Map.size()).append("\n");
        sb.append("  p: ").append(p).append("\n");
        sb.append("  Hits: ").append(hitCount).append(", Misses: ").append(missCount);
        return sb.toString();
    }
}
