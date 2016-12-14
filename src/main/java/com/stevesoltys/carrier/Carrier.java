package com.stevesoltys.carrier;

import com.stevesoltys.carrier.configuration.CarrierConfigurationLoader;
import com.stevesoltys.carrier.net.SMTPServerWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @author Steve Soltys
 */
@SpringBootApplication
@EnableAutoConfiguration
public class Carrier implements CommandLineRunner {

    private final CarrierConfigurationLoader configurationLoader;

    private final SMTPServerWrapper smtpServer;

    @Autowired
    public Carrier(CarrierConfigurationLoader configurationLoader, SMTPServerWrapper smtpServer) {
        this.configurationLoader = configurationLoader;
        this.smtpServer = smtpServer;
    }

    @Override
    public void run(String... args) throws Exception {
        System.setProperty("mail.debug", "true");

        configurationLoader.run();
        smtpServer.start();
    }

    public static void main(String[] args) {
        SpringApplication.run(Carrier.class, args);
    }
}
