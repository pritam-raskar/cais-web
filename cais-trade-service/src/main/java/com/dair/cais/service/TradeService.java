package com.dair.cais.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.dair.cais.entity.TradeEntity;
import com.dair.cais.mapper.TradeMapper;
import com.dair.cais.model.Trade;
import com.dair.cais.repository.TradeRepository;
import com.dair.exception.CaisBaseException;
import com.dair.exception.CaisNotFoundException;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class TradeService {

   @Autowired
   private TradeMapper tradeMapper;
   @Autowired
   private TradeRepository tradeRepository;

   public Trade createTrade(Trade trade) {
      String extractTradeId = Objects.nonNull(trade.getId()) ? trade.getId() : UUID.randomUUID().toString();
      TradeEntity tradeEntityToUpsert = tradeMapper.toEntity(extractTradeId, trade);
      TradeEntity upsertedTradeEntity = tradeRepository.save(tradeEntityToUpsert);
      return tradeMapper.toModel(upsertedTradeEntity);
   }

   public List<Trade> createTrades(List<Trade> trades) {
      List<Trade> createdTrades = trades.stream().map(a -> createTrade(a)).collect(Collectors.toList());
      return createdTrades;
   }

   public Trade getTradeById(final String tradeId) {
      Optional<TradeEntity> tradeByIdOptional = tradeRepository.findById(tradeId);
      if (tradeByIdOptional.isPresent()) {
         return tradeMapper.toModel(tradeByIdOptional.get());
      } else {
         throw new CaisNotFoundException();
      }
   }

   public Map<String, Object> getAllTrades(String name, int offset, int limit, boolean favourite, boolean recent) {
      try {

         Page<TradeEntity> allTradeEntities = routeToJpaMethod(name, offset, limit, favourite, recent);
         List<Trade> allTrades = allTradeEntities.stream().map(a -> tradeMapper.toModel(a))
               .collect(Collectors.toList());

         Map<String, Object> response = new HashMap<>();
         response.put("trades", allTrades);
         response.put("count", allTrades.size());
         return response;
      } catch (Exception e) {
         throw new CaisBaseException("Error retrieving trades");
      }
   }

   private Page<TradeEntity> routeToJpaMethod(String name, int offset, int limit, boolean favourite,
         boolean recent) {
      int pageNumber = (int) (Math.floor(offset / limit) + (offset % limit));

      Pageable paging = PageRequest.of(pageNumber, limit);
      Page<TradeEntity> tradeEntityPages;
      if (StringUtils.isNotEmpty(name)) {
         tradeEntityPages = tradeRepository.findByNameContainingIgnoreCase(name, paging);
      } else {
         tradeEntityPages = tradeRepository.findAll(paging);
      }
      return tradeEntityPages;
   }

}
