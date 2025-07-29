package com.bhavesh.learn.cacheforge.generator.impl;

import com.bhavesh.learn.cacheforge.domain.CacheRequest;
import com.bhavesh.learn.cacheforge.domain.enums.OperationType;
import com.bhavesh.learn.cacheforge.generator.WorkLoadGenerator;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class ZipfianGenerator implements WorkLoadGenerator {

    private double[] buildZipfCDF(int size, double skew) {
        double[] cdf = new double[size];
        double denom = 0.0;
        for (int i = 1; i <= size; i++) {
            denom += 1.0 / Math.pow(i, skew);
        }

        cdf[0] = (1.0 / Math.pow(1, skew)) / denom;
        for (int i = 1; i < size; i++) {
            double prob = (1.0 / Math.pow(i + 1, skew)) / denom;
            cdf[i] = cdf[i - 1] + prob;
        }

        return cdf;
    }

    private int pickZipfKey(double[] cdf, Random rand) {
        double r = rand.nextDouble();
        int low = 0, high = cdf.length - 1;

        while (low < high) {
            int mid = (low + high) / 2;
            if (r <= cdf[mid]) {
                high = mid;
            } else {
                low = mid + 1;
            }
        }

        return low;
    }

    @Override
    public List<CacheRequest> generate(int iterations, int keySpaceSize, double readWriteRatio) {
        Random rand = new Random();
        double[] cdf = buildZipfCDF(keySpaceSize, 1.07);

        List<CacheRequest> ops = new ArrayList<>();
        for (int i = 0; i < iterations; i++) {
            int key = pickZipfKey(cdf, rand);
            OperationType type = rand.nextDouble() < readWriteRatio ? OperationType.GET : OperationType.PUT;
            ops.add(new CacheRequest(type, key, "V" + key));
        }

        return ops;
    }
}
