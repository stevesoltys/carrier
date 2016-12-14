package com.stevesoltys.carrier.configuration;

import com.stevesoltys.carrier.exception.CarrierConfigurationException;
import com.stevesoltys.carrier.repository.CarrierConfigurationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.Map;

/**
 * @author Steve Soltys
 */
@Component
public abstract class CarrierConfiguration {

    @Autowired
    private CarrierConfigurationRepository configurationRepository;

    @PostConstruct
    public void register() {
        configurationRepository.register(this);
    }

    abstract void initialize(Map<String, Object> configuration) throws CarrierConfigurationException;
}
