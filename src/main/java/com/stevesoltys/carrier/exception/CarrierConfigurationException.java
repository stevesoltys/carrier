package com.stevesoltys.carrier.exception;

/**
 * An exception that occurs when there is an error while loading the configuration.
 *
 * @author Steve Soltys
 */
public class CarrierConfigurationException extends Exception {

    public CarrierConfigurationException(String cause) {
        super("Error while loading configuration: " + cause);
    }
}
