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

            InternetAddress toAddress = new InternetAddress(destination);
            InternetAddress fromAddress = new InternetAddress(email.getToEmailHeaderValue());

            if (maskedAddress.getReplyAddresses().containsKey(fromAddress.getPersonal())) {

                String replyAddress = maskedAddress.getReplyAddresses().remove(fromAddress.getPersonal());
                toAddress = new InternetAddress(replyAddress);
                updateHost(replyAddress);

                fromAddress = new InternetAddress(maskedAddress.getAddress());

            } else {
                String domain = clientConfiguration.getDomain();
                InternetAddress originalFromAddress = new InternetAddress(email.getFromEmailHeaderValue());

                InternetAddress replyAddress = maskedAddress.generateReplyAddress(domain, originalFromAddress);
                fromAddress.setPersonal(replyAddress.getAddress());
            }

            updateHost(toAddress.getAddress());

            Transport.send(messageFactory.createMimeMessage(email, fromAddress, toAddress));

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
        InternetAddress localAddress = new InternetAddress(email.getToEmailHeaderValue());

        return maskedAddressRepository.findByAddress(localAddress.getAddress());
    }

}
