package com.stevesoltys.carrier.exception;

/**
 * @author Steve Soltys
 */
public class CarrierConfigurationException extends Exception {

    public CarrierConfigurationException(String cause) {
        super("Error while loading configuration: " + cause);
    }
}
