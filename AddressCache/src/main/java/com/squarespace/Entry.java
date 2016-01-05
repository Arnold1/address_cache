package com.squarespace;

import java.net.InetAddress;

public class Entry {
    InetAddress address;
    long timestamp;

    public Entry(InetAddress address, long timestamp) {
        this.address = address;
        this.timestamp = timestamp;
    }
}