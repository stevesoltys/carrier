package com.stevesoltys.carrier.configuration;

import com.stevesoltys.carrier.exception.CarrierConfigurationException;
import com.stevesoltys.carrier.model.Account;
import com.stevesoltys.carrier.repository.AccountRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * A list of {@link Account}s.
 *
 * @author Steve Soltys
 */
@Component
public class AccountConfiguration extends CarrierConfiguration {

    /**
     * The configuration key for the list of {@link Account}s.
     */
    private static final String ACCOUNT_LIST_KEY = "accounts";

    /**
     * The configuration key for the username of an {@link Account}.
     */
    private static final String USERNAME_KEY = "username";

    /**
     * The configuration key for the password of an {@link Account}.
     */
    private static final String PASSWORD_KEY = "password";

    /**
     * The account repository.
     */
    private final AccountRepository accountRepository;

    @Autowired
    public AccountConfiguration(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    @Override
    @SuppressWarnings("unchecked")
    void initialize(Map<String, Object> configuration) throws CarrierConfigurationException {

        try {
            List<Map<String, String>> accounts = (List<Map<String, String>>) configuration.get(ACCOUNT_LIST_KEY);

            for (Map<String, String> accountConfiguration : accounts) {

                if (!accountConfiguration.containsKey(USERNAME_KEY)) {
                    throw new CarrierConfigurationException(
                            "An account in the configuration does not contain a username.");

                } else if (!accountConfiguration.containsKey(PASSWORD_KEY)) {

                    throw new CarrierConfigurationException(
                            "An account in the configuration does not contain a password.");
                }

                String username = accountConfiguration.get(USERNAME_KEY);
                String password = accountConfiguration.get(PASSWORD_KEY);
                Account account = new Account(username, password);

                accountRepository.getAccounts().add(account);
            }
        } catch (NullPointerException | ClassCastException e) {
            throw new CarrierConfigurationException("Invalid account configuration.");
        }
    }
}
