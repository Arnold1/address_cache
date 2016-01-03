#Overview

This project implements a addresscache interface for a fictional InetAddress cache. The cache has a "Last-In-First-Out" (LIFO) retrieval policy and a "First-In-First-Out" (FIFO) eviction policy. Methods such as peek(), remove() and take() retrieve the most recently added element and an internal cleanup task that in periodic intervals removes the oldest elements from the cache.


#Instructions

* The goal is to write the most beautiful and functionally correct code possible
* This repo includes an Implementation for AddressCache
* A cleanup task runs every 5 seconds to evict expired address's
* The caching time of an element is configurable by the user
* The implementation includes a set of tests to check the correctness
* The cache is designed for optimal asymptotic performance
