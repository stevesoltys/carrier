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

import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import java.util.Optional;

/**
 * The e-mail forwarding service.
 *
 * @author Steve Soltys
 */
@Service
@Transactional(propagation = Propagation.REQUIRES_NEW)
public class MailForwardingService {

    /**
     * The masked address repository.
     */
    private final MaskedAddressRepository maskedAddressRepository;

    /**
     * The mail resolver service.
     */
    private final MailResolverService resolverService;

    /**
     * The client configuration.
     */
    private final SMTPClientConfiguration clientConfiguration;

    /**
     * The message factory.
     */
    private final SMTPMessageFactory messageFactory;

    @Autowired
    public MailForwardingService(MaskedAddressRepository maskedAddressRepository, MailResolverService resolverService,
                                 SMTPClientConfiguration clientConfiguration, SMTPMessageFactory messageFactory) {

        this.maskedAddressRepository = maskedAddressRepository;
        this.resolverService = resolverService;
        this.clientConfiguration = clientConfiguration;
        this.messageFactory = messageFactory;
    }

    /**
     * Forwards the given e-mail to the correct destination.
     *
     * @param to    The e-mail address to find a {@link MaskedAddress} for.
     * @param email The e-mail.
     * @throws CarrierForwardingException If there is an error while forwarding the e-mail.
     */
    public void forward(String to, Email email) throws CarrierForwardingException {

        try {
            Optional<MaskedAddress> maskedAddressOptional = getMaskedAddress(to);

            if (!maskedAddressOptional.isPresent()) {

                throw new CarrierForwardingException("Could not resolve an active masked address for '"
                        + email.getToEmailHeaderValue() + "'");
            }

            MaskedAddress maskedAddress = maskedAddressOptional.get();

            if (maskedAddress.getReplyAddresses().containsKey(to)) {
                forwardReply(email, maskedAddress, to);

            } else {
                forwardIncomingMail(email, maskedAddress);
            }

        } catch (Exception ex) {
            ex.printStackTrace();

            throw new CarrierForwardingException(ex.toString());
        }

    }

    /**
     * Finds a masked address for the given e-mail.
     *
     * @param address The recipient e-mail address.
     * @return An optional, possibly containing a masked address.
     * @throws AddressException If there is an error while parsing the 'to' header in the e-mail.
     */
    private Optional<MaskedAddress> getMaskedAddress(String address) throws AddressException {

        Optional<MaskedAddress> replyAddressOptional = maskedAddressRepository.findByReplyAddress(address);

        if (replyAddressOptional.isPresent()) {
            return replyAddressOptional;
        }

        return maskedAddressRepository.findByAddress(address);
    }

    /**
     * Forwards a reply to an e-mail.
     *
     * @param email         The parsed e-mail.
     * @param maskedAddress The masked address that was used for the previous e-mail.
     * @param replyAddress  The address that the reply was sent to for forwarding.
     * @throws Exception If there is an error while forwarding the reply.
     */
    private void forwardReply(Email email, MaskedAddress maskedAddress, String replyAddress) throws Exception {

        String replyForwardAddress = maskedAddress.getReplyAddresses().remove(replyAddress);
        updateHost(replyForwardAddress);

        InternetAddress fromAddress = new InternetAddress(maskedAddress.getAddress());
        InternetAddress toAddress = new InternetAddress(replyForwardAddress);

        Transport.send(messageFactory.createMimeMessage(email, fromAddress, toAddress));
    }

    /**
     * Forwards incoming mail to the corresponding address.
     *
     * @param email         The parsed e-mail.
     * @param maskedAddress The masked address that was triggered for this e-mail.
     * @throws Exception If there is an error while forwarding the e-mail.
     */
    private void forwardIncomingMail(Email email, MaskedAddress maskedAddress) throws Exception {

        InternetAddress originalFromAddress = new InternetAddress(email.getFromEmailHeaderValue());

        String domain = clientConfiguration.getDomain();
        InternetAddress replyAddress = maskedAddress.generateReplyAddress(domain, originalFromAddress);

        InternetAddress toAddress = new InternetAddress(maskedAddress.getDestination());
        InternetAddress fromAddress = new InternetAddress(replyAddress.getAddress());
        fromAddress.setPersonal(originalFromAddress.getAddress());

        updateHost(toAddress.getAddress());
        Transport.send(messageFactory.createMimeMessage(email, fromAddress, toAddress));
    }

    /**
     * Updates the current SMTP host server that we are connecting to.
     *
     * @param destination The destination e-mail address.
     * @throws CarrierForwardingException If there is an error setting the host.
     */
    private void updateHost(String destination) throws CarrierForwardingException {

        Optional<String> destinationHostOptional = resolverService.resolve(destination);

        if (!destinationHostOptional.isPresent()) {
            throw new CarrierForwardingException("Could not resolve an active MX record for '" + destination + "'");
        }

        System.setProperty("mail.smtp.host", destinationHostOptional.get());
    }

}
