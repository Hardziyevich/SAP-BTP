package pl.onbording.sap.domain.multitenant;

import jakarta.annotation.PostConstruct;
import liquibase.exception.LiquibaseException;
import liquibase.integration.spring.MultiTenantSpringLiquibase;
import liquibase.integration.spring.SpringLiquibase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pl.onbording.sap.domain.multitenant.subscriber.Subscriber;
import pl.onbording.sap.domain.multitenant.subscriber.SubscriberService;
import pl.onbording.sap.infrastructure.Configuration;
import pl.onbording.sap.infrastructure.hibernate.TenatSchemaResolver;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

@Service
@Slf4j
@RequiredArgsConstructor
public class MultiTenantService {

    private final Configuration configuration;
    private final DataSource dataSource;
    private final TenatSchemaResolver tenatSchemaResolver;
    private final SubscriberService subscriberService;

    @PostConstruct
    public void initLiquibaseTenant() throws Exception {
        MultiTenantSpringLiquibase springLiquibase = new MultiTenantSpringLiquibase();
        springLiquibase.setDataSource(dataSource);
        springLiquibase.setChangeLog(configuration.liquibase());
        springLiquibase.setSchemas(
                subscriberService.findActiveSubscribers().stream()
                        .map(Subscriber::getTenantSchema)
                        .toList()
        );
        springLiquibase.afterPropertiesSet();
    }

    public void subscribe(final String tenantId) {
        log.info("TENANT SUB SERVICE {}", tenantId);
        try {
            String schemaName = tenatSchemaResolver.apply(tenantId);

            try (Connection connection = dataSource.getConnection();
                 Statement statement = connection.createStatement()) {
                statement.execute(String.format("CREATE SCHEMA IF NOT EXISTS \"%s\"", schemaName));
            }
            springLiquibase(schemaName).afterPropertiesSet();
            subscriberService.subscribe(schemaName);
        } catch (SQLException | IllegalArgumentException | LiquibaseException e) {
            log.error("Tenant subscription failed for {}.", tenantId, e);
        }
    }


    public void unsubscribe(final String tenantId) {
        log.info("TENANT UNSAB {}", tenantId);
        try {
            final String schemaName = tenatSchemaResolver.apply(tenantId);
            try (Connection connection = dataSource.getConnection();
                 Statement statement = connection.createStatement()) {
                statement.execute(String.format("DROP SCHEMA IF EXISTS \"%s\" CASCADE", schemaName));
            }
            subscriberService.unsubscribe(schemaName);
        } catch (SQLException | IllegalArgumentException e) {
            log.error("Tenant unsubscription failed for {}.", tenantId, e);
        }
    }

    public SpringLiquibase springLiquibase(String schemaName) {
        SpringLiquibase springLiquibase = new SpringLiquibase();
        springLiquibase.setDataSource(dataSource);
        springLiquibase.setChangeLog(configuration.liquibase());
        springLiquibase.setDefaultSchema(schemaName);
        return springLiquibase;
    }

}
