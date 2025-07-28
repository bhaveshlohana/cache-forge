package com.bhavesh.learn.cachepurge.domain;

public record SimulationCommonParams(
        long cacheSize,
        int iterations,
        int keySpaceSize,
        double readWriteRatio
) {
}


