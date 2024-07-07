package ch.m1m.config.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.ToString;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@ToString
@AllArgsConstructor
public class ConfigItemModel {

    private UUID id;
    private LocalDateTime created_at;
    private LocalDateTime updated_at;
    private String domain;
    private String application;
    private String key;
    private String value;
    private String type;
    private String description;
}
