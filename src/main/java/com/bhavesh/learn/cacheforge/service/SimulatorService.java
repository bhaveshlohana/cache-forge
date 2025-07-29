package com.bhavesh.learn.cacheforge.service;

import com.bhavesh.learn.cacheforge.domain.*;
import com.bhavesh.learn.cacheforge.domain.enums.CacheStrategy;
import com.bhavesh.learn.cacheforge.domain.enums.OperationType;
import com.bhavesh.learn.cacheforge.domain.enums.WorkloadPattern;
import com.bhavesh.learn.cacheforge.model.*;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
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

    @Autowired
    MeterRegistry meterRegistry;

    public Map<String, Object> runSimulation(SimulationRequest simulationRequest) {
        Map<String, Object> allResults = new LinkedHashMap<>();

        for (WorkloadPattern pattern : simulationRequest.workloadPatterns()) {
            for (CacheStrategy strategy : simulationRequest.cacheStrategies()) {
                SimulationConfig config = simulationRequest.withPatternAndStrategy(pattern, strategy);
                List<CacheRequest> cacheRequestList = workloadGeneratorService.generateCacheRequest(config);

                Cache<Integer, String> cache = getCacheBasedOnStrategy(config);

                Timer.Sample sample = Timer.start(meterRegistry);
                runSimulationForCache(cache, cacheRequestList, config);
                sample.stop(meterRegistry.timer("cache.simulation.latency",
                        "strategy", strategy.name(),
                        "pattern", pattern.name()));

                allResults.put(pattern.name() + "-" + strategy.name(), getStats(cache));
                registerMetrics(pattern.name(), strategy.name(), getStats(cache));
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


    public void runSimulationForCache(Cache<Integer, String> cache, List<CacheRequest> cacheRequestList, SimulationConfig config) {
        for (CacheRequest request : cacheRequestList) {
            Timer timer = meterRegistry.timer(
                    "cache.operation.latency",
                    "strategy", config.strategy().name(),
                    "pattern", config.pattern().name(),
                    "operation", request.operationType().name()
            );

            timer.record(() -> {
                if (request.operationType() == OperationType.GET) {
                    cache.get(request.key());
                } else if (request.operationType() == OperationType.PUT) {
                    cache.put(request.key(), request.value());
                }
            });
        }
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

    public void registerMetrics(String patternName, String strategyName, Map<String, Object> stats) {
        String keyPrefix = patternName + "-" + strategyName;

        Gauge.builder("cache.stats.hit.count", stats, s -> ((Number) s.get("Hit Count")).doubleValue())
                .description("Cache hit count")
                .tags("strategy", strategyName, "pattern", patternName)
                .register(meterRegistry);

        Gauge.builder("cache.stats.miss.count", stats, s -> ((Number) s.get("Miss Count")).doubleValue())
                .description("Cache miss count")
                .tags("strategy", strategyName, "pattern", patternName)
                .register(meterRegistry);

        Gauge.builder("cache.stats.eviction.count", stats, s -> ((Number) s.get("Eviction Count")).doubleValue())
                .description("Cache eviction count")
                .tags("strategy", strategyName, "pattern", patternName)
                .register(meterRegistry);

        Gauge.builder("cache.stats.total.latency.ms", stats, s -> Double.parseDouble(s.get("Total Latency (ms)").toString()))
                .description("Total latency in milliseconds")
                .tags("strategy", strategyName, "pattern", patternName)
                .register(meterRegistry);

        Gauge.builder("cache.stats.average.latency.ms", stats, s -> {
                    Object value = s.get("Average Latency (ms/op)");
                    return value.equals("N/A") ? 0.0 : Double.parseDouble(value.toString());
                })
                .description("Average latency per operation")
                .tags("strategy", strategyName, "pattern", patternName)
                .register(meterRegistry);

        Gauge.builder("cache.stats.hit.rate", stats, s -> {
                    Object value = s.get("Hit Rate");
                    return value.equals("N/A") ? 0.0 : Double.parseDouble(value.toString().replace("%", ""));
                })
                .description("Hit rate percentage")
                .tags("strategy", strategyName, "pattern", patternName)
                .register(meterRegistry);
    }
}
