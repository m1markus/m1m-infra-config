package ch.m1m.infra.config.example;

import ch.m1m.infra.config.client.api.Config;
import ch.m1m.infra.config.client.api.ConfigUpdate;
import ch.m1m.infra.config.client.api.ConfigUpdateEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

public class ConfigExampleClient {

    private static final Logger log = LoggerFactory.getLogger(ConfigExampleClient.class);

    private final AtomicReference<String> key1 = new AtomicReference<String>("not-set-at-all-v2");

    public static void main(String... args) {

        String configUrl = "http://localhost:8080/config";
        String domain = "example.com";
        String application = "batch";

        Map<String, String> configMap = new HashMap<String, String>();
        configMap.put(Config.CONFIG_URL, configUrl);
        configMap.put(Config.CONFIG_DOMAIN, domain);
        configMap.put(Config.CONFIG_APPLICATION, application);
        configMap.put(Config.CONFIG_POLL_DURATION_SECONDS, "10");

        // this will be an application Singleton
        // Config config = new Config(configUrl, domain, application);
        Config config = new Config(configMap);

        ConfigExampleClient program = new ConfigExampleClient(config);
        program.run();
    }

    public ConfigExampleClient(Config config) {
        config.register(this);
    }

    @ConfigUpdate(key="batch.user.password")
    public void updateValueKey1(ConfigUpdateEvent configUpdateEvent) {
        key1.set(configUpdateEvent.getValue());
    }

    public void run() {
        while(true) {
            //System.out.println("current value is: " + key1.get());
            log.info("current value is: {}", key1.get());

            try {
                TimeUnit.SECONDS.sleep(1);
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
            }
        }
    }
}
