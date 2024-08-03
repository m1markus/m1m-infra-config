package ch.m1m.infra;

import io.smallrye.mutiny.Uni;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.resteasy.reactive.RestQuery;
import org.jboss.resteasy.reactive.RestResponse;
import org.jboss.resteasy.reactive.RestResponse.ResponseBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

// see also: @Blocking / @NonBlocking

@Path("/config")
@ApplicationScoped
public class ConfigResource {

    private static final Logger log = LoggerFactory.getLogger(ConfigResource.class);
    private static final AtomicInteger atomicInteger = new AtomicInteger(0);

    private ConfigItemUtil configItemUtil;

    @ConfigProperty(name = "appl.config.default.domain")
    private String domain;
    @ConfigProperty(name = "appl.config.default.ou")
    private String orgUnit;

    @Inject ConfigItemService configItemService;
    @Inject UpdateNotifier updateNotifier;

    @PostConstruct
    public void init() {
        configItemUtil = new ConfigItemUtil(domain, orgUnit);
    }

    // http://localhost:8080/config/longPollForChange?delaySeconds=3&domain=example.com&ou=exampleOrgUnit&&application=batch&lastProcessedRecordUpdatedAt=1970-01-01T13:01:59.123999
    //
    @Path("longPollForChange")
    @GET
    public Uni<RestResponse<Object>> getAsyncLongPolling(
            @RestQuery Integer delaySeconds,
            @RestQuery String domain,
            @RestQuery String ou,
            @RestQuery String application,
            @RestQuery LocalDateTime lastProcessedRecordUpdatedAt) {
        final UUID longPollId = UUID.randomUUID();
        Duration delayRequestForMillis = Duration.ofMillis(30_000);
        if (delaySeconds != null) {
            delayRequestForMillis = Duration.ofMillis(delaySeconds.longValue() * 1000);
        }
        log.info("GET /config/longPollForChange called... delaySeconds={} domain={} ou={} application={} lastProcessedRecordUpdatedAt={}",
                delayRequestForMillis, domain, ou, application, lastProcessedRecordUpdatedAt);
        if (domain == null) {
            domain = configItemUtil.getDomain();
            log.info("domain was null set default value {}", domain);
        }
        if (ou == null) {
            ou = configItemUtil.getOu();
            log.info("ou was null set default value {}", ou);
        }
        if (application == null) {
            application = "exampleApp";
            log.info("application was null set value {}", application);
        }

        final String registerKey = updateNotifier.generateRegisterKey(domain, ou, application);
        log.info("register client with notify key={}", registerKey);

        final String finDomain = domain;
        final String finOu = ou;
        final String finAppl = application;

        return Uni.createFrom().emitter(em -> {
            log.info("do something with the emitter instance... em={} pollId={}", em, longPollId);
            updateNotifier.register(longPollId, em, registerKey);
            try {
                // lost update detector
                //
                long numPendingUpdates = configItemService.countPendingUpdates(finDomain, finOu, finAppl, lastProcessedRecordUpdatedAt);
                log.info("numPendingUpdates={}", numPendingUpdates);
                if (numPendingUpdates > 0) {
                    em.complete("needUpdate");
                }
            } catch(SQLException e) {
                log.error("failed to evaluate numPendingUpdates", e);
            }

        })
                .ifNoItem().after(delayRequestForMillis).recoverWithItem("noContent")
                .onItem().transform(item -> {
                    item = item + "-item-" + atomicInteger.incrementAndGet();
                    return ResponseBuilder.ok(item, MediaType.TEXT_PLAIN_TYPE)
                            //.status(Response.Status.NO_CONTENT)
                            .status(Response.Status.OK)
                            .build();
                })
                // invoke() is synchronous maybe change to the asynchronous call()
                .onItem().invoke(item -> {
                    updateNotifier.unregister(longPollId);
                    log.info("long polling call ended for pollId={}", longPollId);
                });
    }

    @GET
    public List<ConfigItem> get(@RestQuery String domain,
                                @RestQuery String ou,
                                @RestQuery String application) {
        log.info("GET /config called... domain={} ou={} application={}", domain, ou, application);
        if (domain == null) {
            domain = configItemUtil.getDomain();
            log.info("domain was null set default value {}", domain);
        }
        if (ou == null) {
            ou = configItemUtil.getOu();
            log.info("ou was null set default value {}", ou);
        }
        return configItemService.listByDomainAndApplication(domain, ou, application);
    }

    @POST
    public Response insert(ConfigItem configItem) {
        log.info("POST /config insert() called...");
        configItemUtil.applyDefaults(configItem);
        configItemService.insertConfigItem(configItem);
        return Response.ok().entity(configItem).build();
    }

    @PUT
    public Response update(ConfigItem configItem) {
        log.info("PUT /config update() called...");
        configItemUtil.applyDefaults(configItem);
        configItemService.updateConfigItem(configItem);
        updateNotifier.notifyWaitingClients(configItem.getDomain(), configItem.getOu(), configItem.getApplication());
        return Response.ok().status(204).build();
    }

    @DELETE
    public Response delete(ConfigItem configItem) {
        log.info("DELETE /config delete() called...");
        configItemUtil.applyDefaults(configItem);
        configItemService.deleteConfigItem(configItem);
        updateNotifier.notifyWaitingClients(configItem.getDomain(), configItem.getOu(), configItem.getApplication());
        return Response.ok().status(204).build();
    }
}

/* test with

CREATE TABLE public.CONFIG_ITEM (
	id uuid NOT NULL,
	created_at timestamp(6) DEFAULT now() NOT NULL,
	updated_at timestamp(6) DEFAULT now() NOT NULL,
	domain varchar(255) NOT NULL,
	ou varchar(255) NOT NULL,
	application varchar(255) NOT NULL,
	key varchar(255) NOT NULL,
	value varchar(4096) NULL,
	type varchar(128) NULL,
	description varchar(4096) NULL,

	CONSTRAINT configitem_pkey PRIMARY KEY (id)
);


# insert

curl --header "Content-Type: application/json" --request POST \
    --data '{ "id": "0190d66e-17ed-724d-a5f5-17016f7d0a21", "domain": "example.com", "application": "batch", "key": "batch.user.password", "value": "1234", "type": "password", "description": "this is my batch user pw" }' http://localhost:8080/config

# update

curl --header "Content-Type: application/json" --request PUT \
    --data '{ "id": "0190d66e-17ed-724d-a5f5-17016f7d0a21", "domain":"example.com","application":"batch", "value": "8899" }' http://localhost:8080/config

# delete

curl --header "Content-Type: application/json" --request DELETE \
    --data '{ "id": "0190d66e-17ed-724d-a5f5-17016f7d0a21", "domain":"example.com","application":"batch", "value": "8899" }' http://localhost:8080/config

# select

curl -s "http://localhost:8080/config?domain=example.com&ou=exampleOrgUnit&application=batch" | jq

*/
