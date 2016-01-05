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
        c.removeExpired(currTime);
    }
}

// I implemented the following approach as shown on page 13:
// https://guava-libraries.googlecode.com/files/ConcurrentCachingAtGoogle.pdf
public class InetAddressCache implements AddressCache {
    LinkedList<Entry> ttiList; // sorted by access time - LRU
    LinkedList<Entry> ttlList; // sorted by expiration
    HashMap<InetAddress,Pair<Node<Entry>, Node<Entry>>> map;
    int maxSize;
    long cachingTime;
    Timer timer;
    Object lock;
    final int cleanupTime = 5000;

    public InetAddressCache(int maxSize, long cachingTime) {
        this.ttiList = new LinkedList<Entry>();
        this.ttlList = new LinkedList<Entry>();
        this.map = new HashMap<InetAddress,Pair<Node<Entry>, Node<Entry>>>();
        this.maxSize = maxSize;
        this.cachingTime = cachingTime;
        this.lock = new Object();
        this.timer = new Timer(true);
        this.timer.schedule(new CleanupTimerTask(this), 0, cleanupTime);
    }

    // runtime complexity: O(1)
    void cleanup(InetAddress address, boolean removeTtl) {
        Pair<Node<Entry>, Node<Entry>> rv = map.get(address);
        map.remove(address);
        ttiList.remove(rv.getElement0());
        if (removeTtl) {
            ttlList.remove(rv.getElement1());
        }
    }

    // runtime complexity: O(n) ... n is the number of expired elements to remove
    void removeExpired(long currTime) {
        synchronized(lock) {
            while (!ttlList.isEmpty()) {
                long timestamp = ttlList.back().timestamp;

                if ((currTime - timestamp) >= cachingTime) {
                    Entry e = ttlList.removeBack();
                    Node<Entry> ttiPtr = map.get(e.address).getElement0();
                    map.remove(e.address);
                    ttiList.remove(ttiPtr);
                } else {
                    break;
                }
            }
        }
    }

    // runtime complexity: O(1)
    void insert(InetAddress address, Node<Entry> ttlPtr) {
        Entry e = new Entry(address, System.currentTimeMillis());
        Node<Entry> ttiPtr = ttiList.pushFront(e);

        if (ttlPtr == null) {
        	ttlPtr = ttlList.pushFront(e);
        } 

        map.put(address, new Pair(ttiPtr, ttlPtr));
    }

    // runtime complexity: O(1)
    public boolean offer(InetAddress address) {
        if(ttiList.size() == maxSize) {
            return false;
        }

        synchronized(lock) {
        	Node<Entry> ttlPtr = null;
        	
        	if(map.containsKey(address)) {
            	ttlPtr = map.get(address).getElement1();
                cleanup(address, false);
            }

            boolean empty = ttiList.isEmpty();
            insert(address, ttlPtr);

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
                cleanup(address, true);
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