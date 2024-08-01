package ch.m1m.infra.config.client.api;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class Config {
    public static final String CONFIG_URL = "config.url";
    public static final String CONFIG_DOMAIN = "config.domain";
    public static final String CONFIG_OU = "config.ou";
    public static final String CONFIG_APPLICATION = "config.application";
    public static final String CONFIG_POLL_DURATION_SECONDS = "config.poll.duration.seconds";

    private static final Logger log = LoggerFactory.getLogger(Config.class);

    private Thread configPollerThread;
    private final Map<String, String> extConf;

    private final ConcurrentMap<String, ConfigMethodRepositoryEntry> methodRepoMap = new ConcurrentHashMap<>();

    public Config(Map<String, String> extConf) {
        this.extConf = extConf;
        startConfigPollerThread();
    }

    public void register(Object instance) {
        Class clazz = instance.getClass();

        for(Method method : clazz.getDeclaredMethods()) {

            if (method.isAnnotationPresent(ConfigUpdate.class)) {
                ConfigUpdate configUpdateAnnotation = method.getAnnotation(ConfigUpdate.class);
                String key = configUpdateAnnotation.key();

                log.info("found method with annotation @ConfigUpdate {} for key {}", method.getName(), key);

                // methodRepoMap
                // map: key = a.b.c
                // object: object, method
                // ConfigMethodRepositoryEntry
                //
                ConfigMethodRepositoryEntry repoEntry = ConfigMethodRepositoryEntry.builder()
                        .instance(instance)
                        .method(method)
                        .build();

                methodRepoMap.put(key, repoEntry);

            } else {
                log.debug("found method {}", method.getName());
            }
        }
    }

    Map<String, String> getExtConfMap() {
        return this.extConf;
    }

    ConcurrentMap<String, ConfigMethodRepositoryEntry> getMethodRepoMap() {
        return this.methodRepoMap;
    }

    private void startConfigPollerThread() {
        ConfigPoller configPoller = new ConfigPoller(this);
        configPollerThread = new Thread(configPoller);
        configPollerThread.setDaemon(true);
        configPollerThread.start();
    }
}

// Instant start = Instant.now();
// if (start.isBefore(Instant.now().minusSeconds(5))) {
