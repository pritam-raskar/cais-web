package com.dair.cais.alert.migration;

import com.dair.cais.alert.AlertEntity;
import com.dair.cais.alert.rdbms.RdbmsAlertEntity;
import com.dair.cais.alert.rdbms.RdbmsAlertRepository;
import com.dair.cais.organization.OrganizationUnitEntity;
import com.dair.cais.organization.OrganizationUnitRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.mongodb.core.query.Criteria;
import com.dair.cais.common.config.CaisAlertConstants;

import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class AlertDataMigrationService {

    private final MongoTemplate mongoTemplate;
    private final RdbmsAlertRepository rdbmsAlertRepository;
    private final OrganizationUnitRepository organizationUnitRepository;

    @Transactional
    public void migrateOrgKeyData() {
        log.info("Starting migration of orgKey data for existing alerts");
        AtomicInteger mongoUpdated = new AtomicInteger(0);
        AtomicInteger rdbmsUpdated = new AtomicInteger(0);
        AtomicInteger skipped = new AtomicInteger(0);

        try {
            // First, fetch all organization units and create a map for quick lookup
            Map<String, String> orgIdToKeyMap = organizationUnitRepository.findAll().stream()
                    .collect(Collectors.toMap(
                            org -> String.valueOf(org.getOrgId()),
                            OrganizationUnitEntity::getOrgKey
                    ));

            log.info("Loaded {} organization mappings", orgIdToKeyMap.size());

            // Update MongoDB documents
            Query query = new Query(Criteria.where("orgUnitKey").exists(false));
            List<AlertEntity> mongoAlerts = mongoTemplate.find(query, AlertEntity.class, CaisAlertConstants.ALERTS);

            log.info("Found {} MongoDB alerts requiring update", mongoAlerts.size());

            for (AlertEntity alert : mongoAlerts) {
                try {
                    String orgUnitId = alert.getOrgUnitId();
                    if (orgUnitId != null && orgIdToKeyMap.containsKey(orgUnitId)) {
                        String orgKey = orgIdToKeyMap.get(orgUnitId);

                        Query updateQuery = new Query(Criteria.where("_id").is(alert.getId()));
                        Update update = new Update().set("orgUnitKey", orgKey);

                        mongoTemplate.updateFirst(updateQuery, update, AlertEntity.class, CaisAlertConstants.ALERTS);
                        mongoUpdated.incrementAndGet();

                        if (mongoUpdated.get() % 1000 == 0) {
                            log.info("Processed {} MongoDB alerts", mongoUpdated.get());
                        }
                    } else {
                        skipped.incrementAndGet();
                        log.warn("Skipping MongoDB alert {}: Invalid or missing orgUnitId", alert.getAlertId());
                    }
                } catch (Exception e) {
                    log.error("Error updating MongoDB alert {}: {}", alert.getAlertId(), e.getMessage());
                    skipped.incrementAndGet();
                }
            }

            // Update RDBMS records
            List<RdbmsAlertEntity> rdbmsAlerts = rdbmsAlertRepository.findAllByOrgKeyIsNull();
            log.info("Found {} RDBMS alerts requiring update", rdbmsAlerts.size());

            for (RdbmsAlertEntity alert : rdbmsAlerts) {
                try {
                    String orgUnitId = alert.getOrgUnitId();
                    if (orgUnitId != null && orgIdToKeyMap.containsKey(orgUnitId)) {
                        String orgKey = orgIdToKeyMap.get(orgUnitId);
                        alert.setOrgKey(orgKey);
                        rdbmsAlertRepository.save(alert);
                        rdbmsUpdated.incrementAndGet();

                        if (rdbmsUpdated.get() % 1000 == 0) {
                            log.info("Processed {} RDBMS alerts", rdbmsUpdated.get());
                        }
                    } else {
                        skipped.incrementAndGet();
                        log.warn("Skipping RDBMS alert {}: Invalid or missing orgUnitId", alert.getAlertId());
                    }
                } catch (Exception e) {
                    log.error("Error updating RDBMS alert {}: {}", alert.getAlertId(), e.getMessage());
                    skipped.incrementAndGet();
                }
            }

        } catch (Exception e) {
            log.error("Error during migration: {}", e.getMessage(), e);
            throw new RuntimeException("Migration failed", e);
        }

        log.info("Migration completed. MongoDB records updated: {}, RDBMS records updated: {}, Skipped: {}",
                mongoUpdated.get(), rdbmsUpdated.get(), skipped.get());
    }
}