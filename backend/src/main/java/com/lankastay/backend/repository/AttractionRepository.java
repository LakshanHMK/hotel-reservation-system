package com.lankastay.backend.repository;

import com.lankastay.backend.entity.Attraction;
import com.lankastay.backend.entity.AttractionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AttractionRepository extends JpaRepository<Attraction, Long> {

    List<Attraction> findByDestinationIdOrderByDisplayOrderAsc(Long destinationId);

    List<Attraction> findByDestinationIdAndStatusOrderByDisplayOrderAsc(Long destinationId, AttractionStatus status);

    void deleteByDestinationId(Long destinationId);
}
