package com.stevesoltys.carrier.service;

import com.stevesoltys.carrier.configuration.SMTPClientConfiguration;
import com.stevesoltys.carrier.exception.CarrierForwardingException;
import com.stevesoltys.carrier.model.MaskedAddress;
import com.stevesoltys.carrier.net.SMTPMessageFactory;
import com.stevesoltys.carrier.repository.MaskedAddressRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import tech.blueglacier.email.Email;

import javax.mail.*;
import javax.mail.internet.*;
import java.util.Optional;

/**
 * @author Steve Soltys
 */
@Service
@Transactional(propagation = Propagation.REQUIRES_NEW)
public class MailForwardingService {

    private final MaskedAddressRepository maskedAddressRepository;

    private final MailResolverService resolverService;

    private final SMTPClientConfiguration clientConfiguration;

    private final SMTPMessageFactory messageFactory;

    @Autowired
    public MailForwardingService(MaskedAddressRepository maskedAddressRepository, MailResolverService resolverService,
                                 SMTPClientConfiguration clientConfiguration, SMTPMessageFactory messageFactory) {

        this.maskedAddressRepository = maskedAddressRepository;
        this.resolverService = resolverService;
        this.clientConfiguration = clientConfiguration;
        this.messageFactory = messageFactory;
    }

    public void forward(Email email) throws CarrierForwardingException {

        try {
            Optional<MaskedAddress> maskedAddressOptional = getMaskedAddress(email);

            if (!maskedAddressOptional.isPresent()) {

                throw new CarrierForwardingException("Could not resolve an active masked address for '"
                        + email.getToEmailHeaderValue() + "'");
            }

            MaskedAddress maskedAddress = maskedAddressOptional.get();
            String destination = maskedAddress.getDestination();

            InternetAddress resultToAddress = new InternetAddress(destination);
            InternetAddress resultFromAddress;

            InternetAddress toAddress = new InternetAddress(email.getToEmailHeaderValue());
            InternetAddress fromAddress = new InternetAddress(email.getFromEmailHeaderValue());

            if (maskedAddress.getReplyAddresses().containsKey(toAddress.getAddress())) {

                String replyForwardAddress = maskedAddress.getReplyAddresses().remove(toAddress.getAddress());
                resultToAddress = new InternetAddress(replyForwardAddress);
                updateHost(replyForwardAddress);

                resultFromAddress = new InternetAddress(maskedAddress.getAddress());

            } else {
                String domain = clientConfiguration.getDomain();
                InternetAddress replyAddress = maskedAddress.generateReplyAddress(domain, fromAddress);

                resultFromAddress = new InternetAddress(replyAddress.getAddress());
                resultFromAddress.setPersonal(fromAddress.getAddress());
            }

            updateHost(resultToAddress.getAddress());

            Transport.send(messageFactory.createMimeMessage(email, resultFromAddress, resultToAddress));

        } catch (Exception ex) {
            ex.printStackTrace();

            throw new CarrierForwardingException(ex.toString());
        }

    }

    private void updateHost(String destination) throws AddressException, CarrierForwardingException {

        Optional<String> destinationHostOptional = resolverService.resolve(destination);

        if (!destinationHostOptional.isPresent()) {
            throw new CarrierForwardingException("Could not resolve an active MX record for '" + destination + "'");
        }

        System.setProperty("mail.smtp.host", destinationHostOptional.get());
    }

    private Optional<MaskedAddress> getMaskedAddress(Email email) throws AddressException {
        InternetAddress address = new InternetAddress(email.getToEmailHeaderValue());

        Optional<MaskedAddress> replyAddressOptional = maskedAddressRepository.findByReplyAddress(address.getAddress());

        if(replyAddressOptional.isPresent()) {
            return replyAddressOptional;
        }

        return maskedAddressRepository.findByAddress(address.getAddress());
    }

}
