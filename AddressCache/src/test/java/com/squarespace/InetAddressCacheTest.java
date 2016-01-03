package com.squarespace;

import java.net.InetAddress;
import junit.framework.TestCase;
import java.lang.Thread;

class MyWriteThread extends Thread {
	InetAddressCache c;
	
	public MyWriteThread(InetAddressCache c) {
		this.c = c;
	}
	
	public void run() {
		try {
			Thread.sleep(5000);
			c.offer(InetAddress.getByName("google.com"));
		}
		catch(Exception e) {
			
		}
    }
}

public class InetAddressCacheTest extends TestCase {
	public void test() throws Exception
	{
		InetAddressCache c = new InetAddressCache(10, 0);
		
		c.offer(InetAddress.getByName("google.com"));
		c.offer(InetAddress.getByName("javalobby.org"));
		
		assert(c.peek() == InetAddress.getByName("javalobby.org"));
		c.offer(InetAddress.getByName("google.com"));
		assert(c.peek() == InetAddress.getByName("google.com"));
	    
		assert(c.size() == 2);
		
		c.remove();
		
		assert(c.peek() == InetAddress.getByName("javalobby.org"));
		assert(c.size() == 1);
		
		c.remove();
		
		assert(c.size() == 0);
		
		MyWriteThread myThread = new MyWriteThread(c);
		myThread.start();
    	  
		assert(c.take() == InetAddress.getByName("google.com"));
		assert(c.size() == 0);
	}
}
