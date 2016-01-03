package com.github.arnold1;

import com.squarespace.InetAddressCache;
import java.net.*;
import java.lang.Thread;

class MyWriteThread extends Thread {
	InetAddressCache c;
	
	public MyWriteThread(InetAddressCache c) {
		this.c = c;
	}
	
	public void run() {
		try {
			Thread.sleep(8000);
			c.offer(InetAddress.getByName("starwave.com"));
		}
		catch(Exception e) {
			
		}
    }
}

public class App 
{
    public static void main( String[] args ) throws Exception
    {
    	InetAddressCache c = new InetAddressCache(10, 0);
    	c.offer(InetAddress.getByName("starwave.com"));
    	c.offer(InetAddress.getByName("javalobby.org"));
    	
    	System.out.println(c.peek());
    	c.offer(InetAddress.getByName("starwave.com"));
    	System.out.println(c.peek());
        
    	c.remove();
    	System.out.println(c.peek());
    	c.remove();
    	
    	System.out.println(c.size());
    	
    	MyWriteThread myThread = new MyWriteThread(c);
    	myThread.start();
    	  
    	System.out.println(c.take());
    }
}