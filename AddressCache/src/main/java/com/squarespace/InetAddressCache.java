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
    LinkedList<CacheEntry> ttiList; // sorted by access time - LRU
    LinkedList<CacheEntry> ttlList; // sorted by expiration
    HashMap<InetAddress,Pair<Node<CacheEntry>, Node<CacheEntry>>> map;
    int maxSize;
    long cachingTime;
    Timer timer;
    Object lock;
    final int cleanupTime = 5000;

    public InetAddressCache(int maxSize, long cachingTime) {
        this.ttiList = new LinkedList<CacheEntry>();
        this.ttlList = new LinkedList<CacheEntry>();
        this.map = new HashMap<InetAddress,Pair<Node<CacheEntry>, Node<CacheEntry>>>();
        this.maxSize = maxSize;
        this.cachingTime = cachingTime;
        this.lock = new Object();
        this.timer = new Timer(true);
        this.timer.schedule(new CleanupTimerTask(this), 0, cleanupTime);
    }

    // runtime complexity: O(1)
    void removeEntry(InetAddress address, boolean removeTtl) {
        Pair<Node<CacheEntry>, Node<CacheEntry>> rv = map.get(address);
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
                    CacheEntry e = ttlList.removeBack();
                    Node<CacheEntry> ttiPtr = map.get(e.address).getElement0();
                    map.remove(e.address);
                    ttiList.remove(ttiPtr);
                } else {
                    break;
                }
            }
        }
    }

    // runtime complexity: O(1)
    void insert(InetAddress address, Node<CacheEntry> ttlPtr) {
        CacheEntry e = new CacheEntry(address, System.currentTimeMillis());
        Node<CacheEntry> ttiPtr = ttiList.pushFront(e);

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
            Node<CacheEntry> ttlPtr = null;

            if(map.containsKey(address)) {
                ttlPtr = map.get(address).getElement1();
                removeEntry(address, false);
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
                removeEntry(address, true);
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