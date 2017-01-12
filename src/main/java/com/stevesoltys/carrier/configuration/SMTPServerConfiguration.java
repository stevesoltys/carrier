package com.stevesoltys.carrier.configuration;

import com.stevesoltys.carrier.exception.CarrierConfigurationException;
import org.springframework.stereotype.Component;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Map;

/**
 * Configuration that is used for the SMTP server.
 *
 * @author Steve Soltys
 */
@Component
public class SMTPServerConfiguration extends CarrierConfiguration {

    /**
     * The server map configuration key.
     */
    private static final String SERVER_CONFIGURATION_KEY = "server";

    /**
     * The localhost address configuration key.
     */
    private static final String LOCALHOST_KEY = "localhost";

    /**
     * The port configuration key.
     */
    private static final String PORT_KEY = "port";

    /**
     * The force TLS configuration key.
     */
    private static final String FORCE_TLS_KEY = "force_tls";

    /**
     * The server port.
     */
    private int port;

    /**
     * The force TLS flag.
     */
    private boolean forceTls;

    @Override
    @SuppressWarnings("unchecked")
    protected void initialize(Map<String, Object> configuration) throws CarrierConfigurationException {

        configuration = (Map<String, Object>) configuration.getOrDefault(SERVER_CONFIGURATION_KEY, null);

        if(configuration == null) {
            throw new CarrierConfigurationException("Could not find 'server' configuration entry.");
        }

        try {
            String defaultLocalhost = InetAddress.getLocalHost().getHostAddress();
            String localhost = (String) configuration.getOrDefault(LOCALHOST_KEY, defaultLocalhost);

            System.setProperty("mail.smtp.localhost", localhost);

        } catch (UnknownHostException e) {
            throw new CarrierConfigurationException("Error obtaining localhost configuration: " + e.getMessage());
        }

        this.port = (int) Math.round((double) configuration.getOrDefault(PORT_KEY, 25.0));
        this.forceTls = (boolean) configuration.getOrDefault(FORCE_TLS_KEY, true);
    }

    /**
     * Gets the server port.
     *
     * @return The server port.
     */
    public int getPort() {
        return port;
    }

    /**
     * Gets the forced TLS server flag.
     *
     * @return The force TLS flag.
     */
    public boolean isTlsForced() {
        return forceTls;
    }
}
