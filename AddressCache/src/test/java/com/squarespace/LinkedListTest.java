package com.squarespace;

import java.net.InetAddress;
import junit.framework.TestCase;

public class LinkedListTest extends TestCase {
	public void test() throws Exception
	{
		LinkedList<InetAddress> l = new LinkedList<InetAddress>();
		
		l.push_front(InetAddress.getByName("google.com"));
		Node<InetAddress> node2 = l.push_front(InetAddress.getByName("javalobby.org"));
		l.push_front(InetAddress.getByName("apple.com"));
		
		assert(l.size() == 3);
		assert(l.front() == InetAddress.getByName("apple.com"));
		assert(l.back() == InetAddress.getByName("google.com"));
		
		assert(l.remove_front() == InetAddress.getByName("apple.com"));
		assert(l.size() == 2);
		
		assert(l.remove_back() == InetAddress.getByName("google.com"));
		assert(l.size() == 1);
		
		assert(l.front() == InetAddress.getByName("javalobby.org"));
		assert(l.back() == InetAddress.getByName("javalobby.org"));
		
		l.remove(node2);
		assert(l.size() == 0);
		
		l.push_front(InetAddress.getByName("apple.com"));
		l.clear();
		
		assert(l.size() == 0);
	}
}
