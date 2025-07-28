package com.bhavesh.learn.cachepurge.domain;


public class Node<K, V> {
    public K key;
    public V value;
    public int frequency;
    public Node<K, V> next;
    public Node<K, V> prev;

    public Node(K key, V value) {
        this.key = key;
        this.value = value;
        this.frequency = 1;
    }

    public void incrementFrequency() {
        frequency++;
    }

    @Override
    public String toString() {
        return "Node{" +
                "value=" + value +
                ", key=" + key +
                ", frequency=" + frequency +
                '}';
    }
}
