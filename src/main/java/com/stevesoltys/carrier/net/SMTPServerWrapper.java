package com.stevesoltys.carrier.net;

import com.stevesoltys.carrier.configuration.CarrierConfigurationLoader;
import com.stevesoltys.carrier.configuration.SMTPServerConfiguration;
import com.stevesoltys.carrier.exception.CarrierConfigurationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.subethamail.smtp.helper.SimpleMessageListenerAdapter;
import org.subethamail.smtp.server.SMTPServer;

import javax.annotation.PostConstruct;

/**
 * A wrapper for {@link SMTPServer} using the {@link SMTPServerConfiguration}.
 *
 * @author Steve Soltys
 */
@Component
public class SMTPServerWrapper {

    /**
     * The configuration loader;
     */
    private CarrierConfigurationLoader configurationLoader;

    /**
     * The SMTP message handler.
     */
    private final SMTPMessageHandler messageHandler;

    /**
     * The server configuration.
     */
    private final SMTPServerConfiguration serverConfiguration;

    @Autowired
    public SMTPServerWrapper(SMTPMessageHandler messageHandler, SMTPServerConfiguration serverConfiguration,
                             CarrierConfigurationLoader configurationLoader) {

        this.messageHandler = messageHandler;
        this.serverConfiguration = serverConfiguration;
        this.configurationLoader = configurationLoader;
    }

    /**
     * Starts the SMTP server.
     */
    @PostConstruct
    public void start() throws CarrierConfigurationException {
        configurationLoader.run();

        SMTPServer smtpServer = new SMTPServer(new SimpleMessageListenerAdapter(messageHandler));
        smtpServer.setRequireTLS(serverConfiguration.isTlsForced());
        smtpServer.setPort(serverConfiguration.getPort());

        System.setProperty("mail.debug", "true");
        smtpServer.start();
    }
}
