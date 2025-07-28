package com.bhavesh.learn.cachepurge.util;

import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class LatencyStatsCollector {
    private static class Stats {
        long totalNanos = 0;
        long count = 0;
    }

    private final Map<String, Stats> methodStats = new ConcurrentHashMap<>();

    public void record(String methodName, long durationNanos) {
        methodStats.compute(methodName, (k, v) -> {
            if (v == null) v = new Stats();
            v.totalNanos += durationNanos;
            v.count++;
            return v;
        });
    }

    public void printAllStats() {
        System.out.println("\n--- Latency Stats Per Method ---");
        for (Map.Entry<String, Stats> entry : methodStats.entrySet()) {
            String method = entry.getKey();
            Stats s = entry.getValue();
            long avgMicros = s.totalNanos / s.count / 1000;
            long totalMillis = s.totalNanos / 1_000_000;

            System.out.printf("Method: %-25s | Calls: %-6d | Avg: %-6d µs | Total: %-6d ms\n",
                    method, s.count, avgMicros, totalMillis);
        }
    }

    public String getAllStatsAsString() {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, Stats> entry : methodStats.entrySet()) {
            String method = entry.getKey();
            Stats s = entry.getValue();
            long avgMicros = s.totalNanos / s.count / 1000;
            long totalMillis = s.totalNanos / 1_000_000;
            sb.append("Method: ").append(method)
              .append(" | Calls: ").append(s.count)
              .append(" | Avg: ").append(avgMicros).append(" µs")
              .append(" | Total: ").append(totalMillis).append(" ms\n");
        }
        return sb.toString();
    }

    public void clearStats() {
        methodStats.clear();
    }
}
