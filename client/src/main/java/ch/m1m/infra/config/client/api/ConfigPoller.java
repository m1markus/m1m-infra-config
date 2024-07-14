package ch.m1m.infra.config.client.api;

import ch.m1m.config.model.ConfigItemModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;

public class ConfigPoller implements Runnable {

    private static final Logger log = LoggerFactory.getLogger(ConfigPoller.class);

    private final ConfigItemModelConverter configItemModelConverter = new ConfigItemModelConverter();
    private final Config config;

    private enum PollMode {
        INITIAL_LOAD,
        LONG_POLL
    }

    // one client instance can be reused
    private final HttpClient client = HttpClient.newBuilder()
            .version(HttpClient.Version.HTTP_1_1)
            .build();

    private Map<String, String> extConf;

    public ConfigPoller(Config config) {
        this.config = config;
        this.extConf = config.getExtConfMap();
    }

    @Override
    public void run() {
        int sleepForSeconds = 9;
        PollMode mode = PollMode.INITIAL_LOAD;
        boolean hasUpdatedConfigItems = true;

        String pollUrl = createPollUrl();
        String getConfigUrl = createGetConfigUrl();
        log.info("using poll url={}", pollUrl);

        while (true) {
            log.info("ConfigPoller is running");

            try {
                if (mode == PollMode.LONG_POLL) {
                    hasUpdatedConfigItems = hasUpdatedConfigItems(pollUrl);
                }

                if (hasUpdatedConfigItems) {
                    String respJsonArrAsString = getAllConfigItems(getConfigUrl);
                    processResponseList(configItemModelConverter.toList(respJsonArrAsString));
                    mode = PollMode.LONG_POLL;
                }

                sleepForSeconds = 0;

            } catch (Exception e) {
                log.error("catched {} {}", e.getClass(), e.getMessage());
                sleepForSeconds = 5;
            }

            try {
                log.info("calling sleep({}) SECONDS", sleepForSeconds);
                TimeUnit.SECONDS.sleep(sleepForSeconds);
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
            }
        }
    }

    private void processResponseList(List<ConfigItemModel> list) {
        log.info("processResponseList() list={}", list);
        try {
            list.forEach(this::updateConfigForItem);
        } catch (Exception e) {
            log.error("updateConfigForItem() failed with:", e);
        }
    }

    private void updateConfigForItem(ConfigItemModel item) {
        String key = item.getKey();
        String value = item.getValue();
        ConcurrentMap<String, ConfigMethodRepositoryEntry> methodRepoMap = config.getMethodRepoMap();
        log.info("updateConfigForItem() for domain={} key={} value={}", item.getDomain(), key, value);

        ConfigMethodRepositoryEntry methodRepositoryEntry = methodRepoMap.get(key);
        if (methodRepositoryEntry != null) {
            Object instance = methodRepositoryEntry.getInstance();
            Method method = methodRepositoryEntry.getMethod();

            try {
                if (method != null && instance != null) {
                    ConfigUpdateEvent cuEvt = new ConfigUpdateEvent(value);
                    log.info("pre method.invoke() ...");
                    method.invoke(instance, cuEvt);
                    log.info("post method.invoke()");

                } else {
                    log.info("no update instance or method registered for key={} instance={} method={}",
                            key, instance, method);
                }
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            } catch (InvocationTargetException e) {
                throw new RuntimeException(e);
            }
        } else {
            log.warn("no entry in config repository found for key={}", key);
        }
    }

    private String createPollUrl() {
        String hostPort = extConf.get(Config.CONFIG_URL);
        return String.format("%s/longPollForChange?delaySeconds=%s&domain=%s&application=%s",
                hostPort,
                extConf.get(Config.CONFIG_POLL_DURATION_SECONDS),
                extConf.get(Config.CONFIG_DOMAIN),
                extConf.get(Config.CONFIG_APPLICATION));
    }

    private String createGetConfigUrl() {
        String hostPort = extConf.get(Config.CONFIG_URL);
        return String.format("%s?domain=%s&application=%s",
                hostPort,
                extConf.get(Config.CONFIG_DOMAIN),
                extConf.get(Config.CONFIG_APPLICATION));
    }

    // http://localhost:8080/config/longPollForChange?delaySeconds=5&domain=example.com&application=batch
    //
    private boolean hasUpdatedConfigItems(String pollUrl) throws IOException, InterruptedException {
        boolean hasPendingUpdates = false;
        int restTimeoutSeconds = Integer.valueOf(extConf.get(Config.CONFIG_POLL_DURATION_SECONDS));
        restTimeoutSeconds += 2;

        log.info("initiating GET longPollForChange with {}", pollUrl);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(pollUrl))
                .GET()
                .timeout(Duration.ofSeconds(restTimeoutSeconds))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        String respBody = response.body();
        log.info("response from GET longPollForChange is: {}", respBody);
        if (respBody.startsWith("needUpdate")) {
            hasPendingUpdates = true;
        }

        return hasPendingUpdates;
    }

    private String getAllConfigItems(String getConfigUrl) throws IOException, InterruptedException {

        log.info("initiating GET AllConfigItems with {}", getConfigUrl);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(getConfigUrl))
                .GET()
                .timeout(Duration.ofSeconds(5))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        String respBody = response.body();
        log.info("response from GET longPollForChange is: {}", respBody);
        return respBody;
    }

    private void queryForHello() {
        String urlHello = "http://localhost:8080/hello";

        try {

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(urlHello))
                    .GET()
                    .timeout(Duration.ofSeconds(5))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            log.info("response from url GET is: {}", response.body());

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
