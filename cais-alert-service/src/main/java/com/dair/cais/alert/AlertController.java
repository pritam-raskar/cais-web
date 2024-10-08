package com.dair.cais.alert;

import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/alerts")
@Tag(name = "alerts")

public class AlertController {
   @Autowired
   private AlertService alertService;

   @PostMapping("/insert")
   @Operation(summary = "Create an alert with alert data payload and alert type")
   public ResponseEntity<AlertEntity> insertAlert(
           @RequestParam String alertType,
           @RequestBody AlertEntity alertEntity) {
      AlertEntity createdAlert = alertService.insertAlert(alertType, alertEntity);
      return ResponseEntity.ok().body(createdAlert);
   }

   @GetMapping("/alertId/{alertId}")
   @Operation(summary = "Get an alertId using the alert field alertId")
   public ResponseEntity<Alert> getAlertById(@PathVariable final String alertId) {
      Alert alertById = alertService.getAlertOnId(alertId);
      return ResponseEntity.ok().body(alertById);
   }


   @GetMapping("/findAlertsByOrgFamily")
   @Operation(summary = "Get alerts from orgFamily")
   public List<AlertEntity> findAlertsByOrgFamily(@RequestParam("org") String substring) {
      return alertService.findAlertsByOrgFamily(substring);
   }

   @GetMapping("/findAlertsByOrg")
   @Operation(summary = "Get alerts from orgId")
   public List<AlertEntity> findAlertsByOrg(@RequestParam("org") String substring) {
      return alertService.findAlertsByOrg(substring);
   }

   @GetMapping("/find")
   @Operation(summary = "Mandatory/base level field filters to fetch the list of alerts")
   public List<AlertEntity> findAlertsByCriteria(
           @RequestParam(required = false) String alertId,
           @RequestParam(required = false) String createDate,
           @RequestParam(required = false) String lastUpdateDate,
           @RequestParam(required = false) String totalScore,
           @RequestParam(required = false) String createdBy,
           @RequestParam(required = false) String businessDate,
           @RequestParam(required = false) String focalEntity,
           @RequestParam(required = false) String focus,
           @RequestParam(required = false) String alertTypeId,
           @RequestParam(required = false) String alertRegion,
           @RequestParam(required = false) String alertGroupId,
           @RequestParam(required = false) Boolean isConsolidated,
           @RequestParam(required = false) Boolean isActive,
           @RequestParam(required = false) Boolean hasMultipleScenario,
           @RequestParam(required = false) Boolean isDeleted,
           @RequestParam(required = false) String orgUnitId,
           @RequestParam(required = false) String orgFamily,
           @RequestParam(required = false) String previousOrgUnitId,
           @RequestParam(required = false) Boolean isOrgUnitUpdated,
           @RequestParam(required = false) Boolean isRelatedAlert,
           @RequestParam(required = false) String ownerId,
           @RequestParam(required = false) String ownerName,
           @RequestParam(required = false) String status,
           @RequestParam(required = false) String alertStepId,
           @RequestParam(required = false) String alertStepName,
           @RequestParam(required = false) Boolean isCaseCreated
   ) {
      return alertService.findAlertsByCriteria(
              alertId, createDate, lastUpdateDate, totalScore, createdBy, businessDate,
              focalEntity, focus, alertTypeId, alertRegion, alertGroupId, isConsolidated,
              isActive, hasMultipleScenario, isDeleted, orgUnitId, orgFamily, previousOrgUnitId,
              isOrgUnitUpdated, isRelatedAlert, ownerId, ownerName, status, alertStepId, alertStepName, isCaseCreated
      );
   }

   @PatchMapping("/changescore")
   public ResponseEntity<Alert> updateTotalScore(@PathVariable String alertId, @RequestParam int totalScore) {
      Alert updatedAlert = alertService.updateTotalScore(alertId, totalScore);
      return ResponseEntity.ok().body(updatedAlert);
   }


   @PatchMapping("/changeowner")
   public ResponseEntity<Alert> updateOwnerId(@PathVariable String alertId, @RequestParam String ownerId) {
      Alert updatedAlert = alertService.updateOwnerId(alertId, ownerId);
      return ResponseEntity.ok().body(updatedAlert);
   }

   @PatchMapping("/changeorg")
   public ResponseEntity<Alert> updateOrgUnitId(@PathVariable String alertId, @RequestParam String orgUnitId) {
      Alert updatedAlert = alertService.updateOrgUnitId(alertId, orgUnitId);
      return ResponseEntity.ok().body(updatedAlert);
   }

   @PatchMapping("/changestatus")
   public ResponseEntity<Alert> updateStatus(@PathVariable String alertId, @RequestParam String statusId) {
      Alert updatedAlert = alertService.updateStatus(alertId, statusId);
      return ResponseEntity.ok().body(updatedAlert);
   }

   @PatchMapping("/changestep/{alertId}")
   public ResponseEntity<Alert> changeStep(@PathVariable String alertId, @RequestParam Long stepId) {
      Alert updatedAlert = alertService.changeStep(alertId, stepId);
      return ResponseEntity.ok(updatedAlert);
   }


   @GetMapping("")
   @Operation(summary = "Get all alerts; Use query params for search options like offset ,limit ,fuzzy search")
   public ResponseEntity<Map<String, Object>> getAllAlerts(
           @RequestParam(required = false) String name,
           @RequestParam(required = false, name = "state") String state,
           @RequestParam(required = false, name = "accountNumber") List<String> accountNumberList,
           @RequestParam(required = false, name = "owner") List<String> owners,
           @RequestParam(required = false, name = "assignee") List<String> assignees,
           @RequestParam(required = false, name = "from") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Date createdDateFrom,
           @RequestParam(required = false, name = "to") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Date createdDateTo,
           @Valid @RequestParam(defaultValue = "10") int limit,
           @Valid @RequestParam(defaultValue = "0") int offset) {

      Map<String, Object> allAlerts = alertService.getAllAlerts(name, state, accountNumberList, owners, assignees,
              createdDateFrom, createdDateTo, limit, offset);

      return ResponseEntity.ok().body(allAlerts);
   }

   @PatchMapping("{alertId}")
   @Operation(summary = "Update an alert")
   public ResponseEntity<Alert> patchAlert(@PathVariable final String alertId,
         @RequestParam(required = true) String alertType, @RequestBody Alert alert) {
      Alert updatedAlert = alertService.patchAlert(alertId, alertType, alert);
      return ResponseEntity.ok().body(updatedAlert);
   }

   @DeleteMapping("{alertId}")
   @Operation(summary = "Delete an Alert by its id")
   public ResponseEntity<Alert> deleteAlertById(@PathVariable final String alertId,
         @RequestParam(required = true) String alertType) {
      alertService.deleteAlertById(alertId, alertType);
      return ResponseEntity.ok().build();
   }

   @Hidden
   @PostMapping("/bulk")
   @Operation(summary = "Create bulk alerts")
   public ResponseEntity<List<Alert>> createAlerts(@RequestBody List<Alert> alerts) {
      List<Alert> createdAlerts = alertService.createAlerts(alerts);
      return ResponseEntity.ok().body(createdAlerts);
   }



//   @PostMapping("/create")
//   @Operation(summary = "Create an alert")
//   public ResponseEntity<Alert> createAlert(@RequestBody Alert alert) {
//      Alert createdAlert = alertService.createAlert(alert);
//      return ResponseEntity.ok().body(createdAlert);
//   }

   @PostMapping("/create")
   @Operation(summary = "Create an alert")
   public ResponseEntity<Alert> createAlert(@RequestBody Alert alert) {
      Alert createdAlert = alertService.createAlert(alert);
      return ResponseEntity.ok().body(createdAlert);
   }

   @ExceptionHandler(AlertValidationException.class)
   public ResponseEntity<Map<String, List<String>>> handleAlertValidationException(AlertValidationException ex) {
      Map<String, List<String>> errorResponse = new HashMap<>();
      errorResponse.put("errors", ex.getErrors());
      return ResponseEntity.badRequest().body(errorResponse);
   }


   @GetMapping("{alertId}")
   @Operation(summary = "Get an Alert by its mongoDB id")
   public ResponseEntity<Alert> getAlertById(@PathVariable final String alertId,
                                             @RequestParam(required = false) String alertType) {
      Alert alertById = alertService.getAlertById(alertId, alertType);
      return ResponseEntity.ok().body(alertById);
   }



}