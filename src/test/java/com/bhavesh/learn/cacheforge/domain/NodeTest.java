package com.bhavesh.learn.cacheforge.domain;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class NodeTest {

    @Test
    void shouldCreateNodeWithKeyValueAndDefaultFrequency() {
        Node<Integer, String> node = new Node<>(1, "Apple");

        assertEquals(1, node.key);
        assertEquals("Apple", node.value);
        assertEquals(1, node.frequency);
        assertNull(node.next);
        assertNull(node.prev);
    }

    @Test
    void shouldCreateNodeWithNullKeyAndValue() {
        Node<String, String> node = new Node<>(null, null);

        assertNull(node.key);
        assertNull(node.value);
        assertEquals(1, node.frequency);
    }

    @Test
    void shouldIncrementFrequency() {
        Node<Integer, String> node = new Node<>(1, "Apple");

        assertEquals(1, node.frequency);
        node.incrementFrequency();
        assertEquals(2, node.frequency);
        node.incrementFrequency();
        assertEquals(3, node.frequency);
    }

    @Test
    void shouldIncrementFrequencyMultipleTimes() {
        Node<Integer, String> node = new Node<>(1, "value");

        for (int i = 0; i < 100; i++) {
            node.incrementFrequency();
        }
        assertEquals(101, node.frequency);
    }

    @Test
    void shouldAllowMutableValueField() {
        Node<Integer, String> node = new Node<>(1, "Apple");

        node.value = "Banana";
        assertEquals("Banana", node.value);
    }

    @Test
    void shouldReturnCorrectToString() {
        Node<Integer, String> node = new Node<>(1, "Apple");

        String result = node.toString();
        assertTrue(result.contains("key=1"));
        assertTrue(result.contains("value=Apple"));
        assertTrue(result.contains("frequency=1"));
    }

    @Test
    void shouldSetPrevAndNextPointers() {
        Node<Integer, String> node1 = new Node<>(1, "A");
        Node<Integer, String> node2 = new Node<>(2, "B");

        node1.next = node2;
        node2.prev = node1;

        assertEquals(node2, node1.next);
        assertEquals(node1, node2.prev);
        assertNull(node1.prev);
        assertNull(node2.next);
    }
}
