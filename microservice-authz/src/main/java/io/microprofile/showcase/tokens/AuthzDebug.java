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

import javax.annotation.security.PermitAll;
import javax.enterprise.context.ApplicationScoped;
import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

/**
 * An unsecured endpoint for validating access
 */
@Path("/")
@PermitAll
@ApplicationScoped
public class AuthzDebug {

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public JsonObject debugCheck() {
        JsonObjectBuilder builder = Json.createObjectBuilder();
        JsonObject reply = builder.add("debugInfo", "Microservice Authz").build();
        return reply;
    }
}
