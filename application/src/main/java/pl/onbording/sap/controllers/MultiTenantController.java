package pl.onbording.sap.controllers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pl.onbording.sap.controllers.models.Subscription;
import pl.onbording.sap.domain.multitenant.MultiTenantService;
import pl.onbording.sap.infrastructure.Configuration;

@Slf4j
@RestController
@RequestMapping("/callback/v1.0/tenants")
@RequiredArgsConstructor
public class MultiTenantController {

    private final MultiTenantService multiTenantService;
    private final Configuration configuration;

    @PutMapping("/{tenantId}")
    public ResponseEntity<String> subscribeTenant(@RequestBody Subscription requestBody,
                                                  @PathVariable(value = "tenantId") String tenantId) {
        log.info("TENANT SUBSCRIBE CONTROLLER {}", tenantId);
        log.info("TENANT SUBSCRIPTION: " + requestBody.toString());
        multiTenantService.subscribe(tenantId);
        return ResponseEntity.ok("https://" + requestBody.subscribedSubdomain() + configuration.approuter());
    }

    @DeleteMapping("/{tenantId}")
    public ResponseEntity<Void> unsubscribeTenant(@PathVariable("tenantId") String tenantId) {
        log.info("TENANT UNSUBSCRIBE CONTROLLER {}", tenantId);
        multiTenantService.unsubscribe(tenantId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
