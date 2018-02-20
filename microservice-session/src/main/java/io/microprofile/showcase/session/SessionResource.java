/*
 * Copyright(c) 2016-2017 IBM, Red Hat, and others.
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
package io.microprofile.showcase.session;

import javax.annotation.PostConstruct;
import javax.annotation.security.PermitAll;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;

import java.util.Collection;
import java.util.Collections;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import org.eclipse.microprofile.jwt.JsonWebToken;
import org.eclipse.microprofile.metrics.Counter;
import org.eclipse.microprofile.metrics.Histogram;
import org.eclipse.microprofile.metrics.Metadata;
import org.eclipse.microprofile.metrics.MetricRegistry;
import org.eclipse.microprofile.metrics.MetricType;
import org.eclipse.microprofile.metrics.annotation.Metric;
import org.eclipse.microprofile.metrics.annotation.Timed;

/**
 * @author Ken Finnigan
 * @author Heiko Braun
 * @author Scott Stark
 */
@Path("sessions")
@PermitAll
@ApplicationScoped
public class SessionResource {

    @Inject
    @Metric(name = "requestCount", description = "All JAX-RS request made to the SessionResource",
        displayName = "SessionResource#requestCount")
    private Counter requestCount;

    /**
     * The application metrics registry that allows access to any metric to be accessed/created
     */
    @Inject
    private MetricRegistry metrics;

    /**
     * The store of sessions
     */
    @Inject
    private SessionStore sessionStore;

    /**
     * The current MP-JWT for the authenticated user
     */
    @Inject JsonWebToken jwt;

    @PostConstruct
    void init() {
        Collection<Session> sessions = sessionStore.getSessions();
        System.out.printf("SessionResource.init, session count=%d\n", sessions.size());
        // Create a histogram of the session abstract word counts
        Metadata metadata = new Metadata(SessionResource.class.getName()+".abstractWordCount", MetricType.HISTOGRAM);
        metadata.setDescription("Word count histogram for the session abstracts");
        Histogram abstractWordCount = metrics.histogram(metadata);
        for(Session session : sessions) {
            String[] words = session.getAbstract().split("\\s+");
            abstractWordCount.update(words.length);
        }
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Timed
    public Collection<Session> allSessions(@Context SecurityContext securityContext) throws Exception {
        requestCount.inc();
        if (jwt == null) {
            // User was not authenticated
            System.out.printf("allSessions, no token\n");
            return Collections.emptyList();
        }
        String userName = jwt.getName();
        // Use the isUserInRole of container to check for VIP role in the JWT groups claim
        boolean isVIP = securityContext.isUserInRole("VIP");
        System.out.printf("allSessions(%s), isVIP=%s, User token: %s\n", userName, isVIP, jwt);
        // Check if the user has a session_time_preference custom claim in the token
        Optional<String> sessionTimePref = jwt.claim("session_time_preference");
        if(sessionTimePref.isPresent()) {
            // Create a session filter for the time preference...
        }

        // If the user does NOT have a VIP role, filter out the VIP sessions
        Collection<Session> sessions;
        if (!isVIP) {
            sessions = sessionStore.getSessions()
                .stream()
                .filter(session -> !session.isVIPOnly())
                .collect(Collectors.toList());
        } else {
            sessions = sessionStore.getSessions();
        }
        return sessions;
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Session createSession(final Session session) throws Exception {
        return sessionStore.save(session);
    }

    @GET
    @Path("/{sessionId}")
    @Produces(MediaType.APPLICATION_JSON)
    @Timed
    public Response retrieveSession(@PathParam("sessionId") final String sessionId) throws Exception {
        requestCount.inc();
        final Optional<Session> result = sessionStore.find(sessionId);
        System.out.printf("retrieveSession(%s), exists=%s\n", sessionId, result.isPresent());
        if (result.isPresent())
            return Response.ok(result.get()).build();
        else
            return Response.status(404).build();
    }

    @PUT
    @Path("/{sessionId}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response updateSession(@PathParam("sessionId") final String sessionId, final Session session) throws Exception {
        final Optional<Session> updated = sessionStore.update(sessionId, session);
        if (updated.isPresent())
            return Response.ok(updated.get()).build();
        else
            return Response.status(404).build();
    }

    @DELETE
    @Path("/{sessionId}")
    public Response deleteSession(@PathParam("sessionId") final String sessionId) throws Exception {
        final Optional<Session> removed = sessionStore.remove(sessionId);
        if (removed.isPresent())
            return Response.ok().build();
        else
            return Response.status(404).build();

    }

    //TODO Add Search

    @GET
    @Path("/{sessionId}/speakers")
    @Produces(MediaType.APPLICATION_JSON)
    @Timed
    public Response sessionSpeakers(@PathParam("sessionId") final String sessionId) throws Exception {
        requestCount.inc();
        final Optional<Session> optSession = sessionStore.getSessions().stream()
                .filter(s -> Objects.equals(s.getId(), sessionId))
                .findFirst();
        Session session = optSession.orElse(null);
        if (session != null) {
            System.out.printf("sessionSpeakers(%s), count=%d\n", sessionId, session.getSpeakers().size());
            return Response.ok(session.getSpeakers()).build();
        }
        else {
            System.out.printf("sessionSpeakers(%d) does not exist\n", sessionId);
            return Response.status(404).build();
        }

    }

    @PUT
    @Path("/{sessionId}/speakers/{speakerId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response addSessionSpeaker(@PathParam("sessionId") final String sessionId, @PathParam("speakerId") final String speakerId) throws Exception {

        final Optional<Session> result = sessionStore.find(sessionId);

        if (result.isPresent()) {
            final Session session = result.get();
            final Collection<String> speakers = session.getSpeakers();
            speakers.add(speakerId);
            sessionStore.update(sessionId, session);
            return Response.ok(session).build();
        }

        return Response.status(404).build();
    }

    @DELETE
    @Path("/{sessionId}/speakers/{speakerId}")
    public Response removeSessionSpeaker(@PathParam("sessionId") final String sessionId, @PathParam("speakerId") final String speakerId) throws Exception {
        final Optional<Session> result = sessionStore.find(sessionId);

        if (result.isPresent()) {
            final Session session = result.get();
            final Collection<String> speakers = session.getSpeakers();
            speakers.remove(speakerId);
            sessionStore.update(sessionId, session);
            return Response.ok(session).build();
        }

        return Response.status(404).build();
    }
}
