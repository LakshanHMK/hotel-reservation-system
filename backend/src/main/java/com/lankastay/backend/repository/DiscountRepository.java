package com.lankastay.backend.repository;

import com.lankastay.backend.entity.Discount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DiscountRepository extends JpaRepository<Discount, Long> {
    Optional<Discount> findByCode(String code);
    List<Discount> findByHotelId(Long hotelId);
    List<Discount> findByHotelIdAndStatus(Long hotelId, String status);
    List<Discount> findByStatus(String status);
}
