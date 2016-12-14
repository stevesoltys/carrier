package com.stevesoltys.carrier.configuration;

import com.stevesoltys.carrier.exception.CarrierConfigurationException;
import org.springframework.stereotype.Component;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Map;

/**
 * @author Steve Soltys
 */
@Component
public class SMTPServerConfiguration extends CarrierConfiguration {

    private static final String SERVER_CONFIGURATION_KEY = "server";

    private static final String LOCALHOST_KEY = "localhost";

    private static final String PORT_KEY = "port";

    private static final String FORCE_TLS_KEY = "force_tls";

    private String localhost;

    private int port;

    private boolean forceTls;

    @Override
    @SuppressWarnings("unchecked")
    void initialize(Map<String, Object> configuration) throws CarrierConfigurationException {

        configuration = (Map<String, Object>) configuration.getOrDefault(SERVER_CONFIGURATION_KEY, null);

        if(configuration == null) {
            throw new CarrierConfigurationException("Could not find 'server' configuration entry.");
        }

        try {
            String defaultLocalhost = InetAddress.getLocalHost().getHostAddress();
            this.localhost = (String) configuration.getOrDefault(LOCALHOST_KEY, defaultLocalhost);

            System.setProperty("mail.smtp.localhost", localhost);

        } catch (UnknownHostException e) {
            throw new CarrierConfigurationException("Error obtaining localhost configuration: " + e.getMessage());
        }

        this.port = (int) Math.round((double) configuration.getOrDefault(PORT_KEY, 25.0));
        this.forceTls = (boolean) configuration.getOrDefault(FORCE_TLS_KEY, true);
    }

    public String getLocalhost() {
        return localhost;
    }

    public int getPort() {
        return port;
    }

    public boolean isTlsForced() {
        return forceTls;
    }
}
