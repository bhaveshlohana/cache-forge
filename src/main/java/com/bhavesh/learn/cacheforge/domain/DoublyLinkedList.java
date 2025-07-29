package com.bhavesh.learn.cacheforge.domain;


public class DoublyLinkedList<K, V> {
    private Node<K, V> head;
    private Node<K, V> tail;
    private int size;

    public void addNode(Node<K, V> node) {
        node.prev = null;
        node.next = head;

        if (head != null) {
            head.prev = node;
        }
        head=node;
        if (tail == null) {
            tail = node;
        }
        size++;
    }

    public void addNodeAtTail(Node<K, V> node) {
        node.prev = tail;
        node.next = null;

        if (tail != null) {
            tail.next = node;
        } else {
            head = node;
        }
        tail=node;
        size++;
    }

    public void removeNode(Node<K, V> node) {
        if (node.prev != null)
            node.prev.next = node.next;
        else
            head = node.next;

        if (node.next != null)
            node.next.prev = node.prev;
        else
            tail=node.prev;
        size--;
        if (head == null) {
            tail = null;
        }
    }

    public void moveToHead(Node<K, V> node) {
        removeNode(node);
        addNode(node);
    }

    public Node<K, V> getTail() {
        return tail;
    }

    public Node<K, V> getHead() {
        return head;
    }

    public int getSize() {
        return size;
    }

    public boolean isEmpty() {
        return size == 0;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("  Order (MRU -> LRU): [");
        Node<K, V> current = head;
        while (current != null) {
            sb.append(current.toString()); // Uses Node's toString()
            if (current.next != null) {
                sb.append(" -> ");
            }
            current = current.next;
        }
        sb.append("]\n");
        return sb.toString();
    }
}
