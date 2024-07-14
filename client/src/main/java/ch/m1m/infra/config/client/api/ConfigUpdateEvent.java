package ch.m1m.infra.config.client.api;

public class ConfigUpdateEvent {

    //private static final Logger log = LoggerFactory.getLogger(ConfigUpdateEvent.class);

    private final String value;

    public ConfigUpdateEvent(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
