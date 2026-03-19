package com.bhavesh.learn.cacheforge.factory;

import com.bhavesh.learn.cacheforge.domain.enums.WorkloadPattern;
import com.bhavesh.learn.cacheforge.generator.WorkLoadGenerator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static org.junit.jupiter.api.Assertions.*;

class WorkloadGeneratorFactoryTest {

    private WorkloadGeneratorFactory factory;

    @BeforeEach
    void setUp() {
        factory = new WorkloadGeneratorFactory();
    }

    @ParameterizedTest
    @EnumSource(WorkloadPattern.class)
    void shouldCreateGeneratorForAllPatterns(WorkloadPattern pattern) {
        WorkLoadGenerator generator = factory.createGenerator(pattern);
        assertNotNull(generator);
    }

    @Test
    void shouldCreateRandomGenerator() {
        WorkLoadGenerator generator = factory.createGenerator(WorkloadPattern.random);
        assertNotNull(generator);
        assertFalse(generator.generate(10, 5, 0.5).isEmpty());
    }

    @Test
    void shouldCreateSequentialGenerator() {
        WorkLoadGenerator generator = factory.createGenerator(WorkloadPattern.sequential);
        assertNotNull(generator);
        assertFalse(generator.generate(10, 5, 0.5).isEmpty());
    }

    @Test
    void shouldCreateZipfianGenerator() {
        WorkLoadGenerator generator = factory.createGenerator(WorkloadPattern.zipfian);
        assertNotNull(generator);
        assertFalse(generator.generate(10, 5, 0.5).isEmpty());
    }

    @Test
    void shouldCreateHotspotGenerator() {
        WorkLoadGenerator generator = factory.createGenerator(WorkloadPattern.hotspot);
        assertNotNull(generator);
        assertFalse(generator.generate(10, 5, 0.5).isEmpty());
    }

    @Test
    void shouldCreateTTLGenerator() {
        WorkLoadGenerator generator = factory.createGenerator(WorkloadPattern.ttl);
        assertNotNull(generator);
        assertFalse(generator.generate(10, 5, 0.5).isEmpty());
    }

    @Test
    void shouldCreateTemporalHotspotGenerator() {
        WorkLoadGenerator generator = factory.createGenerator(WorkloadPattern.temporalHotspot);
        assertNotNull(generator);
        assertFalse(generator.generate(10, 5, 0.5).isEmpty());
    }
}
