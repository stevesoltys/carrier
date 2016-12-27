package com.stevesoltys.carrier.configuration;

import com.stevesoltys.carrier.exception.CarrierConfigurationException;
import com.stevesoltys.carrier.repository.CarrierConfigurationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.json.GsonJsonParser;
import org.springframework.stereotype.Component;

import java.io.*;
import java.util.Map;

/**
 * The Carrier configuration loader. Loads the configuration file and initializes the parsed configuration for every
 * {@link CarrierConfiguration} in the {@link CarrierConfigurationRepository}.
 *
 * @author Steve Soltys
 */
@Component
public class CarrierConfigurationLoader {

    /**
     * The default configuration directory for this loader.
     */
    public static final String CARRIER_CONFIGURATION_DIRECTORY = System.getProperty("user.home") + "/.config/carrier";

    /**
     * The {@link CarrierConfiguration} repository.
     */
    private final CarrierConfigurationRepository configurationRepository;

    @Autowired
    public CarrierConfigurationLoader(CarrierConfigurationRepository configurationRepository) {
        this.configurationRepository = configurationRepository;
    }

    /**
     * Runs this configuration loader. Parses the JSON configuration file and runs the
     * {@link CarrierConfiguration#initialize(Map)} function for all configuration instances in the
     * {@link CarrierConfigurationRepository}.
     *
     * @throws CarrierConfigurationException If there is an error while loading the configuration file.
     */
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

        for (CarrierConfiguration configuration : configurationRepository.getConfigurationSet()) {
            configuration.initialize(configurationMap);
        }
    }
}
