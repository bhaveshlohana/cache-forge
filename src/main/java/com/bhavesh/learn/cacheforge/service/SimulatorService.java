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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.aop.support.AopUtils;
import java.util.concurrent.atomic.AtomicInteger;

import java.util.*;
import java.util.concurrent.*;

@Service
public class SimulatorService {

    @Autowired
    WorkloadGeneratorService workloadGeneratorService;

    @Autowired
    MeterRegistry meterRegistry;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;
    
    // configurable progress publish steps (percent and count)
    @Value("${simulation.progress.percent-step:5}")
    private int progressPercentStep = 5;

    @Value("${simulation.progress.count-step:1000}")
    private int progressCountStep = 1000;

    public Map<String, Object> runSimulation(SimulationRequest simulationRequest) {
        return runSimulation(simulationRequest, null);
    }

    /**
     * Overloaded runSimulation which accepts an optional runId. If runId is provided,
     * progress events will be published to `/topic/simulation/{runId}`; otherwise to `/topic/simulation`.
     */
    public Map<String, Object> runSimulation(SimulationRequest simulationRequest, String runId) {
        Map<String, Object> allResults = new LinkedHashMap<>();

        for (WorkloadPattern pattern : simulationRequest.workloadPatterns()) {
            for (CacheStrategy strategy : simulationRequest.cacheStrategies()) {
                SimulationConfig config = simulationRequest.withPatternAndStrategy(pattern, strategy);
                List<CacheRequest> cacheRequestList = workloadGeneratorService.generateCacheRequest(config);

                Cache<Integer, String> cache = getCacheBasedOnStrategy(config);

                Timer.Sample sample = Timer.start(meterRegistry);
                runSimulationForCache(cache, cacheRequestList, config, runId);
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
            case RANDOM -> new RandomCache<>(cacheSize);
            case ARC -> new ARCCache<>(cacheSize);
            case CLOCK -> new ClockCache<>(cacheSize);
        };

        return new LatencyTrackingCache<>(cache);
    }


    public void runSimulationForCache(Cache<Integer, String> cache, List<CacheRequest> cacheRequestList, SimulationConfig config) {
        runSimulationForCache(cache, cacheRequestList, config, null);
    }

    public void runSimulationForCache(Cache<Integer, String> cache, List<CacheRequest> cacheRequestList, SimulationConfig config, String runId) {
        int total = cacheRequestList.size();
        int reportEveryByCount = Math.max(1, progressCountStep);
        int completed = 0;
        int lastPercentSent = 0;

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

            completed++;

            int percent = (int) ((completed * 100L) / (total == 0 ? 1 : total));
            boolean shouldSend = false;
            if (percent - lastPercentSent >= progressPercentStep) {
                shouldSend = true;
            }
            if (completed % reportEveryByCount == 0) {
                shouldSend = true;
            }
            if (completed == total) {
                shouldSend = true;
            }

            if (shouldSend) {
                lastPercentSent = percent;
                Map<String, Object> event = new LinkedHashMap<>();
                event.put("progressPercent", percent);
                event.put("strategy", config.strategy().name());
                event.put("pattern", config.pattern().name());
                event.put("status", completed == total ? "COMPLETED" : "RUNNING");
                event.put("iterationsCompleted", completed);
                event.put("totalIterations", total);
                publishEvent(event, runId);
            }
        }
    }

    private String topicForRun(String runId) {
        return (runId == null || runId.isBlank()) ? "/topic/simulation" : ("/topic/simulation/" + runId);
    }

    private void publishEvent(Map<String, Object> event, String runId) {
        try {
            messagingTemplate.convertAndSend(topicForRun(runId), event);
        } catch (Exception e) {
            // Best-effort: do not throw to avoid failing the simulation
        }
    }

    public Map<String, Object> getStats(Cache<Integer, String> cache) {
        Map<String, Object> stats = new LinkedHashMap<>();
        // Unwrap AOP proxy if present so UI shows real cache type
        String cacheTypeName = AopUtils.isAopProxy(cache) ? AopUtils.getTargetClass(cache).getSimpleName() : cache.getClass().getSimpleName();
        stats.put("Cache Type", cacheTypeName);
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

    /**
     * Run simulation with thread-safe caches and concurrent workload execution.
     * Each cache is wrapped in a ConcurrentCacheDecorator, and the workload is
     * distributed across multiple threads.
     */
    public Map<String, Object> runConcurrentSimulation(SimulationRequest simulationRequest, int threadCount) {
        return runConcurrentSimulation(simulationRequest, threadCount, null);
    }

    public Map<String, Object> runConcurrentSimulation(SimulationRequest simulationRequest, int threadCount, String runId) {
        Map<String, Object> allResults = new LinkedHashMap<>();
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);

        try {
            for (WorkloadPattern pattern : simulationRequest.workloadPatterns()) {
                for (CacheStrategy strategy : simulationRequest.cacheStrategies()) {
                    SimulationConfig config = simulationRequest.withPatternAndStrategy(pattern, strategy);
                    List<CacheRequest> cacheRequestList = workloadGeneratorService.generateCacheRequest(config);

                    // Wrap in ConcurrentCacheDecorator for thread safety
                    Cache<Integer, String> cache = new ConcurrentCacheDecorator<>(getCacheBasedOnStrategy(config));

                    // Partition workload across threads
                    List<List<CacheRequest>> partitions = partitionList(cacheRequestList, threadCount);
                    List<Future<?>> futures = new ArrayList<>();

                    Timer.Sample sample = Timer.start(meterRegistry);

                    AtomicInteger completed = new AtomicInteger(0);
                    int totalRequests = cacheRequestList.size();
                    AtomicInteger lastPercentSent = new AtomicInteger(0);

                    for (List<CacheRequest> partition : partitions) {
                        futures.add(executor.submit(() -> {
                            for (CacheRequest request : partition) {
                                if (request.operationType() == OperationType.GET) {
                                    cache.get(request.key());
                                } else if (request.operationType() == OperationType.PUT) {
                                    cache.put(request.key(), request.value());
                                }

                                int comp = completed.incrementAndGet();
                                int percent = (int) ((comp * 100L) / Math.max(totalRequests, 1));
                                boolean shouldSend = false;
                                if (percent - lastPercentSent.get() >= progressPercentStep) {
                                    shouldSend = true;
                                }
                                if (comp % Math.max(1, progressCountStep) == 0) {
                                    shouldSend = true;
                                }
                                if (comp == totalRequests) {
                                    shouldSend = true;
                                }

                                if (shouldSend) {
                                    synchronized (lastPercentSent) {
                                        int prev = lastPercentSent.get();
                                        if (percent - prev >= progressPercentStep || comp % Math.max(1, progressCountStep) == 0 || comp == totalRequests) {
                                            lastPercentSent.set(percent);
                                            Map<String, Object> event = new LinkedHashMap<>();
                                            event.put("progressPercent", percent);
                                            event.put("strategy", config.strategy().name());
                                            event.put("pattern", config.pattern().name());
                                            event.put("status", comp == totalRequests ? "COMPLETED" : "RUNNING");
                                            event.put("iterationsCompleted", comp);
                                            event.put("totalIterations", totalRequests);
                                            publishEvent(event, runId);
                                        }
                                    }
                                }
                            }
                        }));
                    }

                    // Wait for all threads to complete
                    for (Future<?> future : futures) {
                        try {
                            future.get();
                        } catch (InterruptedException | ExecutionException e) {
                            Thread.currentThread().interrupt();
                            throw new RuntimeException("Concurrent simulation failed", e);
                        }
                    }

                    sample.stop(meterRegistry.timer("cache.concurrent.simulation.latency",
                            "strategy", strategy.name(),
                            "pattern", pattern.name()));

                    Map<String, Object> stats = getStats(cache);
                    stats.put("Thread Count", threadCount);
                    allResults.put(pattern.name() + "-" + strategy.name(), stats);
                    registerMetrics(pattern.name(), strategy.name(), stats);
                }
            }
        } finally {
            executor.shutdown();
        }

        return allResults;
    }

    /**
     * Run all strategy×pattern combinations in parallel using CompletableFuture.
     */
    public Map<String, Object> runSimulationAsync(SimulationRequest simulationRequest) {
        return runSimulationAsync(simulationRequest, null);
    }

    public Map<String, Object> runSimulationAsync(SimulationRequest simulationRequest, String runId) {
        Map<String, Object> allResults = new ConcurrentHashMap<>();
        List<CompletableFuture<Void>> futures = new ArrayList<>();

        for (WorkloadPattern pattern : simulationRequest.workloadPatterns()) {
            for (CacheStrategy strategy : simulationRequest.cacheStrategies()) {
                SimulationConfig config = simulationRequest.withPatternAndStrategy(pattern, strategy);

                CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                    List<CacheRequest> cacheRequestList = workloadGeneratorService.generateCacheRequest(config);
                    Cache<Integer, String> cache = getCacheBasedOnStrategy(config);

                    Timer.Sample sample = Timer.start(meterRegistry);
                    runSimulationForCache(cache, cacheRequestList, config, runId);
                    sample.stop(meterRegistry.timer("cache.simulation.latency",
                            "strategy", strategy.name(),
                            "pattern", pattern.name()));

                    Map<String, Object> stats = getStats(cache);
                    allResults.put(pattern.name() + "-" + strategy.name(), stats);
                });

                futures.add(future);
            }
        }

        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
        return new LinkedHashMap<>(allResults);
    }

    private <T> List<List<T>> partitionList(List<T> list, int partitions) {
        List<List<T>> result = new ArrayList<>();
        int size = list.size();
        int chunkSize = Math.max(1, (size + partitions - 1) / partitions);
        for (int i = 0; i < size; i += chunkSize) {
            result.add(list.subList(i, Math.min(i + chunkSize, size)));
        }
        return result;
    }
}
