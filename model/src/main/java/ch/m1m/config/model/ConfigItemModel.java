package ch.m1m.config.model;

import lombok.*;

import java.time.LocalDateTime;

@Data
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class ConfigItemModel {

    private String id;
    private LocalDateTime created_at;
    private LocalDateTime updated_at;
    private String domain;
    private String ou;
    private String application;
    private String key;
    private String value;
    private String type;
    private String description;
}
