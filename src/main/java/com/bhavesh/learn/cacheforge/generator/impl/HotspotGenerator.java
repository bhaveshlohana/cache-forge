package com.bhavesh.learn.cacheforge.generator.impl;

import com.bhavesh.learn.cacheforge.domain.CacheRequest;
import com.bhavesh.learn.cacheforge.domain.enums.OperationType;
import com.bhavesh.learn.cacheforge.generator.WorkLoadGenerator;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class HotspotGenerator implements WorkLoadGenerator {

    @Override
    public List<CacheRequest> generate(int iterations, int keySpaceSize, double readWriteRatio) {
        Random rand = new Random();

        int hotSetSize = (int) (keySpaceSize * 0.2); // 20%
        int coldSetSize = keySpaceSize - hotSetSize;

        int hotSetStart = 0;
        int coldSetStart = hotSetSize;

        double hotAccessProb = 0.8; // 80% of accesses go to hot set

        List<CacheRequest> ops = new ArrayList<>();

        for (int i = 0; i < iterations; i++) {
            int key;
            if (rand.nextDouble() < hotAccessProb) {
                // Pick from hot set
                key = hotSetStart + rand.nextInt(hotSetSize);
            } else {
                // Pick from cold set
                key = coldSetStart + rand.nextInt(coldSetSize);
            }

            boolean isRead = rand.nextDouble() < readWriteRatio;
            OperationType op = isRead ? OperationType.GET : OperationType.PUT;

            ops.add(new CacheRequest(op, key, "V" + key));
        }

        return ops;
    }
}
