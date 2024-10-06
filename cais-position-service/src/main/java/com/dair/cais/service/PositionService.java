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

import com.dair.cais.entity.PositionEntity;
import com.dair.cais.mapper.PositionMapper;
import com.dair.cais.model.Position;
import com.dair.cais.repository.PositionRepository;
import com.dair.exception.CaisBaseException;
import com.dair.exception.CaisNotFoundException;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class PositionService {

   @Autowired
   private PositionMapper positionMapper;
   @Autowired
   private PositionRepository positionRepository;

   public Position createPosition(Position position) {
      String extractPositionId = Objects.nonNull(position.getId()) ? position.getId() : UUID.randomUUID().toString();
      PositionEntity positionEntityToUpsert = positionMapper.toEntity(extractPositionId, position);
      PositionEntity upsertedPositionEntity = positionRepository.save(positionEntityToUpsert);
      return positionMapper.toModel(upsertedPositionEntity);
   }

   public List<Position> createPositions(List<Position> positions) {
      List<Position> createdPositions = positions.stream().map(a -> createPosition(a)).collect(Collectors.toList());
      return createdPositions;
   }

   public Position getPositionById(final String positionId) {
      Optional<PositionEntity> positionByIdOptional = positionRepository.findById(positionId);
      if (positionByIdOptional.isPresent()) {
         return positionMapper.toModel(positionByIdOptional.get());
      } else {
         throw new CaisNotFoundException();
      }
   }

   public Map<String, Object> getAllPositions(String name, int offset, int limit, boolean favourite, boolean recent) {
      try {

         Page<PositionEntity> allPositionEntities = routeToJpaMethod(name, offset, limit, favourite, recent);
         List<Position> allPosition = allPositionEntities.stream().map(a -> positionMapper.toModel(a))
               .collect(Collectors.toList());

         Map<String, Object> response = new HashMap<>();
         response.put("positions", allPosition);
         response.put("count", allPosition.size());
         return response;
      } catch (Exception e) {
         throw new CaisBaseException("Error retrieving positions");
      }
   }

   private Page<PositionEntity> routeToJpaMethod(String name, int offset, int limit, boolean favourite,
         boolean recent) {
      int pageNumber = (int) (Math.floor(offset / limit) + (offset % limit));

      Pageable paging = PageRequest.of(pageNumber, limit);
      Page<PositionEntity> positionEntityPages;
      if (StringUtils.isNotEmpty(name)) {
         positionEntityPages = positionRepository.findByNameContainingIgnoreCase(name, paging);
      } else {
         positionEntityPages = positionRepository.findAll(paging);
      }
      return positionEntityPages;
   }

}
