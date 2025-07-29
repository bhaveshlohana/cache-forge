package com.bhavesh.learn.cacheforge.domain;

public record SimulationCommonParams(
        long cacheSize,
        int iterations,
        int keySpaceSize,
        double readWriteRatio
) {
}


