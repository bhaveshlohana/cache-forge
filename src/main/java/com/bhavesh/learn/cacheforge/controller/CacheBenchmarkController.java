package com.bhavesh.learn.cacheforge.controller;

import com.bhavesh.learn.cacheforge.domain.SimulationCommonParams;
import com.bhavesh.learn.cacheforge.domain.SimulationRequest;
import com.bhavesh.learn.cacheforge.domain.enums.CacheStrategy;
import com.bhavesh.learn.cacheforge.domain.enums.WorkloadPattern;
import com.bhavesh.learn.cacheforge.service.SimulatorService;
import com.bhavesh.learn.cacheforge.validation.SimulationRequestValidator;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/cache/benchmark")
@Tag(name = "Cache Benchmark", description = "Endpoints for running cache eviction strategy simulations and benchmarks")
public class CacheBenchmarkController {

    @Autowired
    SimulatorService simulatorService;

    @PostMapping("/simulate")
    @Operation(
            summary = "Run cache simulation",
            description = "Runs a simulation for the specified cache strategies, workload patterns, and configuration. "
                    + "Optionally provide a runId to receive real-time WebSocket progress updates on /topic/simulation/{runId}."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Simulation completed successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid simulation parameters")
    })
    public Map<String, Object> simulateCacheBenchmark(
            @RequestBody SimulationRequest simulationRequest,
            @Parameter(description = "Optional run ID for WebSocket progress tracking")
            @RequestParam(required = false) String runId) {

        SimulationRequestValidator.validate(simulationRequest);

        if (runId == null || runId.isBlank()) {
            return simulatorService.runSimulation(simulationRequest);
        }
        return simulatorService.runSimulation(simulationRequest, runId);
    }

    @PostMapping("/simulate/all")
    @Operation(
            summary = "Run simulation with all strategies and patterns",
            description = "Runs a simulation across all available cache strategies and workload patterns using the provided common parameters."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Simulation completed successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid simulation parameters")
    })
    public Map<String, Object> simulateAllCacheBenchmark(@RequestBody SimulationCommonParams params) {
        SimulationRequestValidator.validate(params);
        SimulationRequest allRequest = new SimulationRequest(params);
        return simulatorService.runSimulation(allRequest);
    }

    @PostMapping("/simulate/all-strategies")
    @Operation(
            summary = "Run simulation with all strategies for a specific pattern",
            description = "Runs a simulation across all cache strategies for the given workload pattern."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Simulation completed successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid simulation parameters")
    })
    public Map<String, Object> simulateAllCacheBenchmark(
            @Parameter(description = "Workload pattern to use", required = true)
            @RequestParam WorkloadPattern pattern,
            @RequestBody SimulationCommonParams params) {

        SimulationRequestValidator.validate(params);
        SimulationRequest allRequest = new SimulationRequest(pattern, params);
        return simulatorService.runSimulation(allRequest);
    }

    @PostMapping("/simulate/all-patterns")
    @Operation(
            summary = "Run simulation with all patterns for a specific strategy",
            description = "Runs a simulation across all workload patterns for the given cache strategy."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Simulation completed successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid simulation parameters")
    })
    public Map<String, Object> simulateAllCacheBenchmark(
            @Parameter(description = "Cache strategy to use", required = true)
            @RequestParam CacheStrategy strategy,
            @RequestBody SimulationCommonParams params) {

        SimulationRequestValidator.validate(params);
        SimulationRequest allRequest = new SimulationRequest(strategy, params);
        return simulatorService.runSimulation(allRequest);
    }

    @PostMapping("/simulate/concurrent")
    @Operation(
            summary = "Run concurrent cache simulation",
            description = "Runs a thread-safe simulation where the workload is distributed across multiple threads. "
                    + "Each cache is wrapped in a ConcurrentCacheDecorator for thread safety."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Concurrent simulation completed successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid simulation parameters"),
            @ApiResponse(responseCode = "500", description = "Simulation runtime failure")
    })
    public Map<String, Object> simulateConcurrentBenchmark(
            @RequestBody SimulationRequest simulationRequest,
            @Parameter(description = "Number of threads to use for concurrent execution")
            @RequestParam(defaultValue = "4") int threads,
            @Parameter(description = "Optional run ID for WebSocket progress tracking")
            @RequestParam(required = false) String runId) {

        SimulationRequestValidator.validate(simulationRequest);

        if (runId == null || runId.isBlank()) {
            return simulatorService.runConcurrentSimulation(simulationRequest, threads);
        }
        return simulatorService.runConcurrentSimulation(simulationRequest, threads, runId);
    }

    @PostMapping("/simulate/async")
    @Operation(
            summary = "Run async cache simulation",
            description = "Runs all strategy×pattern combinations in parallel using CompletableFuture for maximum throughput."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Async simulation completed successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid simulation parameters")
    })
    public Map<String, Object> simulateAsyncBenchmark(
            @RequestBody SimulationRequest simulationRequest,
            @Parameter(description = "Optional run ID for WebSocket progress tracking")
            @RequestParam(required = false) String runId) {

        SimulationRequestValidator.validate(simulationRequest);

        if (runId == null || runId.isBlank()) {
            return simulatorService.runSimulationAsync(simulationRequest);
        }
        return simulatorService.runSimulationAsync(simulationRequest, runId);
    }
}