package pl.onbording.sap.infrastructure.hibernate;

import org.springframework.stereotype.Component;

import java.util.function.Function;

@Component
public class TenatSchemaResolver implements Function<String, String> {

    @Override
    public String apply(String tenantId) {
        return String.format("tenant_%s", tenantId.replace("-", "_"));
    }
}
