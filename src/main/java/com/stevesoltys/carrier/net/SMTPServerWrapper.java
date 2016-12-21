package com.stevesoltys.carrier.net;

import com.stevesoltys.carrier.configuration.SMTPServerConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.subethamail.smtp.helper.SimpleMessageListenerAdapter;
import org.subethamail.smtp.server.SMTPServer;

/**
 * A wrapper for {@link SMTPServer} using the {@link SMTPServerConfiguration}.
 *
 * @author Steve Soltys
 */
@Component
public class SMTPServerWrapper {

    /**
     * The SMTP message handler.
     */
    private final SMTPMessageHandler messageHandler;

    /**
     * The server configuration.
     */
    private final SMTPServerConfiguration serverConfiguration;

    @Autowired
    public SMTPServerWrapper(SMTPMessageHandler messageHandler, SMTPServerConfiguration serverConfiguration) {
        this.messageHandler = messageHandler;
        this.serverConfiguration = serverConfiguration;
    }

    /**
     * Starts the SMTP server.
     */
    public void start() {
        SMTPServer smtpServer = new SMTPServer(new SimpleMessageListenerAdapter(messageHandler));

        smtpServer.setRequireTLS(serverConfiguration.isTlsForced());
        smtpServer.setPort(serverConfiguration.getPort());
        smtpServer.start();
    }
}
