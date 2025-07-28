package com.bhavesh.learn.cachepurge.service;

import com.bhavesh.learn.cachepurge.domain.*;
import com.bhavesh.learn.cachepurge.domain.enums.CacheStrategy;
import com.bhavesh.learn.cachepurge.domain.enums.OperationType;
import com.bhavesh.learn.cachepurge.domain.enums.WorkloadPattern;
import com.bhavesh.learn.cachepurge.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
public class SimulatorService {

    @Autowired
    WorkloadGeneratorService workloadGeneratorService;

    public Map<String, Object> rumSimulation(SimulationRequest simulationRequest) {
        Map<String, Object> allResults = new LinkedHashMap<>();

        for (WorkloadPattern pattern : simulationRequest.workloadPatterns()) {
            for (CacheStrategy strategy : simulationRequest.cacheStrategies()) {
                SimulationConfig config = simulationRequest.withPatternAndStrategy(pattern, strategy);
                List<CacheRequest> cacheRequestList = workloadGeneratorService.generateCacheRequest(config);
                Cache<Integer, String> cache = getCacheBasedOnStrategy(config);
                runSimulationForCache(cache, cacheRequestList);
                allResults.put(pattern.name() + "-" + strategy.name(), getStats(cache));
            }
        }
        return allResults;
    }

    public Cache<Integer, String> getCacheBasedOnStrategy(SimulationConfig simulationConfig) {
        long cacheSize = simulationConfig.cacheSize();
        Cache<Integer, String> cache = switch (simulationConfig.strategy()) {
            case LFU -> new LFUCache<>(cacheSize);
            case LRU -> new LRUCache<>(cacheSize);
            case FIFO -> new FIFOCache<>(cacheSize);
            case MRU -> new MRUCache<>(cacheSize);
            case TTL_LRU -> new TTLCacheDecorator<>(new LRUCache<>(cacheSize), 1, TimeUnit.NANOSECONDS);
            default -> new LRUCache<>(cacheSize);
        };

        return new LatencyTrackingCache<>(cache);
    }


    public void runSimulationForCache(Cache<Integer, String> cache, List<CacheRequest> cacheRequestList) {
        cacheRequestList.forEach(cacheRequest -> {
            if (cacheRequest.operationType() == OperationType.GET) {
                cache.get(cacheRequest.key());
            } else if (cacheRequest.operationType() == OperationType.PUT) {
                cache.put(cacheRequest.key(), cacheRequest.value());
            }
        });
    }

    public Map<String, Object> getStats(Cache<Integer, String> cache) {
        Map<String, Object> stats = new LinkedHashMap<>();
        stats.put("Cache Type", cache.getCacheName());
        stats.put("Capacity", cache.getCapacity());
        stats.put("Final Size", cache.getSize());
        stats.put("Hit Count", cache.getHitCount());
        stats.put("Miss Count", cache.getMissCount());
        stats.put("Eviction Count", cache.getEvictionCount());

        // Convert nanoseconds to milliseconds
        double totalLatencyMs = cache.getTotalLatency() / 1_000_000.0;
        stats.put("Total Latency (ms)", String.format("%.2f", totalLatencyMs));

        long totalAccesses = cache.getHitCount() + cache.getMissCount();
        stats.put("Average Latency (ms/op)", totalAccesses > 0
                ? String.format("%.4f", totalLatencyMs / totalAccesses)
                : "N/A");
        stats.put("Hit Rate", totalAccesses > 0
                ? String.format("%.2f%%", (double) cache.getHitCount() / totalAccesses * 100)
                : "N/A");

        return stats;
    }
}
