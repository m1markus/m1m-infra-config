package ch.m1m.infra;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "CONFIG_ITEM")
public class ConfigItem  {

    private UUID id;
    private LocalDateTime created_at;
    private LocalDateTime updated_at;
    private String domain;
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

    public ConfigItem(UUID id, LocalDateTime created_at, LocalDateTime updated_at, String domain, String application, String key, String value, String type, String description) {
        this.id = id;
        this.created_at = created_at;
        this.updated_at = updated_at;
        this.domain = domain;
        this.application = application;
        this.key = key;
        this.value = value;
        this.type = type;
        this.description = description;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public LocalDateTime getCreated_at() {
        return created_at;
    }

    public void setCreated_at(LocalDateTime created_at) {
        this.created_at = created_at;
    }

    public LocalDateTime getUpdated_at() {
        return updated_at;
    }

    public void setUpdated_at(LocalDateTime updated_at) {
        this.updated_at = updated_at;
    }

    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    public String getApplication() {
        return application;
    }

    public void setApplication(String application) {
        this.application = application;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public String toString() {
        return "ConfigItem{" +
                "id=" + id +
                ", created_at=" + created_at +
                ", updated_at=" + updated_at +
                ", domain='" + domain + '\'' +
                ", application='" + application + '\'' +
                ", key='" + key + '\'' +
                ", value='" + value + '\'' +
                ", type='" + type + '\'' +
                ", description='" + description + '\'' +
                '}';
    }
}

/*

CREATE TABLE public.CONFIG_ITEM (
	id uuid NOT NULL,
	created_at timestamp(6) DEFAULT now() NOT NULL,
	updated_at timestamp(6) DEFAULT now() NOT NULL,
	"domain" varchar(255) NOT NULL,
	application varchar(255) NOT NULL,
	"key" varchar(255) NOT NULL,
	value varchar(4096) NULL,
	type varchar(128) NULL,
	description varchar(4096) NULL,

	CONSTRAINT configitem_pkey PRIMARY KEY (id)
);

insert into public.config_item (id, domain, application, key, value, type, description) values('ebf0ea1d-6fd4-4675-b890-7cc235a88859','it-ch','batch','batch.user.pw','1234','password','this is my value')

delete from config_item

*/
