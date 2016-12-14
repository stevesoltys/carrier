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
 * @author Steve Soltys
 */
@Entity
public class MaskedAddress implements Serializable {

    @Column
    @EmbeddedId
    private final String address;

    @Column
    private final String destination;

    @ElementCollection
    @OrderColumn
    private final Map<String, String> replyAddresses;

    private final SecureRandom secureRandom;

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

    public InternetAddress generateReplyAddress(String domain, InternetAddress fromAddress) throws AddressException {
        String generatedAddress = new BigInteger(130, secureRandom).toString(32) + "@" + domain;
        replyAddresses.put(generatedAddress, fromAddress.getAddress());

        return new InternetAddress(generatedAddress);
    }

    public String getAddress() {
        return address;
    }

    public Map<String, String> getReplyAddresses() {
        return replyAddresses;
    }

    public String getDestination() {
        return destination;
    }
}
