package com.stevesoltys.carrier.net;

import com.stevesoltys.carrier.configuration.SMTPServerConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.subethamail.smtp.helper.SimpleMessageListenerAdapter;
import org.subethamail.smtp.server.SMTPServer;

/**
 * @author Steve Soltys
 */
@Component
public class SMTPServerWrapper {

    private final SMTPMessageHandler messageHandler;

    private final SMTPServerConfiguration serverConfiguration;

    @Autowired
    public SMTPServerWrapper(SMTPMessageHandler messageHandler, SMTPServerConfiguration serverConfiguration) {
        this.messageHandler = messageHandler;
        this.serverConfiguration = serverConfiguration;
    }

    public void start() {
        SMTPServer smtpServer = new SMTPServer(new SimpleMessageListenerAdapter(messageHandler));

        smtpServer.setRequireTLS(serverConfiguration.isTlsForced());
        smtpServer.setPort(serverConfiguration.getPort());
        smtpServer.start();
    }
}
