package com.bhavesh.learn.cachepurge.generator;

import com.bhavesh.learn.cachepurge.domain.CacheRequest;

import java.util.List;

public interface WorkLoadGenerator {

    public List<CacheRequest> generate(int iterations, int keySpace, double readWriteRatio);
}
