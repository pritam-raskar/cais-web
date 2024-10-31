package com.dair.cais.audit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.ZonedDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/audit")
public class AuditTrailController {

    @Autowired
    private AuditTrailService auditTrailService;

    @PostMapping("/log")
    public ResponseEntity<AuditTrail> logAction(@RequestBody AuditLogRequest request) {
        AuditTrail auditTrail = auditTrailService.logAction(
                request.getUserId(),
                request.getUserRole(),
                request.getActionId(),
                request.getDescription(),
                request.getCategory(),
                request.getAffectedItemType(),
                request.getAffectedItemId(),
                request.getOldValue(),
                request.getNewValue()
        );
        return ResponseEntity.ok(auditTrail);
    }


    @GetMapping("/user/{userId}")
    public ResponseEntity<List<AuditTrail>> getAuditTrailByUser(
            @PathVariable Long userId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) ZonedDateTime start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) ZonedDateTime end) {
        List<AuditTrail> auditTrails = auditTrailService.getAuditTrailByUser(userId, start, end);
        return ResponseEntity.ok(auditTrails);
    }

    @GetMapping("/item")
    public ResponseEntity<List<AuditTrail>> getAuditTrailByItem(
            @RequestParam String itemType,
            @RequestParam String itemId) {
        List<AuditTrail> auditTrails = auditTrailService.getAuditTrailByItem(itemType, itemId);
        return ResponseEntity.ok(auditTrails);
    }

    @GetMapping("/item/stephistory")
    public ResponseEntity<List<String>> getAuditTrailStepHistory(

            @RequestParam String itemId,
            @RequestParam Integer actionId) {
        List<String> auditTrails = auditTrailService.getAuditTrailStepHistory( itemId, actionId);
        return ResponseEntity.ok(auditTrails);
    }
}
