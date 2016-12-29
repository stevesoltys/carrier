package com.stevesoltys.carrier.net;

import com.google.common.io.ByteStreams;
import com.stevesoltys.carrier.configuration.SMTPClientConfiguration;
import net.markenwerk.utils.mail.dkim.Canonicalization;
import net.markenwerk.utils.mail.dkim.DkimMessage;
import net.markenwerk.utils.mail.dkim.DkimSigner;
import net.markenwerk.utils.mail.dkim.SigningAlgorithm;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import tech.blueglacier.email.Attachment;
import tech.blueglacier.email.Email;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.util.ByteArrayDataSource;
import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * A message factory for {@link MimeMessage}s using the {@link SMTPClientConfiguration}.
 *
 * @author Steve Soltys
 */
@Component
public class SMTPMessageFactory {

    /**
     * The client configuration.
     */
    private final SMTPClientConfiguration clientConfiguration;

    @Autowired
    public SMTPMessageFactory(SMTPClientConfiguration clientConfiguration) {
        this.clientConfiguration = clientConfiguration;
    }

    /**
     * Creates a MIME message used for forwarding the given incoming e-mail. The {@link SMTPClientConfiguration} is
     * utilized to decide whether or not this function will sign the message using DKIM.
     *
     * @param email       The parsed e-mail.
     * @param fromAddress The 'from' address.
     * @param toAddress   The 'to' address.
     * @return The MIME message.
     * @throws Exception If there is an error while creating the MIME message.
     */
    public MimeMessage createMimeMessage(Email email, InternetAddress fromAddress, InternetAddress toAddress)
            throws Exception {

        if (clientConfiguration.isDkimEnabled()) {
            return createSignedMimeMessage(email, fromAddress, toAddress);

        } else {
            return createUnsignedMimeMessage(email, fromAddress, toAddress);
        }
    }

    /**
     * Creates an unsigned MIME message used for forwarding the given incoming e-mail.
     *
     * @param email       The parsed e-mail.
     * @param fromAddress The 'from' address.
     * @param toAddress   The 'to' address.
     * @return The MIME message.
     * @throws Exception If there is an error while creating the unsigned MIME message.
     */
    private MimeMessage createUnsignedMimeMessage(Email email, InternetAddress fromAddress, InternetAddress toAddress)
            throws Exception {

        Session session = Session.getDefaultInstance(System.getProperties());
        MimeMessage message = new MimeMessage(session);

        message.setFrom(fromAddress);
        message.addRecipient(Message.RecipientType.TO, toAddress);
        message.setSubject(email.getEmailSubject());

        Multipart multipart = new MimeMultipart();
        multipart.addBodyPart(createMimeMessageBody(email));

        for(BodyPart attachment : createMimeMessageAttachments(email)) {
            multipart.addBodyPart(attachment);
        }

        message.setContent(multipart);
        return message;
    }

    /**
     * Creates a {@link MimeMessage} body using the given incoming e-mail.
     *
     * @param email The parsed e-mail.
     * @return The body of the message.
     * @throws MessagingException If there is an error while constructing the body of the message.
     */
    private BodyPart createMimeMessageBody(Email email) throws MessagingException {

        BodyPart textBodyPart = new MimeBodyPart();

        if (email.getHTMLEmailBody() != null) {
            InputStream stream = email.getHTMLEmailBody().getIs();

            String result = new BufferedReader(new InputStreamReader(stream)).lines().collect(Collectors.joining("\n"));
            textBodyPart.setContent(result, "text/html; charset=utf-8");

        } else if (email.getPlainTextEmailBody() != null) {
            InputStream stream = email.getPlainTextEmailBody().getIs();

            String result = new BufferedReader(new InputStreamReader(stream)).lines().collect(Collectors.joining("\n"));
            textBodyPart.setText(result);
        }

        return textBodyPart;
    }

    /**
     * Creates a list of {@link MimeMessage} attachments using the given incoming e-mail.
     *
     * @param email The parsed e-mail.
     * @return A list containing the attachments, if any.
     * @throws Exception If there is an error while constructing the list of attachments.
     */
    private List<BodyPart> createMimeMessageAttachments(Email email) throws Exception {

        List<BodyPart> attachments = new LinkedList<>();

        for (Attachment attachment : email.getAttachments()) {
            BodyPart attachmentBodyPart = new MimeBodyPart();
            attachmentBodyPart.setFileName(attachment.getAttachmentName());

            byte[] data = ByteStreams.toByteArray(attachment.getIs());
            DataSource source = new ByteArrayDataSource(data, "application/octet-stream");
            attachmentBodyPart.setDataHandler(new DataHandler(source));

            attachments.add(attachmentBodyPart);
        }

        return attachments;
    }

    /**
     * Creates a signed MIME message used for forwarding the given incoming e-mail.
     *
     * @param email       The parsed e-mail.
     * @param fromAddress The 'from' address.
     * @param toAddress   The 'to' address.
     * @return The MIME message.
     * @throws Exception If there is an error while creating the unsigned MIME message.
     */
    private MimeMessage createSignedMimeMessage(Email email, InternetAddress fromAddress, InternetAddress toAddress)
            throws Exception {

        String signingDomain = clientConfiguration.getDomain();
        String selector = clientConfiguration.getDkimSelector();

        File privateKeyFile = new File(clientConfiguration.getPrivateKeyFile());
        DkimSigner dkimSigner = new DkimSigner(signingDomain, selector, privateKeyFile);

        dkimSigner.setIdentity(fromAddress.getAddress());
        dkimSigner.setHeaderCanonicalization(Canonicalization.SIMPLE);
        dkimSigner.setBodyCanonicalization(Canonicalization.RELAXED);

        dkimSigner.setSigningAlgorithm(SigningAlgorithm.SHA256_WITH_RSA);
        dkimSigner.setLengthParam(true);
        dkimSigner.setZParam(false);

        return new DkimMessage(createUnsignedMimeMessage(email, fromAddress, toAddress), dkimSigner);
    }
}
