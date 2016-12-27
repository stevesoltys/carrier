package com.stevesoltys.carrier.model;

import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.persistence.*;
import java.io.Serializable;
import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.HashMap;
import java.util.Map;

/**
 * A masked e-mail address.
 *
 * @author Steve Soltys
 */
@Entity
public class MaskedAddress implements Serializable {

    /**
     * The e-mail address used for masking.
     */
    @Column
    @EmbeddedId
    private final String address;

    /**
     * The hidden address that we are masking.
     */
    @Column
    private final String destination;

    /**
     * A map containing the 'reply' addresses. These are the addresses used for replying to the original sender. They
     * are randomly generated tokens combined with your domain name.
     */
    @Column
    @ElementCollection
    private final Map<String, String> replyAddresses;

    /**
     * A secure random instance, used for safely generating random tokens.
     */
    private final SecureRandom secureRandom;

    /**
     * Creates a masked address.
     *
     * @param address The incoming e-mail address.
     * @param destination The outgoing e-mail address.
     */
    public MaskedAddress(String address, String destination) {
        this.address = address;
        this.destination = destination;

        this.replyAddresses = new HashMap<>();
        this.secureRandom = new SecureRandom();
    }

    protected MaskedAddress() {
        this.address = null;
        this.destination = null;

        this.replyAddresses = new HashMap<>();
        this.secureRandom = new SecureRandom();
    }

    /**
     * Generates a reply address, given the domain and original 'from' address. These are the addresses used for
     * replying to the original sender. They are randomly generated tokens combined with your domain name.
     *
     * @param domain The domain name.
     * @param fromAddress The 'from' address.
     * @return A randomly generated e-mail address.
     * @throws AddressException If there is an issue generating the address.
     */
    public InternetAddress generateReplyAddress(String domain, InternetAddress fromAddress) throws AddressException {
        String generatedAddress = new BigInteger(130, secureRandom).toString(32) + "@" + domain;
        replyAddresses.put(generatedAddress, fromAddress.getAddress());

        return new InternetAddress(generatedAddress);
    }

    /**
     * Gets the e-mail address used for masking.
     *
     * @return The e-mail address used for masking.
     */
    public String getAddress() {
        return address;
    }

    /**
     * Gets the reply address map. These are the addresses used for replying to the original sender. They are randomly
     * generated tokens combined with your domain name.
     *
     * @return The reply address map.
     */
    public Map<String, String> getReplyAddresses() {
        return replyAddresses;
    }

    /**
     * Gets the address that e-mails will be forwarded to.
     *
     * @return The destination address.
     */
    public String getDestination() {
        return destination;
    }
}
