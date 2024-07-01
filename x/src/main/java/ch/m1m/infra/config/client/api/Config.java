package ch.m1m.infra.config.client.api;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

public class Config {
    public static final String CONFIG_URL = "config.url";
    public static final String CONFIG_DOMAIN = "config.domain";
    public static final String CONFIG_APPLICATION = "config.application";

    private static final Logger log = LoggerFactory.getLogger(Config.class);

    private Thread configPollerThread;
    private Map<String, String> extConf = new HashMap<>();

    public Config(Map<String, String> extConf) {
        this(extConf.get(CONFIG_URL), extConf.get(CONFIG_DOMAIN), extConf.get(CONFIG_APPLICATION));
    }

    public Config(String url, String domain, String application) {
        startConfigPollerThread();
    }

    public void register(Object instance) {
        Class clazz = instance.getClass();

        for(Method method : clazz.getDeclaredMethods()) {

            if (method.isAnnotationPresent(ConfigUpdate.class)) {
                ConfigUpdate configUpdateAnnotation = method.getAnnotation(ConfigUpdate.class);
                String key = configUpdateAnnotation.key();

                log.info("found method with annotation @ConfigUpdate {} with key {}", method.getName(), key);

                ConfigUpdateEvent cuEvt = new ConfigUpdateEvent();
                try {
                    method.invoke(instance, cuEvt);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException(e);
                } catch (InvocationTargetException e) {
                    throw new RuntimeException(e);
                }

            } else {
                log.info("found method {}", method.getName());
            }
        }
    }

    private void startConfigPollerThread() {
        ConfigPoller configPoller = new ConfigPoller();
        configPollerThread = new Thread(configPoller);
        configPollerThread.setDaemon(true);
        configPollerThread.start();
    }
}
// Instant start = Instant.now();
// if (start.isBefore(Instant.now().minusSeconds(5))) {
