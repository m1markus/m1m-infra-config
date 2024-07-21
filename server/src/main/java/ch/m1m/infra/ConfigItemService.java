package ch.m1m.infra;

import io.agroal.api.AgroalDataSource;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
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

    public List<ConfigItem> listByDomainAndApplication(String domain, String application) throws SQLException {
        TypedQuery<ConfigItem> query = em.createQuery("SELECT a FROM ConfigItem a WHERE a.domain = :domain AND a.application = :application ORDER BY key", ConfigItem.class);
        query.setParameter("domain", domain);
        query.setParameter("application", application);
        return query.getResultList();
    }

    @Transactional
    public void insertConfigItem(ConfigItem configItem) {

        log.info("try to insert: {}", configItem);

        if (configItem.getId() == null) {
            UUID newId = uuidGenerator.generate();
            configItem.setId(newId);
        }

        em.createNativeQuery("INSERT INTO Config_Item (id, domain, ou, application, key, value, type, description) VALUES (?,?,?,?,?,?,?,?)")
                .setParameter(1, configItem.getId())
                .setParameter(2, configItem.getDomain())
                .setParameter(3, configItem.getOu())
                .setParameter(4, configItem.getApplication())
                .setParameter(5, configItem.getKey())
                .setParameter(6, configItem.getValue())
                .setParameter(7, configItem.getType())
                .setParameter(8, configItem.getDescription())
                .executeUpdate();
    }

    @Transactional
    public void updateConfigItem(ConfigItem configItem) {

        log.info("try to update: {}", configItem);

        em.createNativeQuery("UPDATE Config_Item SET updated_at=?, value=? WHERE id=?")
                // more operational
                .setParameter(1, LocalDateTime.now())
                // application fields
                .setParameter(2, configItem.getValue())
                .setParameter(3, configItem.getId())
                .executeUpdate();
    }

    @Transactional
    public void deleteConfigItem(ConfigItem configItem) {
        log.info("try to delete: {}", configItem);
        em.createNativeQuery("DELETE FROM Config_Item WHERE id=:id")
                .setParameter("id", configItem.getId())
                .executeUpdate();
    }
}
