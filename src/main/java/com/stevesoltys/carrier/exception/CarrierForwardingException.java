package com.stevesoltys.carrier.exception;

/**
 * @author Steve Soltys
 */
public class CarrierForwardingException extends Exception {

    public CarrierForwardingException(String cause) {
        super("Error while forwarding mail: " + cause);
    }
}
