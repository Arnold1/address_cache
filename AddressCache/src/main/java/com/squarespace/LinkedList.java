package com.squarespace;

class Node<K> {
    Node<K> prev;
    Node<K> next;
    K value;

    public Node(K v) {
        value = v;
        prev = null;
        next = null;
    }
}

class ValueHolder<K,V> {
    V value;
    Node<K> listLocation;

    public ValueHolder(V value, Node<K> ll) {
        this.value = value;
        this.listLocation = ll;
    }
}

class LinkedList<K> {
    Node<K> first = null;
    Node<K> last = null;
    int currentSize = 0;

    public Node<K> pushFront(K v) {
        if(first == null) {
            assert(first == null);
            first = last = new Node<K>(v);
        } else {
            Node<K> tmp = new Node<K>(v);
            first.prev = tmp;
            tmp.next = first;
            first = tmp;
        }
        currentSize++;
        return first;
    }

    public K front() {
        return first.value;
    }

    public K back() {
        return last.value;
    }

    public K removeFront() {
        if(first == null) {
            return null;
        }
        K val = first.value;
        if(first.next == null) {
            first = null;
            last = null;
        } else {
            first = first.next;
            first.prev = null;
        }
        currentSize--;
        return val;
    }

    public K removeBack() {
        if(first == null) {
            return null;
        }
        K val = last.value;
        if(first.next == null) {
            first = null;
            last = null;
        } else {
            last = last.prev;
            last.next = null;
        }
        currentSize--;
        return val;
    }

    public void remove(Node<K> ln) {
        if (first == null || ln == null) {
            return;
        }

        Node<K> prev = ln.prev;
        Node<K> next = ln.next;
        if(prev == null) {
            first = next;
        } else {
            prev.next = next;
        }
        if(next == null) {
            last = prev;
        } else {
            next.prev = prev;
        }
        currentSize--;
    }

    public int size() {
        return currentSize;
    }

    public boolean isEmpty() {
        return currentSize == 0;
    }

    public void clear() {
        if (first == null) {
            return;
        }

        first = null;
        last = null;
        currentSize = 0;
    }
}