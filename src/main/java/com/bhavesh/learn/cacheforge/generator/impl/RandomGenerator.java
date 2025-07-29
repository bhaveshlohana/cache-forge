package com.bhavesh.learn.cacheforge.generator.impl;

import com.bhavesh.learn.cacheforge.domain.CacheRequest;
import com.bhavesh.learn.cacheforge.domain.enums.OperationType;
import com.bhavesh.learn.cacheforge.generator.WorkLoadGenerator;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class RandomGenerator implements WorkLoadGenerator {

    private final Random random = new Random();
    @Override
    public List<CacheRequest> generate(int iterations, int keySpace, double readWriteRatio) {
        List<CacheRequest> cacheRequests = new ArrayList<>();
        for (int i = 0; i < iterations; i++) {
            int key = random.nextInt(keySpace);
            OperationType operationType = random.nextDouble() < readWriteRatio ? OperationType.GET : OperationType.PUT;
            String value = "V" + key;
            cacheRequests.add(new CacheRequest(operationType, key, value));
        }
        return cacheRequests;
    }
}
