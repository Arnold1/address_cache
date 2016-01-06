package com.squarespace;

import java.net.InetAddress;
import junit.framework.TestCase;

public class LinkedListTest extends TestCase {
    public void test() throws Exception {
        LinkedList<InetAddress> l = new LinkedList<InetAddress>();

        l.pushFront(InetAddress.getByName("google.com"));
        Node<InetAddress> node2 = l.pushFront(InetAddress.getByName("javalobby.org"));
        l.pushFront(InetAddress.getByName("apple.com"));

        assert(l.size() == 3);
        assert(l.front() == InetAddress.getByName("apple.com"));
        assert(l.back() == InetAddress.getByName("google.com"));

        assert(l.removeFront() == InetAddress.getByName("apple.com"));
        assert(l.size() == 2);

        assert(l.removeBack() == InetAddress.getByName("google.com"));
        assert(l.size() == 1);

        assert(l.front() == InetAddress.getByName("javalobby.org"));
        assert(l.back() == InetAddress.getByName("javalobby.org"));

        l.remove(node2);
        
        assert(l.size() == 0);

        l.pushFront(InetAddress.getByName("apple.com"));
        l.clear();

        assert(l.size() == 0);
    }
}
