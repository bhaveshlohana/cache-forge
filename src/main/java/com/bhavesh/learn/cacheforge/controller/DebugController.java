package com.bhavesh.learn.cacheforge.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/debug")
public class DebugController {

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @PostMapping("/ws-test")
    public Map<String, Object> sendTest(@RequestParam(required = false) String runId) {
        Map<String, Object> event = new LinkedHashMap<>();
        event.put("progressPercent", 100);
        event.put("strategy", "DEBUG");
        event.put("pattern", "DEBUG");
        event.put("status", "COMPLETED");
        event.put("iterationsCompleted", 1);
        event.put("totalIterations", 1);

        String topic = (runId == null || runId.isBlank()) ? "/topic/simulation" : ("/topic/simulation/" + runId);
        messagingTemplate.convertAndSend(topic, event);

        Map<String, Object> resp = new LinkedHashMap<>();
        resp.put("sent", true);
        resp.put("event", event);
        return resp;
    }
}

