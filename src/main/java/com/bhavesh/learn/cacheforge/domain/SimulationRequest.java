package com.bhavesh.learn.cacheforge.domain;

import com.bhavesh.learn.cacheforge.domain.enums.CacheStrategy;
import com.bhavesh.learn.cacheforge.domain.enums.WorkloadPattern;

import java.util.List;
import java.util.concurrent.TimeUnit;

public record SimulationRequest(
        List<CacheStrategy> cacheStrategies,
        long cacheSize,
        List<WorkloadPattern> workloadPatterns,
        int iterations,
        int keySpaceSize,
        double readWriteRatio,
        boolean ttlEnabled,
        long ttlDuration,
        TimeUnit ttlUnit
) {

    /**
     * Backward-compatible constructor (no TTL).
     */
    public SimulationRequest(List<CacheStrategy> cacheStrategies, long cacheSize,
                             List<WorkloadPattern> workloadPatterns, int iterations,
                             int keySpaceSize, double readWriteRatio) {
        this(cacheStrategies, cacheSize, workloadPatterns, iterations, keySpaceSize, readWriteRatio,
                false, 0, TimeUnit.MILLISECONDS);
    }

    public SimulationConfig withPatternAndStrategy(WorkloadPattern pattern, CacheStrategy strategy) {
        return new SimulationConfig(
                strategy,
                this.cacheSize(),
                pattern,
                this.iterations(),
                this.keySpaceSize(),
                this.readWriteRatio(),
                this.ttlEnabled(),
                this.ttlDuration(),
                this.ttlUnit()
        );
    }

    public SimulationRequest(SimulationCommonParams params) {
        this(
                List.of(CacheStrategy.values()),
                params.cacheSize(),
                List.of(WorkloadPattern.values()),
                params.iterations(),
                params.keySpaceSize(),
                params.readWriteRatio(),
                params.ttlEnabled(),
                params.ttlDuration(),
                params.ttlUnit()
        );
    }

    public SimulationRequest(WorkloadPattern pattern, SimulationCommonParams params) {
        this(
                List.of(CacheStrategy.values()),
                params.cacheSize(),
                List.of(pattern),
                params.iterations(),
                params.keySpaceSize(),
                params.readWriteRatio(),
                params.ttlEnabled(),
                params.ttlDuration(),
                params.ttlUnit()
        );
    }

    public SimulationRequest(CacheStrategy strategy, SimulationCommonParams params) {
        this(
                List.of(strategy),
                params.cacheSize(),
                List.of(WorkloadPattern.values()),
                params.iterations(),
                params.keySpaceSize(),
                params.readWriteRatio(),
                params.ttlEnabled(),
                params.ttlDuration(),
                params.ttlUnit()
        );
    }
}


