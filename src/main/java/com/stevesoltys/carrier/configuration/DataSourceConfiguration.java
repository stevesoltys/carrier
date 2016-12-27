package com.stevesoltys.carrier.configuration;

import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * The Spring {@link javax.sql.DataSource} configuration.
 */
@Configuration
@EnableTransactionManagement
public class DataSourceConfiguration {
}