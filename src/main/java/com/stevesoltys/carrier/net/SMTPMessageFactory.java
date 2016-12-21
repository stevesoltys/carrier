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
import java.io.*;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.stream.Collectors;

/**
 * @author Steve Soltys
 */
@Component
public class SMTPMessageFactory {

    private final SMTPClientConfiguration clientConfiguration;

    @Autowired
    public SMTPMessageFactory(SMTPClientConfiguration clientConfiguration) {
        this.clientConfiguration = clientConfiguration;
    }


    public MimeMessage createMimeMessage(Email email, InternetAddress fromAddress, InternetAddress toAddress)
            throws Exception {

        if (clientConfiguration.isDkimEnabled()) {
            return createSignedMimeMessage(email, fromAddress, toAddress);

        } else {
            return createUnsignedMimeMessage(email, fromAddress, toAddress);
        }
    }

    private MimeMessage createUnsignedMimeMessage(Email email, InternetAddress fromAddress, InternetAddress toAddress)
            throws MessagingException, IOException {

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

    private MimeMessage createSignedMimeMessage(Email email, InternetAddress fromAddress, InternetAddress toAddress)
            throws NoSuchAlgorithmException, IOException, InvalidKeySpecException, MessagingException {

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
