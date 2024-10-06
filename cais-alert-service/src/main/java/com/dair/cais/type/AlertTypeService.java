package com.dair.cais.type;

import com.dair.exception.CaisBaseException;
import com.dair.exception.CaisIllegalArgumentException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.validation.Valid;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
public class AlertTypeService {

   @Autowired
   private AlertTypeMapper typeMapper;


   @Autowired
   private AlertTypeRepository typeRepository;


   //fetch alert type fields
   public AlertType getAlertTypeFields(final String alertType) {
      AlertTypeEntity typeById = typeRepository.getAlertTypeFields(alertType);
      return typeMapper.toModel(typeById);
   }

   public AlertType createAlertType(AlertType type) {
      AlertTypeEntity upsertedAlertType = typeRepository.createUpsertAlertType(typeMapper.toEntity(type));
      return typeMapper.toModel(upsertedAlertType);
   }

   public AlertType patchAlertType(String typeId, String typeType, AlertType type) {
      AlertTypeEntity upsertedAlertType = typeRepository.patchAlertType(typeMapper.toEntity(typeId, type), typeType);
      return typeMapper.toModel(upsertedAlertType);
   }

   public AlertType getAlertTypeById(final String typeId) {
      AlertTypeEntity typeById = typeRepository.getAlertTypeById(typeId);
      return typeMapper.toModel(typeById);
   }



   public Map<String, Object> getAllAlertTypes(String name, String state, List<String> accountNumbers, List<String> owners,
         List<String> assignees, Date createdDateFrom, Date createdDateTo, @Valid int limit, @Valid int offset) {
      validateRequestParams(name, state, accountNumbers, owners, assignees, createdDateFrom, createdDateTo, offset,
            limit);

      try {
         List<AlertTypeEntity> allAlertTypeEntities = typeRepository.getAllAlertTypes(name, state, accountNumbers, owners,
               assignees,
               createdDateFrom, createdDateTo, offset, limit);

         List<AlertType> allAlertTypes = allAlertTypeEntities.stream().map(a -> typeMapper.toModel(a))
               .collect(Collectors.toList());

         // Page<AlertTypeEntity> workspaceEntityPages = routeToJpaMethod(name, offset,
         // limit, favourite, recent);
         // List<AlertType> types = workspaceEntityPages.getContent().stream().map(w ->
         // workspaceMapper.toModel(w))
         // .collect(Collectors.toList());

         Map<String, Object> response = new HashMap<>();
         response.put("types", allAlertTypes);
         response.put("count", allAlertTypes.size());
         return response;
      } catch (Exception e) {
         throw new CaisBaseException("Error retrieving types");
      }
   }

   public List<AlertType> createAlertTypes(List<AlertType> types) {
      List<AlertType> createdAlertTypes = types.stream().map(a -> createAlertType(a)).collect(Collectors.toList());
      return createdAlertTypes;
   }

   private void validateRequestParams(String name, String state, List<String> accountNumbers,
         List<String> owners, List<String> assignees, Date createdDateFrom, Date createdDateTo, int offset, int limit) {
      StringBuilder errorMessage = new StringBuilder();

      if (name != null && !name.isEmpty()) {
         if (name.length() > 20) {
            errorMessage.append("name cannot be longer than 20 characters;");
         }
      }

      if (accountNumbers != null && accountNumbers.size() > 5) {
         errorMessage.append("accountNumber list cannot be greater than 5;");
      }

      if (owners != null && owners.size() > 5) {
         errorMessage.append("owner list cannot be greater than 5;");
      }

      if (assignees != null && assignees.size() > 5) {
         errorMessage.append("assignee list cannot be greater than 5;");
      }

      if (createdDateFrom != null && createdDateTo != null) {
         if (createdDateFrom.after(createdDateTo)) {
            errorMessage.append("from date cannot be after to date;");
         }
      }

      if (limit < 0) {
         errorMessage.append("limit cannot be negative;");
      }
      if (offset < 0) {
         errorMessage.append("offset cannot be negative;");
      }
      if (errorMessage.isEmpty()) {
         return;
      }

      throw new CaisIllegalArgumentException(errorMessage.toString());
   }

}
