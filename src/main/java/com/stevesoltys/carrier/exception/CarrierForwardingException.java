package com.stevesoltys.carrier.exception;

/**
 * An exception that occurs when there is an error forwarding an e-mail.
 *
 * @author Steve Soltys
 */
public class CarrierForwardingException extends Exception {

    public CarrierForwardingException(String cause) {
        super("Error while forwarding mail: " + cause);
    }
}
