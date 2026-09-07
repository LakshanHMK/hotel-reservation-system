package com.lankastay.backend.repository;

import com.lankastay.backend.entity.RoomRate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface RoomRateRepository extends JpaRepository<RoomRate, Long> {
    List<RoomRate> findByHotelId(Long hotelId);
    List<RoomRate> findByRoomId(Long roomId);
    List<RoomRate> findByRoomIdAndStatus(Long roomId, String status);
}
