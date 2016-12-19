package com.stevesoltys.carrier.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * @author Steve Soltys
 */
@ResponseStatus(value = HttpStatus.NOT_FOUND)
public class CarrierAddressNotFoundException extends RuntimeException {

    public CarrierAddressNotFoundException(String cause) {
        super("Error while searching for masked address: '" + cause + "'");
    }
}
