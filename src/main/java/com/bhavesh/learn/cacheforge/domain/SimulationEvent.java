package com.bhavesh.learn.cacheforge.domain;

import java.util.Map;

/**
 * Event published to WebSocket during simulation for real-time progress updates.
 */
public record SimulationEvent(
        String strategy,
        String pattern,
        int progressPercent,
        String status,
        Map<String, Object> currentStats
) {}
