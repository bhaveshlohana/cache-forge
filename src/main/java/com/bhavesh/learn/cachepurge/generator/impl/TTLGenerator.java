package com.bhavesh.learn.cachepurge.generator.impl;

import com.bhavesh.learn.cachepurge.domain.CacheRequest;
import com.bhavesh.learn.cachepurge.domain.enums.OperationType;
import com.bhavesh.learn.cachepurge.generator.WorkLoadGenerator;

import java.util.*;

public class TTLGenerator implements WorkLoadGenerator {

    @Override
    public List<CacheRequest> generate(int iterations, int keySpaceSize, double readWriteRatio) {
        Random rand = new Random();
        Map<Integer, Long> keyExpiryMap = new HashMap<>(); // key -> expiry timestamp
        long currentTime = System.currentTimeMillis();
        List<CacheRequest> requests = new ArrayList<>();

        for (int i = 0; i < iterations; i++) {
            int key = rand.nextInt(keySpaceSize);

            // Assign random TTL if new
            keyExpiryMap.putIfAbsent(key, currentTime + rand.nextInt(10_000)); // TTL between 0-10s

            boolean isRead = rand.nextDouble() < readWriteRatio;
            OperationType op;

            // If expired, force a PUT to simulate refresh
            if (System.currentTimeMillis() > keyExpiryMap.get(key)) {
                op = OperationType.PUT;
                keyExpiryMap.put(key, System.currentTimeMillis() + rand.nextInt(10_000));
            } else {
                op = isRead ? OperationType.GET : OperationType.PUT;
            }

            requests.add(new CacheRequest(op, key, "V" + key));
        }

        return requests;
    }
}
