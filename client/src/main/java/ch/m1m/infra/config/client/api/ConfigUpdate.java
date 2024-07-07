package ch.m1m.infra.config.client.api;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface ConfigUpdate {
    String key() default "*";
}
