package com.dair.cais.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.dair.cais.entity.TradeEntity;

import io.swagger.v3.oas.annotations.Hidden;

@Hidden
@Repository
public interface TradeRepository extends JpaRepository<TradeEntity, String> {
    Page<TradeEntity> findByNameContainingIgnoreCase(String name, Pageable pageable);
}