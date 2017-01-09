package com.stevesoltys.carrier.configuration;

import com.stevesoltys.carrier.repository.CarrierConfigurationRepository;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Tests for the {@link CarrierConfiguration} modules.
 *
 * @author Steve Soltys
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = CarrierConfigurationTestsContext.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class CarrierConfigurationTests {

    /**
     * The mocked component attribute repository.
     */
    @Autowired
    private CarrierConfigurationRepository configurationRepository;

    /**
     * Tests failing of {@link CarrierConfiguration#initialize(Map)} on each entry in the
     * {@link CarrierConfigurationRepository} when the given configuration is invalid.
     */
    @Test(expected = Exception.class)
    public void testInvalidConfiguration() throws Exception {
        Map<String, Object> configuration = new HashMap<>();
        configuration.put("wrong", "config");

        for (CarrierConfiguration configurationEntry : configurationRepository.getConfigurationSet()) {
            configurationEntry.initialize(configuration);
        }
    }

    /**
     * Tests {@link CarrierConfiguration#initialize(Map)} on each entry in the {@link CarrierConfigurationRepository}
     * when the configuration is valid.
     */
    @Test
    public void testValidConfiguration() throws Exception {

        Map<String, Object> configuration = new HashMap<>();

        // Server configuration
        Map<String, Object> server = new HashMap<>();
        server.put("localhost", "server.mydomain.com");
        server.put("force_tls", true);

        configuration.put("server", server);

        // Client configuration
        Map<String, Object> client = new HashMap<>();
        client.put("dkim", true);
        client.put("dkim_selector", "mail");
        client.put("dkim_private_key", "/some/path");

        client.put("domain", "mydomain.com");
        client.put("keystore", "/some/path");
        client.put("keystore_password", "1234567");

        configuration.put("client", client);

        // Account instances
        Map<String, Object> account = new HashMap<>();
        account.put("username", "username");
        account.put("password", "password");

        configuration.put("accounts", Collections.singletonList(account));

        for (CarrierConfiguration configurationEntry : configurationRepository.getConfigurationSet()) {
            configurationEntry.initialize(configuration);
        }
    }

}
