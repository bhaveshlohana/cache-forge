package com.bhavesh.learn.cacheforge.domain;

import com.bhavesh.learn.cacheforge.domain.enums.CacheStrategy;
import com.bhavesh.learn.cacheforge.domain.enums.WorkloadPattern;

public record SimulationConfig(CacheStrategy strategy, long cacheSize, WorkloadPattern pattern, int iterations,
                               int keySpaceSize, double readWriteRatio) {
}


