package com.bhavesh.learn.cacheforge.exception;

/**
 * Thrown when a simulation encounters a runtime failure during execution.
 */
public class SimulationException extends RuntimeException {

    public SimulationException(String message) {
        super(message);
    }

    public SimulationException(String message, Throwable cause) {
        super(message, cause);
    }
}
