package com.stevesoltys.carrier.configuration;

import com.stevesoltys.carrier.exception.CarrierConfigurationException;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Configuration that is used for the SMTP client.
 *
 * @author Steve Soltys
 */
@Component
public class SMTPClientConfiguration extends CarrierConfiguration {

    /**
     * The client configuration key.
     */
    private static final String CLIENT_CONFIGURATION_KEY = "client";

    /**
     * The DKIM flag configuration key.
     */
    private static final String DKIM_FLAG_KEY = "dkim";

    /**
     * The DKIM selector configuration key.
     */
    private static final String DKIM_SELECTOR_KEY = "dkim_selector";

    /**
     * The DKIM private key file configuration key.
     */
    private static final Object DKIM_PRIVATE_KEY = "dkim_private_key";

    /**
     * The default private key file location.
     */
    private static final String DEFAULT_PRIVATE_KEY_LOCATION =
            CarrierConfigurationLoader.CARRIER_CONFIGURATION_DIRECTORY + "/dkim.der";

    /**
     * The domain configuration key.
     */
    private static final String DOMAIN_KEY = "domain";

    /**
     * The SSL flag configuration key.
     */
    private static final String SSL_KEY = "ssl";

    /**
     *  The STARTTLS flag configuration key.
     */
    private static final String START_TLS_KEY = "starttls";

    /**
     * The keystore file location configuration key.
     */
    private static final String KEYSTORE_KEY = "keystore";

    /**
     * The keystore password configuration key.
     */
    private static final String KEYSTORE_PASSWORD_KEY = "keystore_password";

    /**
     * The DKIM flag.
     */
    private boolean dkimEnabled;

    /**
     * The DKIM domain selector.
     */
    private String dkimSelector;

    /**
     * The DKIM private key file location.
     */
    private String privateKeyFile;

    /**
     * The domain address.
     */
    private String domain;

    /**
     * The STARTTLS flag.
     */
    private boolean startTls;

    /**
     * The SSL flag.
     */
    private boolean ssl;

    @Override
    @SuppressWarnings("unchecked")
    protected void initialize(Map<String, Object> configuration) throws CarrierConfigurationException {
        configuration = (Map<String, Object>) configuration.getOrDefault(CLIENT_CONFIGURATION_KEY, null);

        if (configuration == null) {
            throw new CarrierConfigurationException("Could not find 'client' configuration entry.");
        }

        this.dkimEnabled = (boolean) configuration.getOrDefault(DKIM_FLAG_KEY, false);
        this.dkimSelector = (String) configuration.getOrDefault(DKIM_SELECTOR_KEY, null);
        this.privateKeyFile = (String) configuration.getOrDefault(DKIM_PRIVATE_KEY, DEFAULT_PRIVATE_KEY_LOCATION);

        this.domain = (String) configuration.getOrDefault(DOMAIN_KEY, null);
        this.startTls = (boolean) configuration.getOrDefault(START_TLS_KEY, true);
        this.ssl = (boolean) configuration.getOrDefault(SSL_KEY, false);

        System.setProperty("mail.smtp.starttls.enable", Boolean.toString(startTls));
        System.setProperty("mail.smtp.ssl.enable", Boolean.toString(ssl));

        String keystore = (String) configuration.getOrDefault(KEYSTORE_KEY, null);
        String keystorePassword = (String) configuration.getOrDefault(KEYSTORE_PASSWORD_KEY, null);

        if (keystore != null && keystorePassword != null) {
            System.setProperty("javax.net.ssl.keyStore", keystore);
            System.setProperty("javax.net.ssl.keyStorePassword", keystorePassword);
        }
    }

    public boolean isDkimEnabled() {
        return dkimEnabled;
    }

    public String getDomain() {
        return domain;
    }

    public String getDkimSelector() {
        return dkimSelector;
    }

    public String getPrivateKeyFile() {
        return privateKeyFile;
    }

    public boolean isSslEnabled() {
        return ssl;
    }

    public boolean isStartTlsEnabled() {
        return startTls;
    }

}
