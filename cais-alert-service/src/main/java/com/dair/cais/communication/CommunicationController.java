package com.dair.cais.communication;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/communications")
public class CommunicationController {

    @Autowired
    private CommunicationService communicationService;

    @PostMapping("/create")
    public ResponseEntity<Communication> createCommunication(@RequestBody Communication communication) {
        Communication createdCommunication = communicationService.createCommunication(communication);
        return ResponseEntity.ok(createdCommunication);
    }

    @GetMapping("/alert/{alertId}")
    public ResponseEntity<List<Communication>> getCommunicationsByAlertId(@PathVariable String alertId) {
        List<Communication> communications = communicationService.getCommunicationsByAlertId(alertId);
        return ResponseEntity.ok(communications);
    }
}