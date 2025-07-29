package com.bhavesh.learn.cacheforge.controller;

import com.bhavesh.learn.cacheforge.domain.SimulationCommonParams;
import com.bhavesh.learn.cacheforge.domain.SimulationRequest;
import com.bhavesh.learn.cacheforge.domain.enums.CacheStrategy;
import com.bhavesh.learn.cacheforge.domain.enums.WorkloadPattern;
import com.bhavesh.learn.cacheforge.service.SimulatorService;
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
        return simulatorService.runSimulation(simulationRequest);
    }

    @PostMapping("/simulate/all")
    public Map<String, Object> simulateAllCacheBenchmark(@RequestBody SimulationCommonParams params) {
        // Override strategies and patterns with all enum values
        SimulationRequest allRequest = new SimulationRequest(params);
        return simulatorService.runSimulation(allRequest);
    }

    @PostMapping("/simulate/all-strategies")
    public Map<String, Object> simulateAllCacheBenchmark(@RequestParam WorkloadPattern pattern, @RequestBody SimulationCommonParams params) {
        // Override strategies with all enum values
        SimulationRequest allRequest = new SimulationRequest(pattern, params);
        return simulatorService.runSimulation(allRequest);
    }

    @PostMapping("/simulate/all-patterns")
    public Map<String, Object> simulateAllCacheBenchmark(@RequestParam CacheStrategy strategy, @RequestBody SimulationCommonParams params) {
        // Override patterns with all enum values
        SimulationRequest allRequest = new SimulationRequest(strategy, params);
        return simulatorService.runSimulation(allRequest);
    }
}