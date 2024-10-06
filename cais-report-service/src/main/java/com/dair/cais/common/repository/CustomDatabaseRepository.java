package com.dair.cais.common.repository;

import java.util.List;

import org.springframework.stereotype.Repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Repository
public class CustomDatabaseRepository {

    @PersistenceContext
    private EntityManager entityManager;

    public List<String> findAllTableNames(String schema) {
        Query query = entityManager
                .createNativeQuery(
                        "SELECT table_name FROM information_schema.tables WHERE table_schema = 'data_info'");
        log.info(query.getResultList().toString());
        return query.getResultList();
    }
}