package com.stevesoltys.carrier.controller;

import com.stevesoltys.carrier.exception.CarrierAddressNotFoundException;
import com.stevesoltys.carrier.model.MaskedAddress;
import com.stevesoltys.carrier.repository.MaskedAddressRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Optional;

/**
 * @author Steve Soltys
 */
@Controller
@RequestMapping
public class CarrierRestController {

    private static final String CREATE_MASKED_ADDRESS = "/create";

    private static final String DELETE_MASKED_ADDRESS = "/delete";

    private static final String GET_MASKED_ADDRESS = "/get";

    private MaskedAddressRepository maskedAddressRepository;

    @Autowired
    public CarrierRestController(MaskedAddressRepository maskedAddressRepository) {
        this.maskedAddressRepository = maskedAddressRepository;
    }

    @RequestMapping(CREATE_MASKED_ADDRESS)
    @ResponseBody
    public void create(@RequestParam(value = "address") String address,
                       @RequestParam(value = "destination") String destination) {

        Optional<MaskedAddress> maskedAddressOptional = maskedAddressRepository.findByAddress(address);

        if(maskedAddressOptional.isPresent()) {
            maskedAddressRepository.delete(maskedAddressOptional.get());
        }

        maskedAddressRepository.save(new MaskedAddress(address, destination));
    }

    @RequestMapping(GET_MASKED_ADDRESS)
    @ResponseBody
    public MaskedAddress get(@RequestParam(value = "address") String address) {

        Optional<MaskedAddress> maskedAddressOptional = maskedAddressRepository.findByAddress(address);

        if(!maskedAddressOptional.isPresent()) {
            throw new CarrierAddressNotFoundException(address);
        }

        return maskedAddressOptional.get();
    }

    @RequestMapping(DELETE_MASKED_ADDRESS)
    @ResponseBody
    public void delete(@RequestParam(value = "address") String address) {

        Optional<MaskedAddress> maskedAddressOptional = maskedAddressRepository.findByAddress(address);

        if(!maskedAddressOptional.isPresent()) {
            throw new CarrierAddressNotFoundException(address);
        }

        maskedAddressRepository.delete(maskedAddressOptional.get());
    }
}
