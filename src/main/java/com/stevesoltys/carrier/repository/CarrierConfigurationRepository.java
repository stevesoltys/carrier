package com.stevesoltys.carrier.repository;

import com.stevesoltys.carrier.configuration.CarrierConfiguration;
import org.springframework.stereotype.Repository;

import java.util.HashSet;
import java.util.Set;

/**
 * The {@link CarrierConfiguration} repository.
 *
 * @author Steve Soltys
 */
@Repository
public class CarrierConfigurationRepository {

    /**
     * The configuration repository.
     */
    private final Set<CarrierConfiguration> repository = new HashSet<>();

    /**
     * Registers the given Carrier configuration component.
     *
     * @param carrierConfiguration The Carrier configuration component.
     */
    public void register(CarrierConfiguration carrierConfiguration) {
        repository.add(carrierConfiguration);
    }

    /**
     * Gets the set of Carrier configuration components.
     *
     * @return The set of configuration components.
     */
    public Set<CarrierConfiguration> getConfigurationSet() {
        return repository;
    }
}
