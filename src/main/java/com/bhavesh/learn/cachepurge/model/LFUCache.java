package com.bhavesh.learn.cachepurge.model;

import com.bhavesh.learn.cachepurge.domain.DoublyLinkedList;
import com.bhavesh.learn.cachepurge.domain.Node;

import java.util.HashMap;
import java.util.Map;

public class LFUCache<K, V> implements Cache<K, V> {
    private Map<Integer, DoublyLinkedList<K, V>> freqMap;
    private final Map<K, Node<K, V>> cacheMap;
    private final long capacity;
    private long hitCount;
    private long missCount;
    private int minFrequency;
    private int size;
    private long evictionCount;

    public LFUCache(long capacity) {
        this.freqMap = new HashMap<>();
        this.cacheMap = new HashMap<>();
        this.capacity = capacity;
        this.missCount = 0;
        this.hitCount = 0;
        this.size = 0;
        this.minFrequency = 0;
        this.evictionCount = 0;
    }

    @Override
    public V get(K key) {
        if (!cacheMap.containsKey(key)) {
            missCount++;
            return null;
        }
        hitCount++;
        Node<K, V> node = cacheMap.get(key);

        DoublyLinkedList<K, V> currentFreqMap = freqMap.get(node.frequency);
        if (currentFreqMap == null) {
            return null;
        }

        currentFreqMap.removeNode(node);
        if (currentFreqMap.isEmpty() && node.frequency == minFrequency) {
            minFrequency++;
        }

        node.incrementFrequency();
        DoublyLinkedList<K, V> newFreqList = freqMap.computeIfAbsent(node.frequency, k -> new DoublyLinkedList<>());

        newFreqList.addNode(node);
        return node.value;
    }

    @Override
    public void put(K key, V value) {
        if (cacheMap.containsKey(key)) {
            Node<K, V> node = cacheMap.get(key);
            node.value = value;
            DoublyLinkedList<K, V> currentFreqList = freqMap.get(node.frequency);
            if (currentFreqList == null) {
                return;
            }
            currentFreqList.removeNode(node);
            if (currentFreqList.isEmpty() && node.frequency == minFrequency) {
                minFrequency++;
            }

            node.incrementFrequency();
            DoublyLinkedList<K, V> newFreqList = freqMap.computeIfAbsent(node.frequency, k-> new DoublyLinkedList<>());
            newFreqList.addNode(node);
        } else {
            if (size == capacity) {
                DoublyLinkedList<K, V> minFreqList = freqMap.get(minFrequency);
                if (minFreqList == null ||minFreqList.isEmpty()) {
                    return;
                }
                Node<K, V> leastFreqNode = minFreqList.getTail();
                cacheMap.remove(leastFreqNode.key);
                minFreqList.removeNode(leastFreqNode);
                evictionCount++;
                size--;
            }

            Node<K, V> node = new Node<>(key, value);
            DoublyLinkedList<K, V> freqList = freqMap.computeIfAbsent(node.frequency, k -> new DoublyLinkedList<>());
            freqList.addNode(node);
            cacheMap.put(key, node);
            minFrequency = 1;
            size++;
        }
    }

    @Override
    public void remove(K key) {
        if (!cacheMap.containsKey(key)) {
            return;
        }
        Node<K, V> node = cacheMap.get(key);
        cacheMap.remove(key);
        DoublyLinkedList<K, V> freqList = freqMap.get(node.frequency);
        if (freqList != null) {
            freqList.removeNode(node);
            if (node.frequency == minFrequency) {
                while (minFrequency <= capacity && (freqMap.get(minFrequency) == null || freqMap.get(minFrequency).isEmpty())) {
                    minFrequency++;
                }
                // If minFrequency exceeds capacity, it means cache is empty, so reset it
                if (minFrequency > capacity) { // Or if cacheMap.isEmpty()
                    minFrequency = 0; // Or whatever signifies an empty cache's min frequency
                    // If size is 0, minFrequency should probably be 0
                }
            }
        }
        size--;
        if (size == 0) {
            minFrequency = 0;
        }

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
    public void clear() {
        cacheMap.clear();
        freqMap = new HashMap<>();
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
        return 0; // LFU does not track latency
    }

    @Override
    public String getCacheName() {
        return this.getClass().getSimpleName();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("LFUCache State (Size: ").append(size).append("/").append(capacity).append(")\n");
        sb.append("  Min Frequency: ").append(minFrequency).append("\n");
        sb.append("  Map Keys: ").append(cacheMap.keySet()).append("\n");

        sb.append("  Frequency Buckets (Freq -> [Nodes]):\n");

        // Get sorted frequencies to print them in order
        // Using a TreeMap ensures keys are naturally sorted
        // or you can collect keys and sort them.
        freqMap.keySet().stream().sorted().forEach(freq -> {
            DoublyLinkedList<K, V> list = freqMap.get(freq);
            if (list != null && !list.isEmpty()) {
                sb.append("    Freq ").append(freq).append(": [");
                sb.append(list);
            }
        });

        sb.append("  Hits: ").append(hitCount).append(", Misses: ").append(missCount).append("\n");

        return sb.toString();
    }
}
