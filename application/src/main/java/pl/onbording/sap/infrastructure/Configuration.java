package pl.onbording.sap.infrastructure;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "configuration")
public record Configuration(String approuter, String liquibase) {

}
