package com.bhavesh.learn.cacheforge.service;

import com.bhavesh.learn.cacheforge.domain.CacheRequest;
import com.bhavesh.learn.cacheforge.domain.SimulationConfig;
import com.bhavesh.learn.cacheforge.factory.WorkloadGeneratorFactory;
import com.bhavesh.learn.cacheforge.generator.WorkLoadGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class WorkloadGeneratorService {

    @Autowired
    WorkloadGeneratorFactory generatorFactory;

    public List<CacheRequest> generateCacheRequest(SimulationConfig simulationConfig) {
        WorkLoadGenerator generator = generatorFactory.createGenerator(simulationConfig.pattern());
        return generator.generate(simulationConfig.iterations(), simulationConfig.keySpaceSize(), simulationConfig.readWriteRatio());
    }
}
