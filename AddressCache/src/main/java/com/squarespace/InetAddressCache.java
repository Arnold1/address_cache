package com.squarespace;

import java.util.*;
import java.net.InetAddress;

class Node<K>
{
    Node<K> prev;
    Node<K> next;
    K value;

    public Node(K v)
    {
        value = v;
        prev = null;
        next = null;
    }
}

class ValueHolder<K,V>
{
    V value;
    Node<K> listLocation;

    public ValueHolder(V value, Node<K> ll)
    {
        this.value = value;
        this.listLocation = ll;
    }
}

class LinkedList<K>
{
    Node<K> first = null;
    Node<K> last = null;
    int currentSize = 0;

    public Node<K> push_front(K v)
    {
        if(first == null)
        {
            assert(first == null);
            first = last = new Node<K>(v);
        }
        else
        {
            Node<K> tmp = new Node<K>(v);
            first.prev = tmp;
            tmp.next = first;
            first = tmp;
        }
        currentSize++;
        return first;
    }

    public K front()
    {
        return first.value;
    }

    public K back()
    {
        return last.value;
    }

    public K remove_front()
    {
        if(first == null)
        {
            return null;
        }
        K val = first.value;
        if(first.next == null)
        {
            first = null;
            last = null;
        }
        else
        {
            first = first.next;
            first.prev = null;
        }
        currentSize--;
        return val;
    }

    public K remove_back()
    {
        if(first == null)
        {
            return null;
        }
        K val = last.value;
        if(first.next == null)
        {
            first = null;
            last = null;
        }
        else
        {
            last = last.prev;
            last.next = null;
        }
        currentSize--;
        return val;
    }

    public void remove(Node<K> ln)
    {
        if (ln == null)
        {
            return;
        }

        Node<K> prev = ln.prev;
        Node<K> next = ln.next;
        if(prev == null)
        {
            first = next;
        }
        else
        {
            prev.next = next;
        }
        if(next == null)
        {
            last = prev;
        }
        else
        {
            next.prev = prev;
        }
        currentSize--;
    }

    public int size()
    {
        return currentSize;
    }

    public boolean isEmpty()
    {
        return currentSize == 0;
    }
}

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
        System.out.println("CleanupTimerTask::run()");
    }
}

public class InetAddressCache implements AddressCache
{
    LinkedList<InetAddress> l_;
    HashMap<InetAddress,ValueHolder<InetAddress, Node<InetAddress>>> map_;
    int max_size_;
    Timer timer;

    public InetAddressCache(int max_size, int caching_time)
    {
        l_ = new LinkedList<InetAddress>();
        map_ = new HashMap<InetAddress,ValueHolder<InetAddress, Node<InetAddress>>>();
        max_size_ = max_size;

        if (caching_time > 0)
        {
            timer = new Timer(true);
            timer.schedule(new CleanupTimerTask(this), 0, caching_time);
        }
    }

    void cleanup(InetAddress address)
    {
        ValueHolder<InetAddress, Node<InetAddress>> rv = map_.get(address);
        map_.remove(address);
        l_.remove(rv.listLocation);
    }

    public synchronized boolean offer(InetAddress address)
    {
        if(l_.size() == max_size_)
        {
            return false;
        }

        if(map_.containsKey(address))
        {
            cleanup(address);
        }

        boolean empty = l_.isEmpty();
        Node<InetAddress> ln = l_.push_front(address);
        ValueHolder<InetAddress, Node<InetAddress>> rv = new ValueHolder(address, ln);
        map_.put(address, rv);

        if(empty)
        {
            notify();
        }

        return true;
    }

    public synchronized boolean contains(InetAddress address)
    {
        ValueHolder<InetAddress, Node<InetAddress>> rv = map_.get(address);

        if(rv != null)
        {
            return true;
        }

        return false;
    }

    public synchronized boolean remove(InetAddress address)
    {
        ValueHolder<InetAddress, Node<InetAddress>> rv = map_.get(address);

        if(rv != null)
        {
            map_.remove(address);
            l_.remove(rv.listLocation);
            return true;
        }

        return false;
    }

    public synchronized InetAddress peek()
    {
        if(l_.isEmpty())
        {
            return null;
        }

        return l_.front();
    }

    public synchronized InetAddress remove()
    {
        if(l_.isEmpty())
        {
            return null;
        }

        InetAddress address = l_.remove_front();
        map_.remove(address);
        return address;
    }

    public synchronized InetAddress take() throws InterruptedException
    {
        while(l_.isEmpty())
        {
            wait();
        }

        InetAddress address = l_.remove_back();
        map_.remove(address);
        return address;
    }

    public void close()
    {

    }

    public int size()
    {
        return l_.size();
    }

    public boolean isEmpty()
    {
        return l_.size() == 0;
    }
}