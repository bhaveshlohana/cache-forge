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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CacheBenchmarkController.class)
class CacheBenchmarkControllerRunIdTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private SimulatorService simulatorService;

    @Test
    void shouldForwardRunIdToServiceForSimulate() throws Exception {
        Map<String, Object> mockResult = new LinkedHashMap<>();
        when(simulatorService.runSimulation(any(), anyString())).thenReturn(mockResult);

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

        mockMvc.perform(post("/api/cache/benchmark/simulate?runId=my-run-123")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk());

        // verify the controller invoked the service overload with runId
        verify(simulatorService).runSimulation(any(), eq("my-run-123"));
    }
}


