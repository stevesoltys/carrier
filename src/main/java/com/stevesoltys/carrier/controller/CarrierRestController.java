package com.stevesoltys.carrier.controller;

import com.stevesoltys.carrier.exception.CarrierAddressNotFoundException;
import com.stevesoltys.carrier.model.MaskedAddress;
import com.stevesoltys.carrier.repository.MaskedAddressRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

/**
 * Contains the REST API for creating, modifying, and deleting {@link MaskedAddress} entries.
 *
 * @author Steve Soltys
 */
@RestController
@RequestMapping
public class CarrierRestController {

    /**
     * REST mapping for creating masked address entries.
     */
    private static final String CREATE_MASKED_ADDRESS = "/create";

    /**
     * REST mapping for getting masked address entries.
     */
    private static final String GET_MASKED_ADDRESS = "/get";

    /**
     * REST mapping for deleting masked address entries.
     */
    private static final String DELETE_MASKED_ADDRESS = "/delete";

    /**
     * The masked address repository.
     */
    private MaskedAddressRepository maskedAddressRepository;

    @Autowired
    public CarrierRestController(MaskedAddressRepository maskedAddressRepository) {
        this.maskedAddressRepository = maskedAddressRepository;
    }

    /**
     * Creates a {@link MaskedAddress}.
     *
     * @param address The incoming address.
     * @param destination The outgoing address.
     */
    @RequestMapping(CREATE_MASKED_ADDRESS)
    @ResponseBody
    public void create(@RequestParam(value = "address") String address,
                       @RequestParam(value = "destination") String destination) {

        Optional<MaskedAddress> maskedAddressOptional = maskedAddressRepository.findByAddress(address);

        if (maskedAddressOptional.isPresent()) {
            maskedAddressRepository.delete(maskedAddressOptional.get());
        }

        maskedAddressRepository.save(new MaskedAddress(address, destination));
    }

    /**
     * Gets a masked address.
     *
     * @param address The incoming address of the masked address.
     * @return The masked address.
     */
    @RequestMapping(GET_MASKED_ADDRESS)
    @ResponseBody
    public MaskedAddress get(@RequestParam(value = "address") String address) {

        Optional<MaskedAddress> maskedAddressOptional = maskedAddressRepository.findByAddress(address);

        if (!maskedAddressOptional.isPresent()) {
            throw new CarrierAddressNotFoundException(address);
        }

        return maskedAddressOptional.get();
    }

    /**
     * Deletes a {@link MaskedAddress}.
     *
     * @param address The incoming address.
     * @return The masked address.
     */
    @RequestMapping(DELETE_MASKED_ADDRESS)
    @ResponseBody
    public MaskedAddress delete(@RequestParam(value = "address") String address) {

        Optional<MaskedAddress> maskedAddressOptional = maskedAddressRepository.findByAddress(address);

        if (!maskedAddressOptional.isPresent()) {
            throw new CarrierAddressNotFoundException(address);
        }

        maskedAddressRepository.delete(maskedAddressOptional.get());

        return maskedAddressOptional.get();
    }
}
