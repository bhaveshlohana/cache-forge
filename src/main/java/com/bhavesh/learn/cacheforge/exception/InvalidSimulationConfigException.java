package com.bhavesh.learn.cacheforge.exception;

/**
 * Thrown when simulation configuration parameters are invalid.
 */
public class InvalidSimulationConfigException extends RuntimeException {

    public InvalidSimulationConfigException(String message) {
        super(message);
    }

    public InvalidSimulationConfigException(String message, Throwable cause) {
        super(message, cause);
    }
}
