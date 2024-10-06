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

import com.dair.cais.model.Trade;
import com.dair.cais.service.TradeService;

import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/trades")
@Tag(name = "trades")

public class TradeController {

   @Autowired
   private TradeService tradeService;

   @PostMapping
   @Operation(summary = "Create a trade")
   public ResponseEntity<Trade> createTrade(@RequestBody Trade alert) {
      Trade createdTrade = tradeService.createTrade(alert);
      return ResponseEntity.ok().body(createdTrade);
   }

   @Hidden
   @PostMapping("/bulk")
   @Operation(summary = "Create bulk trades")
   public ResponseEntity<List<Trade>> createTrades(@RequestBody List<Trade> trades) {
      List<Trade> createdTrades = tradeService.createTrades(trades);
      return ResponseEntity.ok().body(createdTrades);
   }

   @GetMapping("{tradeId}")
   @Operation(summary = "Get a trade by its id")
   public ResponseEntity<Trade> getTradeById(@PathVariable final String tradeId) {
      Trade tradeById = tradeService.getTradeById(tradeId);
      return ResponseEntity.ok().body(tradeById);
   }

   @GetMapping("")
   @Operation(summary = "Get all trades; Use query params for search options like offset ,limit ,fuzzy search")
   public ResponseEntity<Map<String, Object>> getAllTrades(@RequestParam(required = false) String name,
         @Valid @RequestParam(defaultValue = "0") int offset,
         @Valid @RequestParam(defaultValue = "10") int limit,
         @RequestParam(required = false) boolean favourite,
         @RequestParam(required = false) boolean recent) {
      return ResponseEntity.ok().body(tradeService.getAllTrades(name, offset, limit, favourite, recent));
   }
}
