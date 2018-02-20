package io.microprofile.showcase.session;

import java.util.Date;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.health.Health;
import org.eclipse.microprofile.health.HealthCheck;
import org.eclipse.microprofile.health.HealthCheckResponse;

/**
 * Illustration of new MP-1.2 health check for the session application with configuration
 * used to externalize the session count data label
 */
@Health
@ApplicationScoped
public class SessionCheck implements HealthCheck {
    @Inject
    private SessionStore sessionStore;
    @Inject
    @ConfigProperty(name = "sessionCountName", defaultValue = "sessionCount")
    private String sessionCountName;
    @ConfigProperty(name = "JAR_SHA256")
    @Inject
    private String jarSha256;

    @Override
    public HealthCheckResponse call() {
        return HealthCheckResponse.named("sessions-check")
            .withData(sessionCountName, sessionStore.getSessions().size())
            .withData("lastCheckDate", new Date().toString())
            .withData("jarSHA256", jarSha256)
            .up()
            .build();
    }
}
