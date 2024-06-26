package ch.m1m.infra;

import io.agroal.api.AgroalDataSource;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@ApplicationScoped
public class ConfigItemService {

    private static Logger log = LoggerFactory.getLogger(ConfigItemService.class);

    @Inject
    AgroalDataSource defaultDataSource;

    @Inject
    EntityManager em;

    @Inject
    UUIDv7 uuidGenerator;

    public List<ConfigItem> listAll() throws SQLException {
        List<ConfigItem> resultList = new ArrayList<>();
        Connection conn = defaultDataSource.getConnection();
        Statement stmt = conn.createStatement();

        ResultSet rs = stmt.executeQuery("SELECT id, key FROM ConfigItem ORDER BY key");
        while (rs.next()) {
            String id = rs.getString("id");
            String key = rs.getString("key");
            resultList.add(new ConfigItem(UUID.fromString(id), key));
        }
        return resultList;
    }

    public List<ConfigItem> listAllV2() throws SQLException {
        List<ConfigItem> resultList = em.createQuery("SELECT a FROM ConfigItem a ORDER BY key", ConfigItem.class)
                .getResultList();
        return resultList;
    }

    @Transactional
    public void insertConfigItem(ConfigItem configItem) {

        log.info("try to insert: {}", configItem);

        UUID newId = uuidGenerator.generate();
        configItem.setId(newId);

        em.createNativeQuery("INSERT INTO Config_Item (id, domain, application, key, value, type, description) VALUES (?,?,?,?,?,?,?)")
                .setParameter(1, configItem.getId())
                .setParameter(2, configItem.getDomain())
                .setParameter(3, configItem.getApplication())
                .setParameter(4, configItem.getKey())
                .setParameter(5, configItem.getValue())
                .setParameter(6, configItem.getType())
                .setParameter(7, configItem.getDescription())
                .executeUpdate();
    }
}
