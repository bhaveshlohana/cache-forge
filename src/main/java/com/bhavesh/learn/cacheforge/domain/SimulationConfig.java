package com.bhavesh.learn.cacheforge.domain;

import com.bhavesh.learn.cacheforge.domain.enums.CacheStrategy;
import com.bhavesh.learn.cacheforge.domain.enums.WorkloadPattern;

import java.util.concurrent.TimeUnit;

public record SimulationConfig(CacheStrategy strategy, long cacheSize, WorkloadPattern pattern, int iterations,
                               int keySpaceSize, double readWriteRatio,
                               boolean ttlEnabled, long ttlDuration, TimeUnit ttlUnit) {

    /**
     * Compact constructor for backward compatibility (no TTL).
     */
    public SimulationConfig(CacheStrategy strategy, long cacheSize, WorkloadPattern pattern,
                            int iterations, int keySpaceSize, double readWriteRatio) {
        this(strategy, cacheSize, pattern, iterations, keySpaceSize, readWriteRatio,
                false, 0, TimeUnit.MILLISECONDS);
    }
}
