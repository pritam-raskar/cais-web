package com.dair.cais.filter.repository;

import com.dair.cais.filter.domain.UserSavedFilter;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserFilterRepository extends JpaRepository<UserSavedFilter, Long> {

    @Query("SELECT f FROM UserSavedFilter f WHERE " +
            "(f.userId = :userId OR f.isPublic = true) AND " +
            "f.entityType.entityTypeId = :entityTypeId AND " +
            "f.entityIdentifier = :entityIdentifier")
    List<UserSavedFilter> findAccessibleFilters(
            @Param("userId") String userId,
            @Param("entityTypeId") Long entityTypeId,
            @Param("entityIdentifier") String entityIdentifier);

    Optional<UserSavedFilter> findByFilterIdAndUserId(Long filterId, String userId);

    List<UserSavedFilter> findByUserIdAndEntityType_EntityTypeIdAndEntityIdentifier(
            String userId, Long entityTypeId, String entityIdentifier);

    List<UserSavedFilter> findByIsPublicTrueAndEntityType_EntityTypeIdAndEntityIdentifier(
            Long entityTypeId, String entityIdentifier);

    Optional<UserSavedFilter> findByUserIdAndEntityType_EntityTypeIdAndEntityIdentifierAndIsDefault(
            String userId, Long entityTypeId, String entityIdentifier, Boolean isDefault);

    @Query("SELECT f FROM UserSavedFilter f WHERE " +
            "f.filterName LIKE %:searchTerm% OR " +
            "f.filterDescription LIKE %:searchTerm%")
    Page<UserSavedFilter> searchFilters(@Param("searchTerm") String searchTerm, Pageable pageable);

    boolean existsByUserIdAndEntityType_EntityTypeIdAndEntityIdentifierAndFilterNameIgnoreCase(
            String userId, Long entityTypeId, String entityIdentifier, String filterName);
}
