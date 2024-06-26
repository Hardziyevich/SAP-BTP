package pl.onbording.sap.infrastructure.hibernate;

import com.sap.cloud.sdk.cloudplatform.tenant.TenantAccessor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.checkerframework.checker.initialization.qual.Initialized;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.UnknownKeyFor;
import org.hibernate.cfg.Environment;
import org.hibernate.context.spi.CurrentTenantIdentifierResolver;
import org.springframework.boot.autoconfigure.orm.jpa.HibernatePropertiesCustomizer;
import org.springframework.stereotype.Component;

import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class TenantIdentifierResolver implements CurrentTenantIdentifierResolver, HibernatePropertiesCustomizer {

    @Override
    public @UnknownKeyFor @NonNull @Initialized String resolveCurrentTenantIdentifier() {
        log.info("Current tenant: " + TenantAccessor.getCurrentTenant());
        return TenantAccessor.getCurrentTenant().getTenantId();
    }

    @Override
    public @UnknownKeyFor @NonNull @Initialized boolean validateExistingCurrentSessions() {
        return true;
    }

    @Override
    public void customize(Map<String, Object> hibernateProperties) {
        hibernateProperties.put(Environment.MULTI_TENANT_IDENTIFIER_RESOLVER, this);
    }
}
