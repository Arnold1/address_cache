package com.squarespace;

import java.util.*;
import java.net.InetAddress;

class CleanupTimerTask extends TimerTask
{
    InetAddressCache c;

    public CleanupTimerTask(InetAddressCache c)
    {
        this.c = c;
    }

    public void run()
    {
        c.remove();
    }
}

public class InetAddressCache implements AddressCache
{
    LinkedList<InetAddress> l;
    HashMap<InetAddress,ValueHolder<InetAddress, Node<InetAddress>>> map;
    int max_size;
    Timer timer;
    Object lock;

    public InetAddressCache(int max_size, int caching_time)
    {
        l = new LinkedList<InetAddress>();
        map = new HashMap<InetAddress,ValueHolder<InetAddress, Node<InetAddress>>>();
        this.max_size = max_size;
        lock = new Object();

        if (caching_time > 0)
        {
            timer = new Timer(true);
            timer.schedule(new CleanupTimerTask(this), 0, caching_time);
        }
    }

    void cleanup(InetAddress address)
    {
        ValueHolder<InetAddress, Node<InetAddress>> rv = map.get(address);
        map.remove(address);
        l.remove(rv.listLocation);
    }

    public boolean offer(InetAddress address)
    {
        if(l.size() == max_size)
        {
            return false;
        }

        synchronized(lock)
        {
            if(map.containsKey(address))
            {
                cleanup(address);
            }

            boolean empty = l.isEmpty();
            Node<InetAddress> ln = l.push_front(address);

            ValueHolder<InetAddress, Node<InetAddress>> rv = new ValueHolder(address, ln);
            map.put(address, rv);

            if(empty)
            {
                lock.notify();
            }
        }

        return true;
    }

    public boolean contains(InetAddress address)
    {
        synchronized(lock)
        {
            if(map.containsKey(address))
            {
                return true;
            }
            return false;
        }
    }

    public boolean remove(InetAddress address)
    {
        synchronized(lock)
        {
            if(map.containsKey(address)) 
            {
		cleanup(address);
		return true;
	    }
        }

        return false;
    }

    public InetAddress peek()
    {
        if(l.isEmpty())
        {
            return null;
        }

        synchronized(lock)
        {
            return l.front();
        }
    }

    public InetAddress remove()
    {
        if(l.isEmpty())
        {
            return null;
        }

        synchronized(lock)
        {
            InetAddress address = l.remove_front();
            map.remove(address);
            return address;
        }
    }

    public InetAddress take() throws InterruptedException
    {
        synchronized(lock)
        {
            while(l.isEmpty())
            {
                lock.wait();
            }

            InetAddress address = l.remove_back();
            map.remove(address);
            return address;
        }
    }

    public void close()
    {
        synchronized(lock)
        {
            l.clear();
            map.clear();

            if (timer != null)
            {
                timer.cancel();
                timer.purge();
            }
        }
    }

    public int size()
    {
        return l.size();
    }

    public boolean isEmpty()
    {
        return l.size() == 0;
    }
}
