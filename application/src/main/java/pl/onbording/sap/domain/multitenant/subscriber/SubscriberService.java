package pl.onbording.sap.domain.multitenant.subscriber;

import lombok.RequiredArgsConstructor;
import lombok.val;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SubscriberService {

    private final SubscriberRepository subscriberRepository;

    @Transactional
    public void subscribe(String tenantSchema) {
        subscriberRepository.findOneByTenantSchema(tenantSchema)
                .ifPresentOrElse(
                        subscriber -> subscriber.setState(State.SUBSCRIBED),
                        () -> create(tenantSchema)
                );
    }

    @Transactional
    public void unsubscribe(String tenantSchema) {
        subscriberRepository.findOneByTenantSchema(tenantSchema)
                .ifPresent(subscriber -> subscriber.setState(State.UNSUBSCRIBED));
    }

    public List<Subscriber> findAll() {
        return subscriberRepository.findAll();
    }

    public void create(String tenantSchema) {
        val subscriber = new Subscriber();
        subscriber.setTenantSchema(tenantSchema);
        subscriber.setState(State.SUBSCRIBED);
        subscriberRepository.save(subscriber);
    }
}
