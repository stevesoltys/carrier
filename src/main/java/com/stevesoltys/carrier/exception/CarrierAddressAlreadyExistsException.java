package com.stevesoltys.carrier.exception;

import com.stevesoltys.carrier.controller.CarrierRestController;
import com.stevesoltys.carrier.model.MaskedAddress;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * An exception that occurs when a {@link MaskedAddress} already exists when being created in the
 * {@link CarrierRestController}.
 *
 * @author Steve Soltys
 */
@ResponseStatus(value = HttpStatus.CONFLICT)
public class CarrierAddressAlreadyExistsException extends RuntimeException {

    public CarrierAddressAlreadyExistsException(String cause) {
        super("Error while creating masked address: '" + cause + "', address already exists");
    }
}
