package ch.m1m.infra.config.client.api;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

public class ConfigExampleClient {

    private static final Logger log = LoggerFactory.getLogger(ConfigExampleClient.class);

    private final Config config;
    private final AtomicReference<String> key1 = new AtomicReference<>("not-set");

    public static void main(String... args) {

        String configUrl = "http://config_server.m1m.ch/config";
        String domain = "it-ch";
        String application = "batch";

        Map<String, String> configMap = new HashMap<>();
        configMap.put(Config.CONFIG_URL, configUrl);
        configMap.put(Config.CONFIG_DOMAIN, domain);
        configMap.put(Config.CONFIG_APPLICATION, application);

        // this will be an application Singleton
        // Config config = new Config(configUrl, domain, application);
        Config config = new Config(configMap);

        ConfigExampleClient program = new ConfigExampleClient(config);
        program.run();
    }

    public ConfigExampleClient(Config config) {
        this.config = config;
        config.register(this);
    }

    @ConfigUpdate(key="x.y.z")
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
