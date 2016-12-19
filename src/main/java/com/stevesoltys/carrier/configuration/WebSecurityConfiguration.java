package com.stevesoltys.carrier.configuration;

import com.stevesoltys.carrier.model.Account;
import com.stevesoltys.carrier.repository.AccountRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

/**
 * The web security configuration for this application.
 *
 * @author Steve Soltys
 */
@Configuration
@EnableWebSecurity
public class WebSecurityConfiguration extends WebSecurityConfigurerAdapter {

    /**
     * The account repository.
     */
    private final AccountRepository accountRepository;

    @Autowired
    public WebSecurityConfiguration(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.authorizeRequests()
                .anyRequest().fullyAuthenticated()
                .and().httpBasic();
    }

    @Autowired
    public void configureGlobal(AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(userDetailsService());
    }

    @Bean
    public UserDetailsService userDetailsService() {
        return username -> {

            Optional<Account> accountOptional = accountRepository.findByUsername(username);

            if (!accountOptional.isPresent()) {
                throw new UsernameNotFoundException("Could not find the user '" + username + "'");
            }

            Account account = accountOptional.get();

            return new User(account.getUsername(), account.getPassword(), true, true, true, true,
                    AuthorityUtils.createAuthorityList("USER"));
        };
    }
}
