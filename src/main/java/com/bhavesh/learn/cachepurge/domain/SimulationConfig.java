package com.bhavesh.learn.cachepurge.domain;

import com.bhavesh.learn.cachepurge.domain.enums.CacheStrategy;
import com.bhavesh.learn.cachepurge.domain.enums.WorkloadPattern;

public record SimulationConfig(CacheStrategy strategy, long cacheSize, WorkloadPattern pattern, int iterations,
                               int keySpaceSize, double readWriteRatio) {
}


