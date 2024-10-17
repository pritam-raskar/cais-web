package com.dair.cais.access.Actions;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ActionRepository extends JpaRepository<ActionEntity, Integer> {

    List<ActionEntity> findByActionType(String actionType);

    List<ActionEntity> findByActionCategory(String actionCategory);
}
