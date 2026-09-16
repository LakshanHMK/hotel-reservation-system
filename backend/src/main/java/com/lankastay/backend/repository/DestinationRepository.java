package com.lankastay.backend.repository;

import com.lankastay.backend.entity.Destination;
import com.lankastay.backend.entity.DestinationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DestinationRepository extends JpaRepository<Destination, Long>, JpaSpecificationExecutor<Destination> {

    Optional<Destination> findBySlug(String slug);

    boolean existsBySlug(String slug);

    boolean existsBySlugAndIdNot(String slug, Long id);

    boolean existsByNameIgnoreCaseAndIdNot(String name, Long id);

    boolean existsByNameIgnoreCase(String name);

    List<Destination> findByStatus(DestinationStatus status);

    @Query("SELECT d FROM Destination d WHERE d.status = :status ORDER BY d.name ASC")
    List<Destination> findActiveDestinationsOrdered(@Param("status") DestinationStatus status);

    @Query("SELECT d FROM Destination d WHERE " +
           "(:status IS NULL OR d.status = :status) AND " +
           "(:region IS NULL OR LOWER(d.region) = LOWER(:region)) AND " +
           "(:district IS NULL OR LOWER(d.district) = LOWER(:district)) AND " +
           "(:search IS NULL OR LOWER(d.name) LIKE LOWER(CONCAT('%', :search, '%')) OR LOWER(d.shortDescription) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<Destination> findByManagementFilters(
            @Param("status") DestinationStatus status,
            @Param("region") String region,
            @Param("district") String district,
            @Param("search") String search,
            Pageable pageable
    );
}
