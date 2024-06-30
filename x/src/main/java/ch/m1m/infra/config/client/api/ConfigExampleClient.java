package ch.m1m.infra.config.client.api;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

public class ConfigExampleClient {

    private final Config config;
    private final AtomicReference<String> key1 = new AtomicReference("not-set");

    public static void main(String... args) {

        String configUrl = "http://my_config_server.m1m.ch/config";
        String domain = "it-ch";
        String application = "batch";

        // this will be an application Singleton
        Config config = new Config(configUrl, domain, application);

        ConfigExampleClient program = new ConfigExampleClient(config);
        program.run();
    }

    public ConfigExampleClient(Config config) {
        this.config = config;
        config.register(this);
    }

    @ConfigUpdate(key="x.y.z")
    public void updateValues(ConfigUpdateEvent configUpdateEvent) {

        key1.set((String)configUpdateEvent.getValue());
    }

    public void run() {
        while(true) {
            System.out.println("current value is: " + key1.get());

            try {
                TimeUnit.SECONDS.sleep(1);
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
            }
        }
    }
}