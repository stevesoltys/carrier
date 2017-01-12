package com.stevesoltys.carrier.repository;

import com.stevesoltys.carrier.model.Account;
import org.springframework.stereotype.Repository;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

/**
 * A repository which contains {@link Account}s.
 *
 * @author Steve Soltys
 */
@Repository
public class AccountRepository {

    /**
     * The set of {@link Account}s that this repository contains.
     */
    private final Set<Account> accounts = new HashSet<>();

    /**
     * Attempts to find an {@link Account} in the repository, given the username.
     *
     * @param username The username.
     * @return An optional, possibly containing the account. If it is empty, the account was not found.
     */
    public Optional<Account> findByUsername(String username) {

        for (Account account : accounts) {

            if (account.getUsername().equals(username)) {
                return Optional.of(account);
            }
        }

        return Optional.empty();
    }

    /**
     * Registers an account in the repository.
     *
     * @param account The account.
     */
    public void register(Account account) {
        accounts.add(account);
    }

}
