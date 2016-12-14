package com.stevesoltys.carrier.configuration;

import com.stevesoltys.carrier.exception.CarrierConfigurationException;
import com.stevesoltys.carrier.repository.CarrierConfigurationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.json.GsonJsonParser;
import org.springframework.stereotype.Component;

import java.io.*;
import java.util.Map;

/**
 * @author Steve Soltys
 */
@Component
public class CarrierConfigurationLoader {

    public static final String CARRIER_CONFIGURATION_DIRECTORY = System.getProperty("user.home") + "/.config/carrier";

    private final CarrierConfigurationRepository configurationRepository;

    @Autowired
    public CarrierConfigurationLoader(CarrierConfigurationRepository configurationRepository) {
        this.configurationRepository = configurationRepository;
    }

    public void run() throws CarrierConfigurationException {

        File configurationDirectory = new File(CARRIER_CONFIGURATION_DIRECTORY);

        if (!configurationDirectory.exists() && !configurationDirectory.mkdirs()) {
            throw new CarrierConfigurationException("Could not create Carrier configuration directory.");
        }

        File configurationFile = new File(configurationDirectory, "config.json");

        StringBuilder builder = new StringBuilder();

        try (BufferedReader bufferedReader = new BufferedReader(new FileReader(configurationFile))) {

            String line;
            while ((line = bufferedReader.readLine()) != null) {
                builder.append(line);
            }

        } catch (FileNotFoundException e) {
            throw new CarrierConfigurationException("Could not find Carrier configuration file.");
        } catch (IOException e) {
            throw new CarrierConfigurationException("Could not read Carrier configuration file: " + e.getMessage());
        }

        String configurationString = builder.toString();

        GsonJsonParser parser = new GsonJsonParser();
        Map<String, Object> configurationMap = parser.parseMap(configurationString);

        for(CarrierConfiguration configuration : configurationRepository.getConfigurationSet()) {
            configuration.initialize(configurationMap);
        }
    }
}
