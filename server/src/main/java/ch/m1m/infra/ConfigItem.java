package ch.m1m.infra;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "CONFIG_ITEM")
@AllArgsConstructor
@Data
@ToString
public class ConfigItem  {

    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    private UUID id;
    private LocalDateTime created_at;
    private LocalDateTime updated_at;
    private String domain;
    private String ou;
    private String application;
    private String key;
    private String value;
    private String type;
    private String description;

    public ConfigItem() {}

    public ConfigItem(UUID id, String key) {
        this.id = id;
        this.key = key;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    public UUID getId() {
        return id;
    }

    public void setId(UUID uuid) {
        this.id = uuid;
    }
}
