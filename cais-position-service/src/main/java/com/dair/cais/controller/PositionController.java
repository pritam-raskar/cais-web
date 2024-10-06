package com.dair.cais.controller;

import java.util.List;
import java.util.Map;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.dair.cais.model.Position;
import com.dair.cais.service.PositionService;

import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/positions")
@Tag(name = "positions")

public class PositionController {

   @Autowired
   private PositionService positionService;

   @PostMapping
   @Operation(summary = "Create a position")
   public ResponseEntity<Position> createPosition(@RequestBody Position alert) {
      Position createdPosition = positionService.createPosition(alert);
      return ResponseEntity.ok().body(createdPosition);
   }

   @Hidden
   @PostMapping("/bulk")
   @Operation(summary = "Create bulk positions")
   public ResponseEntity<List<Position>> createPositions(@RequestBody List<Position> trades) {
      List<Position> createdPositions = positionService.createPositions(trades);
      return ResponseEntity.ok().body(createdPositions);
   }

   @GetMapping("{positionId}")
   @Operation(summary = "Get a position by its id")
   public ResponseEntity<Position> getPositionById(@PathVariable final String positionId) {
      Position positionById = positionService.getPositionById(positionId);
      return ResponseEntity.ok().body(positionById);
   }

   @GetMapping("")
   @Operation(summary = "Get all positions; Use query params for search options like offset ,limit ,fuzzy search")
   public ResponseEntity<Map<String, Object>> getAllPositions(@RequestParam(required = false) String name,
         @Valid @RequestParam(defaultValue = "0") int offset,
         @Valid @RequestParam(defaultValue = "10") int limit,
         @RequestParam(required = false) boolean favourite,
         @RequestParam(required = false) boolean recent) {
      return ResponseEntity.ok().body(positionService.getAllPositions(name, offset, limit, favourite, recent));
   }
}
