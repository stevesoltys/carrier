package com.stevesoltys.carrier.service;

import com.google.common.io.ByteStreams;
import com.stevesoltys.carrier.configuration.SMTPClientConfiguration;
import com.stevesoltys.carrier.exception.CarrierForwardingException;
import com.stevesoltys.carrier.model.MaskedAddress;
import com.stevesoltys.carrier.repository.MaskedAddressRepository;
import net.markenwerk.utils.mail.dkim.Canonicalization;
import net.markenwerk.utils.mail.dkim.DkimMessage;
import net.markenwerk.utils.mail.dkim.DkimSigner;
import net.markenwerk.utils.mail.dkim.SigningAlgorithm;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import tech.blueglacier.email.Attachment;
import tech.blueglacier.email.Email;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.mail.*;
import javax.mail.internet.*;
import javax.mail.util.ByteArrayDataSource;
import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @author Steve Soltys
 */
@Service
@Transactional(propagation = Propagation.REQUIRES_NEW)
public class MailForwardingService {

    private final MaskedAddressRepository maskedAddressRepository;

    private final MailResolverService resolverService;

    private final SMTPClientConfiguration clientConfiguration;

    @Autowired
    public MailForwardingService(MaskedAddressRepository maskedAddressRepository, MailResolverService resolverService,
                                 SMTPClientConfiguration clientConfiguration) {

        this.maskedAddressRepository = maskedAddressRepository;
        this.resolverService = resolverService;
        this.clientConfiguration = clientConfiguration;
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

            Transport.send(createMimeMessage(email, fromAddress, toAddress));

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

    private MimeMessage createMimeMessage(Email email, InternetAddress senderAddress,
                                          InternetAddress destinationAddress) throws Exception {

        if (clientConfiguration.isDkimEnabled()) {
            return createSignedMimeMessage(email, senderAddress, destinationAddress);

        } else {
            return createUnsignedMimeMessage(email, senderAddress, destinationAddress);
        }
    }

    private MimeMessage createUnsignedMimeMessage(Email email, InternetAddress fromAddress, InternetAddress toAddress)
            throws Exception {

        Session session = Session.getDefaultInstance(System.getProperties());
        MimeMessage message = new MimeMessage(session);

        message.setFrom(fromAddress);
        message.addRecipient(Message.RecipientType.TO, toAddress);
        message.setSubject(email.getEmailSubject());

        Multipart multipart = new MimeMultipart();

        InputStream stream = email.getPlainTextEmailBody().getIs();
        String result = new BufferedReader(new InputStreamReader(stream)).lines().collect(Collectors.joining("\n"));

        BodyPart textBodyPart = new MimeBodyPart();
        textBodyPart.setText(result);
        multipart.addBodyPart(textBodyPart);

        for (Attachment attachment : email.getAttachments()) {

            BodyPart attachmentBodyPart = new MimeBodyPart();
            attachmentBodyPart.setFileName(attachment.getAttachmentName());

            byte[] data = ByteStreams.toByteArray(attachment.getIs());
            DataSource source = new ByteArrayDataSource(data, "application/octet-stream");
            attachmentBodyPart.setDataHandler(new DataHandler(source));

            multipart.addBodyPart(attachmentBodyPart);
        }

        message.setContent(multipart);

        return message;
    }

    private MimeMessage createSignedMimeMessage(Email email, InternetAddress senderAddress,
                                                InternetAddress destinationAddress) throws Exception {

        String signingDomain = clientConfiguration.getDomain();
        String selector = clientConfiguration.getDkimSelector();

        File privateKeyFile = new File(clientConfiguration.getPrivateKeyFile());
        DkimSigner dkimSigner = new DkimSigner(signingDomain, selector, privateKeyFile);

        dkimSigner.setIdentity(senderAddress.getAddress());
        dkimSigner.setHeaderCanonicalization(Canonicalization.SIMPLE);
        dkimSigner.setBodyCanonicalization(Canonicalization.RELAXED);

        dkimSigner.setSigningAlgorithm(SigningAlgorithm.SHA256_WITH_RSA);
        dkimSigner.setLengthParam(true);
        dkimSigner.setZParam(false);

        return new DkimMessage(createUnsignedMimeMessage(email, senderAddress, destinationAddress), dkimSigner);
    }
}
