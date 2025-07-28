package com.bhavesh.learn.cachepurge.generator.impl;

import com.bhavesh.learn.cachepurge.domain.CacheRequest;
import com.bhavesh.learn.cachepurge.domain.enums.OperationType;
import com.bhavesh.learn.cachepurge.generator.WorkLoadGenerator;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class SequentialGenerator implements WorkLoadGenerator {

    @Override
    public List<CacheRequest> generate(int iterations, int keySpace, double readWriteRatio) {
        List<CacheRequest> cacheRequests = new ArrayList<>();
        for (int i = 0; i < iterations; i++) {
            int key = i % keySpace;
            boolean isRead = (Math.random() < readWriteRatio);
            OperationType operationType = isRead ? OperationType.GET : OperationType.PUT;
            String value = "V" + key;
            cacheRequests.add(new CacheRequest(operationType, key, value));
        }
        return cacheRequests;
    }
}
