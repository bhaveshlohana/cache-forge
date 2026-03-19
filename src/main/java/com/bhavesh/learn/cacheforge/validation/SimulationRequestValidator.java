package com.bhavesh.learn.cacheforge.validation;

import com.bhavesh.learn.cacheforge.domain.SimulationRequest;
import com.bhavesh.learn.cacheforge.domain.SimulationCommonParams;
import com.bhavesh.learn.cacheforge.exception.InvalidSimulationConfigException;

import java.util.ArrayList;
import java.util.List;

/**
 * Centralized validator for simulation parameters.
 * Collects all validation errors and throws a single exception with a descriptive message.
 */
public final class SimulationRequestValidator {

    private SimulationRequestValidator() {
        // Utility class — no instances
    }

    /**
     * Validates a SimulationRequest and throws {@link InvalidSimulationConfigException}
     * if any parameter is invalid.
     */
    public static void validate(SimulationRequest request) {
        List<String> errors = new ArrayList<>();

        if (request.cacheSize() <= 0) {
            errors.add("cacheSize must be greater than 0");
        }
        if (request.iterations() <= 0) {
            errors.add("iterations must be greater than 0");
        }
        if (request.keySpaceSize() <= 0) {
            errors.add("keySpaceSize must be greater than 0");
        }
        if (request.readWriteRatio() < 0.0 || request.readWriteRatio() > 1.0) {
            errors.add("readWriteRatio must be between 0.0 and 1.0");
        }
        if (request.cacheStrategies() == null || request.cacheStrategies().isEmpty()) {
            errors.add("At least one cache strategy must be selected");
        }
        if (request.workloadPatterns() == null || request.workloadPatterns().isEmpty()) {
            errors.add("At least one workload pattern must be selected");
        }
        if (request.ttlEnabled() && request.ttlDuration() <= 0) {
            errors.add("TTL duration must be greater than 0 when TTL is enabled");
        }
        if (request.ttlEnabled() && request.ttlUnit() == null) {
            errors.add("TTL unit must not be null when TTL is enabled");
        }

        if (!errors.isEmpty()) {
            throw new InvalidSimulationConfigException(String.join("; ", errors));
        }
    }

    /**
     * Validates SimulationCommonParams used by the /simulate/all endpoints.
     */
    public static void validate(SimulationCommonParams params) {
        List<String> errors = new ArrayList<>();

        if (params.cacheSize() <= 0) {
            errors.add("cacheSize must be greater than 0");
        }
        if (params.iterations() <= 0) {
            errors.add("iterations must be greater than 0");
        }
        if (params.keySpaceSize() <= 0) {
            errors.add("keySpaceSize must be greater than 0");
        }
        if (params.readWriteRatio() < 0.0 || params.readWriteRatio() > 1.0) {
            errors.add("readWriteRatio must be between 0.0 and 1.0");
        }
        if (params.ttlEnabled() && params.ttlDuration() <= 0) {
            errors.add("TTL duration must be greater than 0 when TTL is enabled");
        }

        if (!errors.isEmpty()) {
            throw new InvalidSimulationConfigException(String.join("; ", errors));
        }
    }
}
