package com.squarespace;

import java.net.InetAddress;
import junit.framework.TestCase;

import java.io.Console;
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
        } catch(Exception e) {

        }
    }
}

public class InetAddressCacheTest extends TestCase {
    public void test() throws Exception {
        InetAddressCache c = new InetAddressCache(10, 10000);

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

        myThread.join();

        c.close();
        
        InetAddressCache c2 = new InetAddressCache(10, 2000);
        c2.offer(InetAddress.getByName("google.com"));
        c2.offer(InetAddress.getByName("javalobby.org"));
        
        Thread.sleep(3000);
        
        assert(c2.size() == 2);
        
        Thread.sleep(3000);
        
        assert(c2.size() == 0);
        
        c2.close();
    }
}
