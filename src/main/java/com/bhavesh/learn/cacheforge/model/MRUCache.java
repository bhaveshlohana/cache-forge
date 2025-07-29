package com.bhavesh.learn.cacheforge.model;

import com.bhavesh.learn.cacheforge.domain.DoublyLinkedList;
import com.bhavesh.learn.cacheforge.domain.Node;

import java.util.HashMap;
import java.util.Map;

public class MRUCache<K, V> implements Cache<K, V> {
    private DoublyLinkedList<K, V> linkedList;
    private final Map<K, Node<K, V>> cacheMap;
    private final long capacity;
    private long hitCount;
    private long missCount;
    private long evictionCount;

    public MRUCache(long capacity) {
        if (capacity <= 0) {
            throw new IllegalArgumentException("Capacity must a positive integer value");
        }
        this.cacheMap = new HashMap<>();
        this.linkedList = new DoublyLinkedList<>();
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
        Node<K, V> node = cacheMap.get(key);
        linkedList.moveToHead(node);
        hitCount++;
        return node.value;
    }

    @Override
    public void put(K key, V value) {
        if (cacheMap.containsKey(key)) {
            Node<K, V> node = cacheMap.get(key);
            node.value = value;
            linkedList.moveToHead(node);
        } else {
            if (linkedList.getSize() == capacity) {
                cacheMap.remove(linkedList.getHead().key);
                linkedList.removeNode(linkedList.getHead());
                evictionCount++;
            }

            Node<K, V> node = new Node<>(key, value);
            linkedList.addNode(node);
            cacheMap.put(key, node);
        }
    }

    @Override
    public void remove(K key) {
        if (cacheMap.containsKey(key)) {
            Node<K, V> node = cacheMap.get(key);
            linkedList.removeNode(node);
            cacheMap.remove(key);
        }
    }

    @Override
    public int getSize() {
        return linkedList.getSize();
    }

    @Override
    public long getCapacity() {
        return capacity;
    }

    @Override
    public void clear() {
        cacheMap.clear();
        linkedList = new DoublyLinkedList<>();
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
        hitCount=0;
        missCount=0;
        evictionCount=0;
    }

    @Override
    public long getTotalLatency() {
        return 0; // LRU does not track latency
    }

    @Override
    public String getCacheName() {
        return this.getClass().getSimpleName();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Cache State (Size: ").append(linkedList.getSize()).append("/").append(capacity).append("):\n");

        // Option 1: Print from HashMap (shows what's present)
        sb.append("  Map: ").append(cacheMap.keySet()).append("\n");

        // Option 2: Print order from Doubly Linked List (shows recency order)
        sb.append(linkedList);

        sb.append("  Hits: ").append(hitCount).append(", Misses: ").append(missCount);

        return sb.toString();
    }
}
