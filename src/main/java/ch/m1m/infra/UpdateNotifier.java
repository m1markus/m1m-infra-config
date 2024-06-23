package ch.m1m.infra;

import io.quarkus.scheduler.Scheduled;
import io.smallrye.mutiny.subscription.UniEmitter;
import jakarta.annotation.PostConstruct;
import jakarta.inject.Singleton;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Singleton
public class UpdateNotifier {

    private Map<String, PollMapEntry> pendingPollRequests = new ConcurrentHashMap<>();

    @PostConstruct
    public void onInit() {
        log.info("ChangeNotifier onInit() ");
    }

    @Scheduled(every = "1s")
    public void notifyWaitingClients() {
        log.info("ChangeNotifier notifyWaitingClients()");

        String domain = "it-ch";
        String application = "batch";
        String updateForApplicationKey = generateRegisterKey(domain, application);

        // check if there is one or more clients to notify about an update

        log.info("notify pendingPollRequests size={}", pendingPollRequests.size());
        pendingPollRequests.forEach((key, pollMapEntry) -> {
            String pollingApplicationKey = pollMapEntry.getClientRegisterKey();
            if (updateForApplicationKey.equals(pollingApplicationKey)) {
                pollMapEntry.getEmitter().complete("needUpdate");
                log.info("notify client for needed update: registerKey={}", updateForApplicationKey);
            }
        });
    }

    public void register(UUID longPollId, UniEmitter<? super Object> em, String registerKey) {
        final String key = longPollId.toString();
        log.info("ChangeNotifier register() called: key={} registerKey={}", key, registerKey);

        PollMapEntry pollMapEntry = new PollMapEntry(key, registerKey, em);
        pendingPollRequests.put(key, pollMapEntry);

        log.info("ChangeNotifier register() new entry added pollMapEntry={}", pollMapEntry);
    }

    public void unregister(UUID longPollId) {
        final String key = longPollId.toString();
        log.info("ChangeNotifier unregister() called: key={}", key);

        pendingPollRequests.remove(key);
    }

    public String generateRegisterKey(String domain, String application) {
        return "%s/%s".formatted(domain.trim().toLowerCase(), application.trim().toLowerCase());
    }
}
