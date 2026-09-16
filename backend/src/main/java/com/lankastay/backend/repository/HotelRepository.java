// SE2030 LankaStay - Hotel Management
// Functional Owner: Wickramasinghe M.P.T.H - IT25300115

package com.lankastay.backend.repository;

import com.lankastay.backend.entity.Hotel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface HotelRepository extends JpaRepository<Hotel, Long> {

    Optional<Hotel> findBySlug(String slug);

    boolean existsBySlug(String slug);

    boolean existsBySlugAndIdNot(String slug, Long id);

    List<Hotel> findByDestinationId(Long destinationId);

    List<Hotel> findByStatus(String status);

    @Query("SELECT h FROM Hotel h WHERE " +
           "(:search IS NULL OR LOWER(h.name) LIKE LOWER(CONCAT('%', :search, '%')) OR LOWER(h.shortDescription) LIKE LOWER(CONCAT('%', :search, '%'))) AND " +
           "(:destinationId IS NULL OR h.destinationId = :destinationId) AND " +
           "(:setupStatus IS NULL OR h.setupStatus = :setupStatus) AND " +
           "(:publicationStatus IS NULL OR h.publicationStatus = :publicationStatus) AND " +
           "(:propertyType IS NULL OR LOWER(h.propertyType) = LOWER(:propertyType))")
    List<Hotel> filterHotels(
            @Param("search") String search,
            @Param("destinationId") Long destinationId,
            @Param("setupStatus") String setupStatus,
            @Param("publicationStatus") String publicationStatus,
            @Param("propertyType") String propertyType
    );
}
