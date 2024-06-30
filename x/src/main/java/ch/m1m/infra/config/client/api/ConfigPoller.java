package ch.m1m.infra.config.client.api;

import java.util.concurrent.TimeUnit;

public class ConfigPoller implements Runnable {

    @Override
    public void run() {

        while(true) {
            System.out.println("ConfigPoller is running");

            try {
                TimeUnit.SECONDS.sleep(1);
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
            }
        }
    }
}
