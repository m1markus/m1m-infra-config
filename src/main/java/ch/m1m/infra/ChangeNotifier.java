package ch.m1m.infra;

import io.quarkus.scheduler.Scheduled;
import io.smallrye.mutiny.subscription.UniEmitter;
import jakarta.annotation.PostConstruct;
import jakarta.inject.Singleton;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Singleton
public class ChangeNotifier {

    @PostConstruct
    public void onInit() {
        log.info("ChangeNotifier onInit() ");
    }

    @Scheduled(every="1s")
    public void notifyWaitingClients() {
        log.info("ChangeNotifier notifyWaitingClients() ");

        // check if there is one or more clients to notify about an update

    }

    public void register(UniEmitter<? super Object> em, String registerKey) {
        log.info("ChangeNotifier register() called: registerKey={}", registerKey);

        // put to map() and cleanup old map entries

        em.complete("toto");
    }
}
