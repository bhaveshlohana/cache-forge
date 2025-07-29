package com.bhavesh.learn.cacheforge.service;

import com.bhavesh.learn.cacheforge.domain.CacheRequest;
import com.bhavesh.learn.cacheforge.domain.SimulationConfig;
import com.bhavesh.learn.cacheforge.generator.WorkLoadGenerator;
import com.bhavesh.learn.cacheforge.generator.impl.*;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class WorkloadGeneratorService {

    WorkLoadGenerator generator;

    public List<CacheRequest> generateCacheRequest(SimulationConfig simulationConfig) {
        switch (simulationConfig.pattern()) {
            case random -> generator = new RandomGenerator();
            case sequential -> generator = new SequentialGenerator();
            case zipfian -> generator = new ZipfianGenerator();
            case hotspot -> generator = new HotspotGenerator();
            case ttl -> generator = new TTLGenerator();
            case temporalHotspot -> generator = new TemporalHotspotGenerator();
            default -> generator = new RandomGenerator();
        }

        return generator.generate(simulationConfig.iterations(), simulationConfig.keySpaceSize(), simulationConfig.readWriteRatio());
    }
}
