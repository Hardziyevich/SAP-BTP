package pl.onbording.sap.domain.multitenant.subscriber;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SubscriberRepository extends JpaRepository<Subscriber, Long> {

    Optional<Subscriber> findOneByTenantSchema(String tenantSchema);
}
