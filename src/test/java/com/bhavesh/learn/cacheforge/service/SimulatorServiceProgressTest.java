package com.bhavesh.learn.cacheforge.service;

import com.bhavesh.learn.cacheforge.domain.CacheRequest;
import com.bhavesh.learn.cacheforge.domain.DoublyLinkedList;
import com.bhavesh.learn.cacheforge.domain.Node;
import com.bhavesh.learn.cacheforge.domain.SimulationConfig;
import com.bhavesh.learn.cacheforge.domain.enums.CacheStrategy;
import com.bhavesh.learn.cacheforge.domain.enums.OperationType;
import com.bhavesh.learn.cacheforge.domain.enums.WorkloadPattern;
import com.bhavesh.learn.cacheforge.factory.CacheFactory;
import com.bhavesh.learn.cacheforge.model.Cache;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class SimulatorServiceProgressTest {

    @Mock
    SimpMessagingTemplate messagingTemplate;

    @Test
    void shouldPublishProgressEventsDuringRun() throws Exception {
        SimulatorService service = new SimulatorService();

        // inject mocked messaging template and a simple meter registry
        Field mtField = SimulatorService.class.getDeclaredField("messagingTemplate");
        mtField.setAccessible(true);
        mtField.set(service, messagingTemplate);

        Field mrField = SimulatorService.class.getDeclaredField("meterRegistry");
        mrField.setAccessible(true);
        mrField.set(service, new SimpleMeterRegistry());

        // inject CacheFactory
        service.cacheFactory = new CacheFactory();

        // create a small simulation config and requests
        SimulationConfig config = new SimulationConfig(CacheStrategy.LRU, 3, WorkloadPattern.random, 10, 5, 0.5);

        // create 10 operations (mix of GET and PUT)
        List<CacheRequest> requests = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            if (i % 2 == 0) {
                requests.add(new CacheRequest(OperationType.PUT, i, "v" + i));
            } else {
                requests.add(new CacheRequest(OperationType.GET, i, null));
            }
        }

        // get a cache instance from the service
        Cache<Integer, String> cache = service.getCacheBasedOnStrategy(config);

        // run simulation
        service.runSimulationForCache(cache, requests, config);

        // verify that at least one message was sent and that a COMPLETED event was sent
        verify(messagingTemplate, atLeastOnce()).convertAndSend(eq("/topic/simulation"), ArgumentMatchers.any(Map.class));

        // verify that one of the sent messages had status COMPLETED
        verify(messagingTemplate, atLeastOnce()).convertAndSend(eq("/topic/simulation"), ArgumentMatchers.<Map>argThat(m -> {
            if (m == null) return false;
            Object status = m.get("status");
            return "COMPLETED".equals(status);
        }));
    }
}


