package com.bhavesh.learn.cachepurge.controller;

import com.bhavesh.learn.cachepurge.domain.SimulationCommonParams;
import com.bhavesh.learn.cachepurge.domain.SimulationRequest;
import com.bhavesh.learn.cachepurge.domain.enums.CacheStrategy;
import com.bhavesh.learn.cachepurge.domain.enums.WorkloadPattern;
import com.bhavesh.learn.cachepurge.service.SimulatorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/cache/benchmark")
public class CacheBenchmarkController {
    @Autowired
    SimulatorService simulatorService;

    @PostMapping("/simulate")
    public Map<String, Object> simulateCacheBenchmark(@RequestBody SimulationRequest simulationRequest) {
        return simulatorService.rumSimulation(simulationRequest);
    }

    @PostMapping("/simulate/all")
    public Map<String, Object> simulateAllCacheBenchmark(@RequestBody SimulationCommonParams params) {
        // Override strategies and patterns with all enum values
        SimulationRequest allRequest = new SimulationRequest(params);
        return simulatorService.rumSimulation(allRequest);
    }

    @PostMapping("/simulate/all-strategies")
    public Map<String, Object> simulateAllCacheBenchmark(@RequestParam WorkloadPattern pattern, @RequestBody SimulationCommonParams params) {
        // Override strategies with all enum values
        SimulationRequest allRequest = new SimulationRequest(pattern, params);
        return simulatorService.rumSimulation(allRequest);
    }

    @PostMapping("/simulate/all-patterns")
    public Map<String, Object> simulateAllCacheBenchmark(@RequestParam CacheStrategy strategy, @RequestBody SimulationCommonParams params) {
        // Override patterns with all enum values
        SimulationRequest allRequest = new SimulationRequest(strategy, params);
        return simulatorService.rumSimulation(allRequest);
    }
}