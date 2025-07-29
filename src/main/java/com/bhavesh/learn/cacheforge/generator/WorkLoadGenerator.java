package com.bhavesh.learn.cacheforge.generator;

import com.bhavesh.learn.cacheforge.domain.CacheRequest;

import java.util.List;

public interface WorkLoadGenerator {

    public List<CacheRequest> generate(int iterations, int keySpace, double readWriteRatio);
}
