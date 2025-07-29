package com.bhavesh.learn.cacheforge.domain;

import com.bhavesh.learn.cacheforge.domain.enums.CacheStrategy;
import com.bhavesh.learn.cacheforge.domain.enums.WorkloadPattern;

import java.util.List;

public record SimulationRequest (List<CacheStrategy> cacheStrategies, long cacheSize, List<WorkloadPattern> workloadPatterns, int iterations, int keySpaceSize, double readWriteRatio) {
    public SimulationConfig withPatternAndStrategy(WorkloadPattern pattern, CacheStrategy strategy) {
        return new SimulationConfig(
                strategy,
                this.cacheSize(),
                pattern,
                this.iterations(),
                this.keySpaceSize(),
                this.readWriteRatio()
        );
    }

    public SimulationRequest(SimulationCommonParams params) {
        this(
            List.of(CacheStrategy.values()),
            params.cacheSize(),
            List.of(WorkloadPattern.values()),
            params.iterations(),
            params.keySpaceSize(),
            params.readWriteRatio()
        );
    }

    public SimulationRequest(WorkloadPattern pattern, SimulationCommonParams params) {
        this(
                List.of(CacheStrategy.values()),
                params.cacheSize(),
                List.of(pattern),
                params.iterations(),
                params.keySpaceSize(),
                params.readWriteRatio()
        );
    }

    public SimulationRequest(CacheStrategy strategy, SimulationCommonParams params) {
        this(
                List.of(strategy),
                params.cacheSize(),
                List.of(WorkloadPattern.values()),
                params.iterations(),
                params.keySpaceSize(),
                params.readWriteRatio()
        );
    }
}


