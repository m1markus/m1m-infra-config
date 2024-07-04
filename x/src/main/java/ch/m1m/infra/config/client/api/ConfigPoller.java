package ch.m1m.infra.config.client.api;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class ConfigPoller implements Runnable {

    private static final Logger log = LoggerFactory.getLogger(ConfigPoller.class);

    // one client instance can be reused
    private final HttpClient client = HttpClient.newBuilder()
            .version(HttpClient.Version.HTTP_1_1)
            .build();

    private Map<String, String> extConf;

    public ConfigPoller(Map<String, String> extConf) {
        this.extConf = extConf;
    }

    @Override
    public void run() {
        int sleepForSeconds = 9;

        String pollUrl = createPollUrl();
        log.info("using poll url={}", pollUrl);

        while (true) {
            log.info("ConfigPoller is running");

            //queryForHello();

            try {
                if (hasUpdatedConfigItems(pollUrl)) {
                    log.info("need config update, query for new values");
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

    private String createPollUrl() {
        String hostPort = extConf.get(Config.CONFIG_URL);
        return String.format("%s/longPollForChange?delaySeconds=%s&domain=%s&application=%s",
                hostPort,
                extConf.get(Config.CONFIG_POLL_DURATION_SECONDS),
                extConf.get(Config.CONFIG_DOMAIN),
                extConf.get(Config.CONFIG_APPLICATION));
    }

    // http://localhost:8080/config/longPollForChange?delaySeconds=5&domain=example.com&application=batch
    //
    private boolean hasUpdatedConfigItems(String pollUrl) throws IOException, InterruptedException {
        boolean hasPendingUpdates = false;
        int restTimeoutSeconds = Integer.valueOf(extConf.get(Config.CONFIG_POLL_DURATION_SECONDS));
        restTimeoutSeconds += 2;

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
