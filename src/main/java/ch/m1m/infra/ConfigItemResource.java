package ch.m1m.infra;

import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Response;
import org.jboss.resteasy.reactive.RestQuery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@Path("/config")
@ApplicationScoped
public class ConfigItemResource {

    private static Logger log = LoggerFactory.getLogger(ConfigItemResource.class);

    private static AtomicInteger atomicInteger = new AtomicInteger(0);

    @Inject
    ConfigItemService configItemService;

    @Path("x")
    @GET
    public Uni<String> getAsync(@RestQuery Integer delayMillis) {
        Duration delayRequestForMillis = Duration.ofMillis(30_000);
        if (delayMillis != null) {
            delayRequestForMillis = Duration.ofMillis(delayMillis.longValue());
        }
        log.info("GET /config/x called... delaySeconds={}", delayRequestForMillis);
        return Uni.createFrom().emitter(em -> {
            log.info("do something with the emitter instance...");
            em.complete("toto");
        })
                .onItem().transform(item -> item + "-item-" + atomicInteger.incrementAndGet())
                .ifNoItem().after(Duration.ofSeconds(4)).fail()
                .onItem().delayIt().by(delayRequestForMillis);

        //return Uni.createFrom().item("toto");
    }

    @GET
    public List<ConfigItem> get() throws SQLException {
        log.warn("GET /config called...");
        return configItemService.listAllV2();
    }

    @POST
    public Response insert(ConfigItem configItem) {
        log.info("POST /config insert() called...");
        configItemService.insertConfigItem(configItem);
        return Response.ok().entity(configItem).build();
    }
}

/* test with

curl --header "Content-Type: application/json" --request POST \
    --data '{ "id": "ebf0ea1d-6fd4-4675-b890-7cc235a88851", "domain": "it-ch", "application": "batch", "key": "batch.user.password", "value": "1234", "type": "password", "description": "this is my batch user pw" }' http://localhost:8080/config

*/
