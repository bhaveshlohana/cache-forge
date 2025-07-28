package com.bhavesh.learn.cachepurge.service;

import com.bhavesh.learn.cachepurge.domain.CacheRequest;
import com.bhavesh.learn.cachepurge.domain.TTLCacheDecorator;
import com.bhavesh.learn.cachepurge.model.*;

import java.util.concurrent.TimeUnit;

public class Main {

    public static void main(String[] args) throws InterruptedException {
        System.out.println("--- Starting LRU Cache Test ---");

        // Test 1: Basic Puts and Gets
        System.out.println("\n--- Test 1: Basic Puts and Gets (Capacity 3) ---");
//        Cache<Integer, String> cache = new LRUCache<>(3);
        TTLCacheDecorator<Integer, String> cache = new TTLCacheDecorator<>(new LRUCache<>(3), 1000, TimeUnit.NANOSECONDS); // 1 second TTL
        System.out.println("Initial: " + cache); // Should be empty

        cache.put(1, "Apple");
        System.out.println("Put (1, Apple): " + cache); // [1:Apple]

        cache.put(2, "Banana");
        System.out.println("Put (2, Banana): " + cache); // [2:Banana -> 1:Apple]
        Thread.sleep(10000);
        cache.put(3, "Cherry");
        System.out.println("Put (3, Cherry): " + cache); // [3:Cherry -> 2:Banana -> 1:Apple]
        // Cache is now full: Size 3/3

        System.out.println("\nAttempting to get non-existent key (4):");
        System.out.println("Get (4): " + cache.get(4)); // Should be null
        System.out.println("After Get (4) (Miss): " + cache); // Order unchanged, miss count +1

        System.out.println("\nAttempting to get existent key (2):");
        System.out.println("Get (2): " + cache.get(2)); // Should return "Banana"
        System.out.println("After Get (2) (Hit, Recency Update): " + cache); // [2:Banana -> 3:Cherry -> 1:Apple]

        System.out.println("\n--- Test 2: Eviction Scenario ---");
        cache.put(4, "Date"); // This should cause an eviction (1:Apple is LRU)
        System.out.println("Put (4, Date) (Eviction): " + cache); // [4:Date -> 2:Banana -> 3:Cherry] (1:Apple should be gone)

        System.out.println("\nAttempting to get evicted key (1):");
        System.out.println("Get (1): " + cache.get(1)); // Should be null
        System.out.println("After Get (1) (Miss): " + cache); // Order unchanged, miss count +1

        System.out.println("\n--- Test 3: Updating Existing Key & Eviction Chain ---");
        cache.put(2, "New Banana"); // Update existing, moves to MRU
        System.out.println("Put (2, New Banana) (Update & Recency): " + cache); // [2:New Banana -> 4:Date -> 3:Cherry]

        cache.put(5, "Elderberry"); // Evicts 3:Cherry
        System.out.println("Put (5, Elderberry) (Eviction): " + cache); // [5:Elderberry -> 2:New Banana -> 4:Date]

        System.out.println("\n--- Test 4: Final Stats and Clear ---");
        System.out.println("Final Cache State: " + cache);
        System.out.println("Final Hits: " + cache.getHitCount() + ", Misses: " + cache.getMissCount());

        cache.resetStats();
        System.out.println("Stats after reset: Hits=" + cache.getHitCount() + ", Misses=" + cache.getMissCount());

        cache.clear();
        System.out.println("After Clear: " + cache); // Should be empty again
        System.out.println("Final state after clear (size): " + cache.getSize());
    }
}

//public class Main {
//
//    public static void main(String[] args) {
//        LRUCache<Integer, String> lruCache = new LRUCache<>(3);
//        lruCache.put(1, "one");
//        System.out.println(lruCache);
//        lruCache.put(2, "two");
//        System.out.println(lruCache);
//        lruCache.get(2);
//        System.out.println(lruCache);
//        lruCache.put(3, "three");
//        System.out.println(lruCache);
//        lruCache.put(4, "four");
//        System.out.println(lruCache);
//        lruCache.get(1);
//        System.out.println(lruCache);
//        lruCache.get(2);
//        System.out.println(lruCache);
//        lruCache.get(1);
//        System.out.println(lruCache);
//        lruCache.get(3);
//        System.out.println(lruCache);
//        lruCache.get(4);
//        System.out.println(lruCache);
//        System.out.println(lruCache.getHitCount() + " " + lruCache.getMissCount());
//    }
//}
