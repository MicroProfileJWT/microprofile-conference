/*
 * Copyright(c) 2017 IBM, Red Hat, and others.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *      http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.microprofile.showcase.tokens;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.health.Health;
import org.eclipse.microprofile.health.HealthCheck;
import org.eclipse.microprofile.health.HealthCheckResponse;
import org.eclipse.microprofile.health.HealthCheckResponseBuilder;

/**
 * The health check for the authz service
 */
@Health
@ApplicationScoped
public class AuthzCheck implements HealthCheck {
    @ConfigProperty(defaultValue = "true")
    @Inject
    private boolean includeTokenCounts;
    @Inject
    private TokenStorage storage;

    @PostConstruct
    private void init() {
        System.out.printf("AuthzCheck.init, includeTokenCounts=%s, storage=%s\n", includeTokenCounts, storage);
    }

    @Override
    public HealthCheckResponse call() {
        HealthCheckResponseBuilder builder = HealthCheckResponse.named("authz-check")
            .up();
        if(includeTokenCounts) {
            builder = builder.withData("tokenCounts", storage.size());
        }
        return builder.build();
    }
}
