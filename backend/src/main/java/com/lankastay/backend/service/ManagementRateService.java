package com.lankastay.backend.service;

import com.lankastay.backend.entity.RoomRate;
import com.lankastay.backend.repository.RoomRateRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;

@Service
public class ManagementRateService {
    private final RoomRateRepository roomRateRepository;

    public ManagementRateService(RoomRateRepository roomRateRepository) {
        this.roomRateRepository = roomRateRepository;
    }

    @Transactional(readOnly = true)
    public List<RoomRate> getRatesForHotel(Long hotelId) {
        if (hotelId == null) {
            return roomRateRepository.findAll();
        }
        return roomRateRepository.findByHotelId(hotelId);
    }

    @Transactional(readOnly = true)
    public List<RoomRate> getRatesForRoom(Long roomId) {
        return roomRateRepository.findByRoomId(roomId);
    }

    @Transactional(readOnly = true)
    public Optional<RoomRate> getRateById(Long id) {
        return roomRateRepository.findById(id);
    }

    @Transactional
    public RoomRate createRate(RoomRate rate) {
        return roomRateRepository.save(rate);
    }

    @Transactional
    public RoomRate updateRate(Long id, RoomRate updated) {
        RoomRate rate = roomRateRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Room Rate not found with ID: " + id));
        rate.setRatePlanName(updated.getRatePlanName());
        rate.setRatePlanCode(updated.getRatePlanCode());
        rate.setBaseNightlyRate(updated.getBaseNightlyRate());
        rate.setWeekendNightlyRate(updated.getWeekendNightlyRate());
        rate.setMealPlan(updated.getMealPlan());
        rate.setCancellationPolicy(updated.getCancellationPolicy());
        rate.setDepositRequired(updated.getDepositRequired());
        rate.setDepositPercentage(updated.getDepositPercentage());
        rate.setStatus(updated.getStatus());
        return roomRateRepository.save(rate);
    }

    @Transactional
    public void deleteRate(Long id) {
        roomRateRepository.deleteById(id);
    }
}
