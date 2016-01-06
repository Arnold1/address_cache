package com.squarespace;

import java.net.InetAddress;

public class CacheEntry {
    InetAddress address;
    long timestamp;

    public CacheEntry(InetAddress address, long timestamp) {
        this.address = address;
        this.timestamp = timestamp;
    }
}