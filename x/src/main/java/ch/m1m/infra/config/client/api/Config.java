package ch.m1m.infra.config.client.api;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.time.Instant;

public class Config {
    private Thread configPollerThread;

    private Instant start = Instant.now();

    public Config(String url, String domain, String application) {
        startConfigPollerThread();
    }

    public void register(Object instance) {
        Class clazz = instance.getClass();

        for(Method method : clazz.getDeclaredMethods()) {

            if (method.isAnnotationPresent(ConfigUpdate.class)) {
                ConfigUpdate configUpdateAnnotation = method.getAnnotation(ConfigUpdate.class);
                String key = configUpdateAnnotation.key();

                System.out.println("found method with annotation @ConfigUpdate " + method.getName() + "with key " + key);

                ConfigUpdateEvent cuEvt = new ConfigUpdateEvent();
                try {
                    method.invoke(instance, cuEvt);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException(e);
                } catch (InvocationTargetException e) {
                    throw new RuntimeException(e);
                }

            } else {
                System.out.println("found method " + method.getName());
            }
        }
    }

    public Object getValue(String key) {
        String key1Value = "key1-value";

        if (start.isBefore(Instant.now().minusSeconds(5))) {
            key1Value = "key1-NEW-value";
        }

        return key1Value;
    }

    private void startConfigPollerThread() {
        ConfigPoller configPoller = new ConfigPoller();
        configPollerThread = new Thread(configPoller);
        configPollerThread.setDaemon(true);
        configPollerThread.start();
    }
}
