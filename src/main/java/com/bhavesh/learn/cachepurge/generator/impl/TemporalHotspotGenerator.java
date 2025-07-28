package com.bhavesh.learn.cachepurge.generator.impl;

import com.bhavesh.learn.cachepurge.domain.CacheRequest;
import com.bhavesh.learn.cachepurge.domain.enums.OperationType;
import com.bhavesh.learn.cachepurge.generator.WorkLoadGenerator;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class TemporalHotspotGenerator implements WorkLoadGenerator {

    @Override
    public List<CacheRequest> generate(int iterations, int keySpaceSize, double readWriteRatio) {
        Random rand = new Random();
        int windowSize = 1000;  // Change hotspot every 1000 requests
        int hotsetSize = (int) (keySpaceSize * 0.2); // 20% hot
        double hotProb = 0.8;

        List<CacheRequest> requests = new ArrayList<>();

        for (int i = 0; i < iterations; i++) {
            // Change hot set every window
            int window = i / windowSize;
            int hotStart = (window * hotsetSize) % (keySpaceSize - hotsetSize);

            int key;
            if (rand.nextDouble() < hotProb) {
                key = hotStart + rand.nextInt(hotsetSize);  // From hotset
            } else {
                key = rand.nextInt(keySpaceSize);  // From full space
            }

            OperationType op = rand.nextDouble() < readWriteRatio ? OperationType.GET : OperationType.PUT;
            requests.add(new CacheRequest(op, key, "V" + key));
        }

        return requests;
    }
}
