package ch.m1m.infra;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ConfigItemUtil {

    private String domain;
    private String ou;

    public ConfigItem applyDefaults(ConfigItem item) {
        if (item.getDomain() == null) {
            item.setDomain(domain);
        }
        if (item.getOu() == null) {
            item.setOu(ou);
        }
        return item;
    }
}
