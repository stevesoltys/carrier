package com.stevesoltys.carrier.net;

import com.stevesoltys.carrier.exception.CarrierForwardingException;
import com.stevesoltys.carrier.model.MaskedAddress;
import com.stevesoltys.carrier.repository.MaskedAddressRepository;
import com.stevesoltys.carrier.service.MailForwardingService;
import org.apache.james.mime4j.MimeException;
import org.apache.james.mime4j.codec.DecodeMonitor;
import org.apache.james.mime4j.message.DefaultBodyDescriptorBuilder;
import org.apache.james.mime4j.parser.ContentHandler;
import org.apache.james.mime4j.parser.MimeStreamParser;
import org.apache.james.mime4j.stream.BodyDescriptorBuilder;
import org.apache.james.mime4j.stream.MimeConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.subethamail.smtp.helper.SimpleMessageListener;
import tech.blueglacier.email.Email;
import tech.blueglacier.parser.CustomContentHandler;

import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;

/**
 * @author Steve Soltys
 */
@Component
public class SMTPMessageHandler implements SimpleMessageListener {

    private final MaskedAddressRepository maskedAddressRepository;

    private final MailForwardingService mailForwardingService;

    @Autowired
    public SMTPMessageHandler(MaskedAddressRepository maskedAddressRepository,
                              MailForwardingService mailForwardingService) {

        this.maskedAddressRepository = maskedAddressRepository;
        this.mailForwardingService = mailForwardingService;
    }

    @Override
    public boolean accept(String from, String recipient) {

        Optional<MaskedAddress> replyOptional = maskedAddressRepository.findByReplyAddress(recipient);

        if(replyOptional.isPresent() && from.equalsIgnoreCase(replyOptional.get().getDestination())) {
            return true;
        }

        return maskedAddressRepository.findByAddress(recipient).isPresent();
    }

    @Override
    public void deliver(String from, String recipient, InputStream data) throws IOException {

        ContentHandler contentHandler = new CustomContentHandler();

        MimeConfig mime4jParserConfig = new MimeConfig();
        BodyDescriptorBuilder bodyDescriptorBuilder = new DefaultBodyDescriptorBuilder();

        MimeStreamParser mime4jParser = new MimeStreamParser(mime4jParserConfig, DecodeMonitor.SILENT,
                bodyDescriptorBuilder);
        mime4jParser.setContentDecoding(true);
        mime4jParser.setContentHandler(contentHandler);

        try {
            mime4jParser.parse(data);

        } catch (MimeException e) {
            throw new IOException("Error parsing MIME message: " + e.getMessage());
        }

        Email email = ((CustomContentHandler) contentHandler).getEmail();

        try {
            mailForwardingService.forward(email);

        } catch (CarrierForwardingException e) {
            e.printStackTrace();
        }
    }
}
