package com.bhavesh.learn.cacheforge.factory;

import com.bhavesh.learn.cacheforge.domain.enums.WorkloadPattern;
import com.bhavesh.learn.cacheforge.generator.WorkLoadGenerator;
import com.bhavesh.learn.cacheforge.generator.impl.*;
import org.springframework.stereotype.Component;

/**
 * Factory for creating workload generators based on the configured pattern.
 * Centralizes generator instantiation logic previously embedded in
 * {@link com.bhavesh.learn.cacheforge.service.WorkloadGeneratorService}.
 */
@Component
public class WorkloadGeneratorFactory {

    /**
     * Creates a WorkLoadGenerator for the given workload pattern.
     *
     * @param pattern the workload pattern to generate
     * @return a new generator instance
     */
    public WorkLoadGenerator createGenerator(WorkloadPattern pattern) {
        return switch (pattern) {
            case random -> new RandomGenerator();
            case sequential -> new SequentialGenerator();
            case zipfian -> new ZipfianGenerator();
            case hotspot -> new HotspotGenerator();
            case ttl -> new TTLGenerator();
            case temporalHotspot -> new TemporalHotspotGenerator();
        };
    }
}
