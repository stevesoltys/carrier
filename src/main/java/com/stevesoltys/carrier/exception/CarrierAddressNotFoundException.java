package com.stevesoltys.carrier.exception;

import com.stevesoltys.carrier.controller.CarrierRestController;
import com.stevesoltys.carrier.model.MaskedAddress;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * An exception that occurs when a {@link MaskedAddress} could not be found while querying the
 * {@link CarrierRestController}.
 *
 * @author Steve Soltys
 */
@ResponseStatus(value = HttpStatus.NOT_FOUND)
public class CarrierAddressNotFoundException extends RuntimeException {

    public CarrierAddressNotFoundException(String cause) {
        super("Error while searching for masked address: '" + cause + "'");
    }
}
