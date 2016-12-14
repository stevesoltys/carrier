package com.stevesoltys.carrier.repository;

import com.stevesoltys.carrier.configuration.CarrierConfiguration;
import org.springframework.stereotype.Repository;

import java.util.HashSet;
import java.util.Set;

/**
 * @author Steve Soltys
 */
@Repository
public class CarrierConfigurationRepository {

    private final Set<CarrierConfiguration> repository = new HashSet<>();

    public void register(CarrierConfiguration carrierConfiguration) {
        repository.add(carrierConfiguration);
    }

    public Set<CarrierConfiguration> getConfigurationSet() {
        return repository;
    }
}
