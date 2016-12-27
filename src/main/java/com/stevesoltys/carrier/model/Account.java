package com.stevesoltys.carrier.model;

/**
 * An account with credentials that can be used to access the REST API.
 *
 * @author Steve Soltys
 */
public class Account {

    /**
     * The username.
     */
    private final String username;

    /**
     * The password.
     */
    private final String password;

    public Account(String username, String password) {
        this.username = username;
        this.password = password;
    }

    /**
     * Gets this account's username.
     *
     * @return The username.
     */
    public String getUsername() {
        return username;
    }

    /**
     * Gets this account's password.
     *
     * @return The password.
     */
    public String getPassword() {
        return password;
    }

}
