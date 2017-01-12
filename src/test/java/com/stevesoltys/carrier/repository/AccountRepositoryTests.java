package com.stevesoltys.carrier.repository;

import com.stevesoltys.carrier.model.Account;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.Optional;

/**
 * @author Steve Soltys
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = AccountRepositoryTestsContext.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class AccountRepositoryTests {

    /**
     * The mocked account to be populated in the repository.
     */
    private static final Account MOCKED_ACCOUNT = new Account("username", "password");

    /**
     * The account repository.
     */
    @Autowired
    private AccountRepository accountRepository;

    /**
     * Initializes this test.
     */
    @Before
    public void initialize() {
        accountRepository.register(MOCKED_ACCOUNT);
    }

    /**
     * Tests {@link AccountRepository#findByUsername(String)}.
     */
    @Test
    public void testFindByUsername() {
        Optional<Account> accountOptional = accountRepository.findByUsername(MOCKED_ACCOUNT.getUsername());

        assert accountOptional.isPresent() && MOCKED_ACCOUNT.equals(accountOptional.get());
    }

    /**
     * Tests failing of {@link AccountRepository#findByUsername(String)}.
     */
    @Test
    public void testFindByUsernameFailure() {
        Optional<Account> accountOptional = accountRepository.findByUsername(null);

        assert !accountOptional.isPresent();
    }

    /**
     * Tests {@link AccountRepository#register(Account)}.
     */
    @Test
    public void testRegister() {
        accountRepository.register(MOCKED_ACCOUNT);

        assert accountRepository.findByUsername(MOCKED_ACCOUNT.getUsername()).isPresent();
    }

}
