package com.stevesoltys.carrier.configuration;

import com.stevesoltys.carrier.exception.CarrierConfigurationException;
import com.stevesoltys.carrier.repository.CarrierConfigurationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.Map;

/**
 * A generic configuration class. Classes that extend this one are automatically registered to the
 * {@link CarrierConfigurationRepository}.
 *
 * @author Steve Soltys
 */
@Component
public abstract class CarrierConfiguration {

    /**
     * The configuration repository.
     */
    @Autowired
    private CarrierConfigurationRepository configurationRepository;

    /**
     * Registers this class to the {@link CarrierConfigurationRepository}.
     */
    @PostConstruct
    public void register() {
        configurationRepository.register(this);
    }

    /**
     * Initializes this configuration, given the parsed configuration map.
     *
     * @param configuration The parsed configuration map.
     * @throws CarrierConfigurationException If there is an error while initializing this section of the configuration.
     */
    abstract void initialize(Map<String, Object> configuration) throws CarrierConfigurationException;
}
