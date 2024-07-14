package ch.m1m.infra.config.client.api;

import lombok.*;

import java.lang.reflect.Method;

@Data
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class ConfigMethodRepositoryEntry {
    private Object instance;
    private Method method;
}
