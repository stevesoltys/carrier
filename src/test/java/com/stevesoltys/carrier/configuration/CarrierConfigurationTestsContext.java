package com.stevesoltys.carrier.configuration;

import com.stevesoltys.carrier.repository.AccountRepository;
import com.stevesoltys.carrier.repository.CarrierConfigurationRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * The context configuration for the {@link CarrierConfigurationTests}.
 *
 * @author Steve Soltys
 */
@Configuration
public class CarrierConfigurationTestsContext {

    @Bean
    public CarrierConfigurationRepository carrierConfigurationRepository() {
        return new CarrierConfigurationRepository();
    }

    @Bean
    public SMTPServerConfiguration smtpServerConfiguration() {
        return new SMTPServerConfiguration();
    }

    @Bean
    public SMTPClientConfiguration smtpClientConfiguration() {
        return new SMTPClientConfiguration();
    }

    @Bean
    public AccountConfiguration accountConfiguration() {
        return new AccountConfiguration(accountRepository());
    }

    @Bean
    public AccountRepository accountRepository() {
        return new AccountRepository();
    }
}
