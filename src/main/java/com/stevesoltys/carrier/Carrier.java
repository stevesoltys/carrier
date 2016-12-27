package com.stevesoltys.carrier;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.support.SpringBootServletInitializer;

/**
 * @author Steve Soltys
 */
@SpringBootApplication
@EnableAutoConfiguration
public class Carrier extends SpringBootServletInitializer {

    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
        return application.sources(Carrier.class);
    }

    public static void main(String[] args) {
        SpringApplication.run(Carrier.class, args);
    }
}
