package com.dair.cais.workflow.mapper;

import com.dair.cais.workflow.entity.WorkflowEntity;
import com.dair.cais.workflow.model.Workflow;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Mapper for converting between Workflow and WorkflowEntity objects.
 */
@Slf4j
@Component
public class WorkflowMapper {

    public Workflow toModel(WorkflowEntity entity) {
        log.debug("Converting WorkflowEntity to Workflow model: {}", entity);
        if (entity == null) {
            return null;
        }

        Workflow workflow = new Workflow();
        workflow.setWorkflowId(entity.getWorkflowId());
        workflow.setWorkflowName(entity.getWorkflowName());
        workflow.setDescription(entity.getDescription());
        workflow.setCreatedBy(entity.getCreatedBy());
        workflow.setCreatedDate(entity.getCreatedDate());
        workflow.setUpdatedDate(entity.getUpdatedDate());
        workflow.setUpdatedBy(entity.getUpdatedBy());

        log.debug("Successfully converted to Workflow model: {}", workflow);
        return workflow;
    }

    public WorkflowEntity toEntity(Workflow model) {
        log.debug("Converting Workflow model to entity: {}", model);
        if (model == null) {
            return null;
        }

        WorkflowEntity entity = new WorkflowEntity();
        entity.setWorkflowId(model.getWorkflowId());
        entity.setWorkflowName(model.getWorkflowName());
        entity.setDescription(model.getDescription());
        entity.setCreatedBy(model.getCreatedBy());
        entity.setCreatedDate(model.getCreatedDate());
        entity.setUpdatedDate(LocalDateTime.now());
        entity.setUpdatedBy(model.getUpdatedBy());

        log.debug("Successfully converted to WorkflowEntity: {}", entity);
        return entity;
    }

    public List<Workflow> toModelList(List<WorkflowEntity> entities) {
        if (entities == null) {
            return null;
        }

        List<Workflow> workflows = new ArrayList<>(entities.size());
        for (WorkflowEntity entity : entities) {
            workflows.add(toModel(entity));
        }

        return workflows;
    }

    public List<WorkflowEntity> toEntityList(List<Workflow> models) {
        if (models == null) {
            return null;
        }

        List<WorkflowEntity> entities = new ArrayList<>(models.size());
        for (Workflow model : models) {
            entities.add(toEntity(model));
        }

        return entities;
    }
}