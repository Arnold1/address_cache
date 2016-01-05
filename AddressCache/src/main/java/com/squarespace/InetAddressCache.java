package com.squarespace;

import java.util.*;
import java.net.InetAddress;

class CleanupTimerTask extends TimerTask {
    InetAddressCache c;

    public CleanupTimerTask(InetAddressCache c) {
        this.c = c;
    }

    public void run() {
        long currTime = System.currentTimeMillis();
        c.remove(currTime);
    }
}

// I implemented the following approach as shown on page 13:
// https://guava-libraries.googlecode.com/files/ConcurrentCachingAtGoogle.pdf
public class InetAddressCache implements AddressCache {
    LinkedList<Entry> ttiList; // sorted by access time - LRU
    LinkedList<Entry> ttlList; // sorted by expiration
    HashMap<InetAddress,Pair<ValueHolder<Entry, Node<Entry>>,
            ValueHolder<Entry, Node<Entry>>>> map;
    int maxSize;
    long cachingTime;
    Timer timer;
    Object lock;

    public InetAddressCache(int maxSize, long cachingTime) {
        this.ttiList = new LinkedList<Entry>();
        this.ttlList = new LinkedList<Entry>();
        this.map = new HashMap<InetAddress,Pair<ValueHolder<Entry, Node<Entry>>,
        ValueHolder<Entry, Node<Entry>>>>();
        this.maxSize = maxSize;
        this.cachingTime = cachingTime;
        this.lock = new Object();
        this.timer = new Timer(true);
        this.timer.schedule(new CleanupTimerTask(this), 0, 1000);
    }

    // runtime complexity: O(1)
    void cleanup(InetAddress address) {
        Pair<ValueHolder<Entry, Node<Entry>>, ValueHolder<Entry, Node<Entry>>> rv = map.get(address);
        map.remove(address);
        ttiList.remove(rv.getElement0().listLocation);
        ttlList.remove(rv.getElement1().listLocation);
    }

    // runtime complexity: O(1)
    void remove(long currTime) {
        synchronized(lock) {
            while (!ttiList.isEmpty()) {
                long timestamp = ttiList.back().timestamp;

                if ((currTime - timestamp) >= cachingTime) {
                    Entry e = ttiList.removeBack();
                    Pair<ValueHolder<Entry, Node<Entry>>, ValueHolder<Entry, Node<Entry>>> rv = map.get(e.address);
                    map.remove(e.address);
                    ttlList.remove(rv.getElement1().listLocation);
                } else {
                    break;
                }
            }
        }
    }

    // runtime complexity: O(1)
    void insert(InetAddress address) {
        Entry e = new Entry(address, System.currentTimeMillis());
        Node<Entry> ln = ttiList.pushFront(e);
        Node<Entry> ln2 = ttlList.pushFront(e);

        ValueHolder<Entry, Node<Entry>> rv = new ValueHolder(address, ln);
        ValueHolder<Entry, Node<Entry>> rv2 = new ValueHolder(address, ln2);
        map.put(address, new Pair(rv, rv2));
    }

    // runtime complexity: O(1)
    public boolean offer(InetAddress address) {
        if(ttiList.size() == maxSize) {
            return false;
        }

        synchronized(lock) {
            if(map.containsKey(address)) {
                cleanup(address);
            }

            boolean empty = ttiList.isEmpty();
            insert(address);

            if(empty) {
                lock.notify();
            }
        }
        return true;
    }

    // runtime complexity: O(1)
    public boolean contains(InetAddress address) {
        synchronized(lock) {
            if(map.containsKey(address)) {
                return true;
            }
            return false;
        }
    }

    // runtime complexity: O(1)
    public boolean remove(InetAddress address) {
        synchronized(lock) {
            if(map.containsKey(address)) {
                cleanup(address);
                return true;
            }
        }

        return false;
    }

    // runtime complexity: O(1)
    public InetAddress peek() {
        if(ttiList.isEmpty()) {
            return null;
        }

        synchronized(lock) {
            return ttiList.front().address;
        }
    }

    // runtime complexity: O(1)
    public InetAddress remove() {
        if(ttiList.isEmpty()) {
            return null;
        }

        synchronized(lock) {
            InetAddress address = ttiList.removeFront().address;
            map.remove(address);
            return address;
        }
    }

    // runtime complexity: O(1)
    public InetAddress take() throws InterruptedException {
        synchronized(lock) {
            while(ttiList.isEmpty()) {
                lock.wait();
            }

            InetAddress address = ttiList.removeBack().address;
            map.remove(address);
            return address;
        }
    }

    // runtime complexity: O(1)
    public void close() {
        synchronized(lock) {
            ttiList.clear();
            map.clear();

            if (timer != null) {
                timer.cancel();
                timer.purge();
            }
        }
    }

    // runtime complexity: O(1)
    public int size() {
        return ttiList.size();
    }

    // runtime complexity: O(1)
    public boolean isEmpty() {
        return ttiList.size() == 0;
    }
}