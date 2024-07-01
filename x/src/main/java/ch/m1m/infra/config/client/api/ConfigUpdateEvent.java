package ch.m1m.infra.config.client.api;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConfigUpdateEvent {

    private static final Logger log = LoggerFactory.getLogger(ConfigUpdateEvent.class);

    public String getValue() {
        return "NEW-value-for-all-keys";
    }
}
