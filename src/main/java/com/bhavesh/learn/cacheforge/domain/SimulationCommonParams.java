package com.bhavesh.learn.cacheforge.domain;

import java.util.concurrent.TimeUnit;

public record SimulationCommonParams(
        long cacheSize,
        int iterations,
        int keySpaceSize,
        double readWriteRatio,
        boolean ttlEnabled,
        long ttlDuration,
        TimeUnit ttlUnit
) {
    public SimulationCommonParams(long cacheSize, int iterations, int keySpaceSize, double readWriteRatio) {
        this(cacheSize, iterations, keySpaceSize, readWriteRatio, false, 0, TimeUnit.MILLISECONDS);
    }
}


