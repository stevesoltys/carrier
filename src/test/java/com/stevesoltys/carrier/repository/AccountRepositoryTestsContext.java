package com.stevesoltys.carrier.repository;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author Steve Soltys
 */
@Configuration
public class AccountRepositoryTestsContext {

    @Bean
    public AccountRepository accountRepository() {
        return new AccountRepository();
    }
}
