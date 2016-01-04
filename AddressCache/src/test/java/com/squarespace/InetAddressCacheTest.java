package com.squarespace;

import java.net.InetAddress;
import junit.framework.TestCase;
import java.lang.Thread;

class MyThread extends Thread
{
    InetAddressCache c;

    public MyThread(InetAddressCache c)
    {
        this.c = c;
    }

    public void run()
    {
        try
        {
            Thread.sleep(5000);
            c.offer(InetAddress.getByName("google.com"));
        }
        catch(Exception e)
        {

        }
    }
}

public class InetAddressCacheTest extends TestCase
{
    public void test() throws Exception
    {
        InetAddressCache c = new InetAddressCache(10, 0);

        assert(c.size() == 0);

        c.offer(InetAddress.getByName("google.com"));

        assert(c.size() == 1);

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

        MyThread myThread = new MyThread(c);
        myThread.start();

        assert(c.take() == InetAddress.getByName("google.com"));
        assert(c.size() == 0);

        myThread.join();
        c.close();

        InetAddressCache c2 = new InetAddressCache(10, 1000);
        c2.offer(InetAddress.getByName("google.com"));
        c2.offer(InetAddress.getByName("javalobby.org"));

        Thread.sleep(5000);

        assert(c2.size() == 0);

        c2.close();
    }
}
