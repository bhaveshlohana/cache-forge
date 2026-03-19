package com.bhavesh.learn.cacheforge.domain;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DoublyLinkedListTest {

    private DoublyLinkedList<Integer, String> list;

    @BeforeEach
    void setUp() {
        list = new DoublyLinkedList<>();
    }

    @Test
    void shouldBeEmptyInitially() {
        assertEquals(0, list.getSize());
        assertTrue(list.isEmpty());
        assertNull(list.getHead());
        assertNull(list.getTail());
    }

    @Test
    void shouldAddNodeToHead() {
        Node<Integer, String> node = new Node<>(1, "A");
        list.addNode(node);

        assertEquals(1, list.getSize());
        assertEquals(node, list.getHead());
        assertEquals(node, list.getTail());
        assertFalse(list.isEmpty());
    }

    @Test
    void shouldAddMultipleNodesToHead() {
        Node<Integer, String> node1 = new Node<>(1, "A");
        Node<Integer, String> node2 = new Node<>(2, "B");
        Node<Integer, String> node3 = new Node<>(3, "C");

        list.addNode(node1);
        list.addNode(node2);
        list.addNode(node3);

        assertEquals(3, list.getSize());
        assertEquals(node3, list.getHead());  // Last added is head
        assertEquals(node1, list.getTail());  // First added is tail
    }

    @Test
    void shouldMaintainCorrectPointers_AddNode() {
        Node<Integer, String> node1 = new Node<>(1, "A");
        Node<Integer, String> node2 = new Node<>(2, "B");

        list.addNode(node1);
        list.addNode(node2);

        // node2 is head, node1 is tail
        assertNull(node2.prev);
        assertEquals(node1, node2.next);
        assertEquals(node2, node1.prev);
        assertNull(node1.next);
    }

    @Test
    void shouldAddNodeAtTail() {
        Node<Integer, String> node1 = new Node<>(1, "A");
        Node<Integer, String> node2 = new Node<>(2, "B");

        list.addNodeAtTail(node1);
        list.addNodeAtTail(node2);

        assertEquals(2, list.getSize());
        assertEquals(node1, list.getHead());  // First added stays head
        assertEquals(node2, list.getTail());  // Last added is tail
    }

    @Test
    void shouldAddSingleNodeAtTail_SetsHeadAndTail() {
        Node<Integer, String> node = new Node<>(1, "A");
        list.addNodeAtTail(node);

        assertEquals(node, list.getHead());
        assertEquals(node, list.getTail());
        assertEquals(1, list.getSize());
    }

    @Test
    void shouldRemoveHead() {
        Node<Integer, String> node1 = new Node<>(1, "A");
        Node<Integer, String> node2 = new Node<>(2, "B");

        list.addNode(node1);
        list.addNode(node2);
        list.removeNode(node2); // remove head

        assertEquals(1, list.getSize());
        assertEquals(node1, list.getHead());
        assertEquals(node1, list.getTail());
    }

    @Test
    void shouldRemoveTail() {
        Node<Integer, String> node1 = new Node<>(1, "A");
        Node<Integer, String> node2 = new Node<>(2, "B");

        list.addNode(node1);
        list.addNode(node2);
        list.removeNode(node1); // remove tail

        assertEquals(1, list.getSize());
        assertEquals(node2, list.getHead());
        assertEquals(node2, list.getTail());
    }

    @Test
    void shouldRemoveMiddleNode() {
        Node<Integer, String> node1 = new Node<>(1, "A");
        Node<Integer, String> node2 = new Node<>(2, "B");
        Node<Integer, String> node3 = new Node<>(3, "C");

        list.addNode(node1);
        list.addNode(node2);
        list.addNode(node3);
        list.removeNode(node2); // remove middle

        assertEquals(2, list.getSize());
        assertEquals(node3, list.getHead());
        assertEquals(node1, list.getTail());
        assertEquals(node1, node3.next);
        assertEquals(node3, node1.prev);
    }

    @Test
    void shouldRemoveOnlyNode() {
        Node<Integer, String> node = new Node<>(1, "A");
        list.addNode(node);
        list.removeNode(node);

        assertEquals(0, list.getSize());
        assertTrue(list.isEmpty());
        assertNull(list.getHead());
        assertNull(list.getTail());
    }

    @Test
    void shouldMoveNodeToHead() {
        Node<Integer, String> node1 = new Node<>(1, "A");
        Node<Integer, String> node2 = new Node<>(2, "B");
        Node<Integer, String> node3 = new Node<>(3, "C");

        list.addNode(node1); // tail
        list.addNode(node2);
        list.addNode(node3); // head

        // Move tail to head
        list.moveToHead(node1);

        assertEquals(3, list.getSize());
        assertEquals(node1, list.getHead());
        assertEquals(node2, list.getTail());
    }

    @Test
    void shouldMoveHeadToHead_NoChange() {
        Node<Integer, String> node1 = new Node<>(1, "A");
        Node<Integer, String> node2 = new Node<>(2, "B");

        list.addNode(node1);
        list.addNode(node2); // head

        list.moveToHead(node2);

        assertEquals(2, list.getSize());
        assertEquals(node2, list.getHead());
        assertEquals(node1, list.getTail());
    }

    @Test
    void shouldReturnCorrectToString() {
        Node<Integer, String> node1 = new Node<>(1, "A");
        list.addNode(node1);

        String result = list.toString();
        assertTrue(result.contains("Order (MRU -> LRU):"));
        assertTrue(result.contains("key=1"));
    }
}
