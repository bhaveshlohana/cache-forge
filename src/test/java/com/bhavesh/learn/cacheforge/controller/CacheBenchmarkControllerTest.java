package com.bhavesh.learn.cacheforge.controller;

import com.bhavesh.learn.cacheforge.service.SimulatorService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CacheBenchmarkController.class)
class CacheBenchmarkControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private SimulatorService simulatorService;

    @Test
    void shouldSimulateCacheBenchmark() throws Exception {
        Map<String, Object> mockResult = new LinkedHashMap<>();
        mockResult.put("random-LRU", Map.of("Hit Count", 50));

        org.mockito.Mockito.when(simulatorService.runSimulation(org.mockito.ArgumentMatchers.any()))
                .thenReturn(mockResult);

        String requestBody = """
                {
                    "cacheStrategies": ["LRU"],
                    "cacheSize": 100,
                    "workloadPatterns": ["random"],
                    "iterations": 1000,
                    "keySpaceSize": 50,
                    "readWriteRatio": 0.7
                }
                """;

        mockMvc.perform(post("/api/cache/benchmark/simulate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.random-LRU").exists());
    }

    @Test
    void shouldSimulateAllCacheBenchmark() throws Exception {
        Map<String, Object> mockResult = new LinkedHashMap<>();
        mockResult.put("random-LRU", Map.of("Hit Count", 50));

        org.mockito.Mockito.when(simulatorService.runSimulation(org.mockito.ArgumentMatchers.any()))
                .thenReturn(mockResult);

        String requestBody = """
                {
                    "cacheSize": 100,
                    "iterations": 1000,
                    "keySpaceSize": 50,
                    "readWriteRatio": 0.7
                }
                """;

        mockMvc.perform(post("/api/cache/benchmark/simulate/all")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk());
    }

    @Test
    void shouldSimulateAllStrategies() throws Exception {
        Map<String, Object> mockResult = new LinkedHashMap<>();
        mockResult.put("random-LRU", Map.of("Hit Count", 50));

        org.mockito.Mockito.when(simulatorService.runSimulation(org.mockito.ArgumentMatchers.any()))
                .thenReturn(mockResult);

        String requestBody = """
                {
                    "cacheSize": 100,
                    "iterations": 1000,
                    "keySpaceSize": 50,
                    "readWriteRatio": 0.7
                }
                """;

        mockMvc.perform(post("/api/cache/benchmark/simulate/all-strategies")
                        .param("pattern", "random")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk());
    }

    @Test
    void shouldSimulateAllPatterns() throws Exception {
        Map<String, Object> mockResult = new LinkedHashMap<>();
        mockResult.put("random-LRU", Map.of("Hit Count", 50));

        org.mockito.Mockito.when(simulatorService.runSimulation(org.mockito.ArgumentMatchers.any()))
                .thenReturn(mockResult);

        String requestBody = """
                {
                    "cacheSize": 100,
                    "iterations": 1000,
                    "keySpaceSize": 50,
                    "readWriteRatio": 0.7
                }
                """;

        mockMvc.perform(post("/api/cache/benchmark/simulate/all-patterns")
                        .param("strategy", "LRU")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk());
    }
}
