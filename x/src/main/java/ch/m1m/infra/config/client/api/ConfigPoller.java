package ch.m1m.infra.config.client.api;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

public class ConfigPoller implements Runnable {

    private static final Logger log = LoggerFactory.getLogger(ConfigPoller.class);

    @Override
    public void run() {

        while(true) {
            log.info("ConfigPoller is running");

            try {
                TimeUnit.SECONDS.sleep(1);
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
            }
        }
    }
}
