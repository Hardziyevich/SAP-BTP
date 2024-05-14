package pl.onbording.sap.controllers.models;

public record Subscription(String subscriptionAppId, String subscriptionAppName, String subscribedTenantId,
                           String subscribedSubdomain, String globalAccountGUID, String subscribedLicenseType) {
}
