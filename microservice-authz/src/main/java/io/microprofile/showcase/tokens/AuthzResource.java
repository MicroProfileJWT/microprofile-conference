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

import java.security.PrivateKey;
import java.util.HashMap;

import javax.annotation.PostConstruct;
import javax.annotation.security.PermitAll;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.jwt.Claims;

/**
 * An MP-JWT token generation service
 */
@Path("authz")
@PermitAll
@ApplicationScoped
public class AuthzResource {
    @Inject
    private TokenStorage storage;

    /**
     * An example of injecting a custom property type
     */
    @ConfigProperty(name="authz.signingKey")
    @Inject
    private PrivateKey signingKey;

    /**
     * Pass the injected private key into the {@linkplain TokenUtils} class to use as the
     * MP-JWT signer.
     */
    @PostConstruct
    private void init() {
        System.out.printf("AuthzResource.init, signingKey=%s\n", signingKey);
        TokenUtils.setSigningKey(signingKey);
    }

    /**
     * The MP-JWT generation endpoint. A user posts to this endpoint with a {@linkplain Credentials}
     * object, currently username/password, and an MP-JWT token is generated. This service
     *
     * @param credentials - encapsulation of a username/password
     * @return the string format of the MP-JWT token
     * @throws Exception
     */
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response createTokenForCredentials(Credentials credentials) throws Exception {
        System.out.printf("Creating token for username: %s\n", credentials.getUsername());
        String username = credentials.getUsername();
        String simpleName = simpleName(username);
        // Password is name less any email domain + "-secret"
        String password = credentials.getPassword();
        String expectedPassword = simpleName+"-secret";
        if(!password.equals(expectedPassword)) {
            System.err.printf("password(%s) != %s\n", password, expectedPassword);
            return Response.status(403).build();
        }

        // Build up the JWT with the MP-JWT claims + custom claims
        HashMap<String, Object> claims = new HashMap<>();
        claims.put(Claims.upn.name(), username);
        claims.put(Claims.preferred_username.name(), simpleName);
        String jsonResName = String.format("/%s.json", simpleName);
        String stoken = TokenUtils.generateTokenString(jsonResName, claims);
        storage.put(username, stoken);
        System.out.printf("Created token: %s\n", stoken);
        AuthToken token = new AuthToken(credentials.getUsername(), stoken);
        return Response.ok(token).build();
    }

    /**
     * Either returns the last token created for the given user or a NO_CONTENT status
     * @param username - username last passed to {@linkplain #createTokenForCredentials}
     * @return the last JWT token string or no content
     */
    @GET
    @Path("/token/{username}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getLastUserToken(@PathParam("username") String username) {
        String token = storage.get(username);
        Response response;
        if(token == null) {
            response = Response.noContent().build();
        } else {
            response = Response.ok(token).build();
        }
        return response;
    }

    private String simpleName(String username) {
        String simpleName = username;
        int atIndex = username.indexOf('@');
        if(atIndex >= 0) {
            simpleName = username.substring(0, atIndex);
        }
        return simpleName;
    }

}
