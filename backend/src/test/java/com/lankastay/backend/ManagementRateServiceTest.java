package com.lankastay.backend;

import com.lankastay.backend.entity.RoomRate;
import com.lankastay.backend.repository.RoomRateRepository;
import com.lankastay.backend.service.ManagementRateService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ManagementRateServiceTest {

    @Mock
    private RoomRateRepository roomRateRepository;

    @InjectMocks
    private ManagementRateService rateService;

    private RoomRate sampleRate;

    @BeforeEach
    void setUp() {
        sampleRate = new RoomRate(
                301L,
                101L,
                "Standard Flexible Rate",
                "FLEX-BB",
                38500.0,
                42000.0,
                "BED_AND_BREAKFAST",
                "FLEXIBLE_24H",
                false,
                0.0,
                "ACTIVE"
        );
        sampleRate.setId(1L);
    }

    @Test
    @DisplayName("1. Retrieve rates for specific hotel")
    void testGetRatesForHotel() {
        when(roomRateRepository.findByHotelId(301L)).thenReturn(List.of(sampleRate));

        List<RoomRate> rates = rateService.getRatesForHotel(301L);

        assertNotNull(rates);
        assertEquals(1, rates.size());
        assertEquals("Standard Flexible Rate", rates.get(0).getRatePlanName());
        verify(roomRateRepository, times(1)).findByHotelId(301L);
    }

    @Test
    @DisplayName("2. Retrieve rates for specific room")
    void testGetRatesForRoom() {
        when(roomRateRepository.findByRoomId(101L)).thenReturn(List.of(sampleRate));

        List<RoomRate> rates = rateService.getRatesForRoom(101L);

        assertNotNull(rates);
        assertEquals(1, rates.size());
        assertEquals("FLEX-BB", rates.get(0).getRatePlanCode());
        verify(roomRateRepository, times(1)).findByRoomId(101L);
    }

    @Test
    @DisplayName("3. Retrieve rate by ID")
    void testGetRateById() {
        when(roomRateRepository.findById(1L)).thenReturn(Optional.of(sampleRate));

        Optional<RoomRate> rateOpt = rateService.getRateById(1L);

        assertTrue(rateOpt.isPresent());
        assertEquals(38500.0, rateOpt.get().getBaseNightlyRate());
        verify(roomRateRepository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("4. Create new room rate plan")
    void testCreateRate() {
        when(roomRateRepository.save(any(RoomRate.class))).thenReturn(sampleRate);

        RoomRate created = rateService.createRate(sampleRate);

        assertNotNull(created);
        assertEquals(1L, created.getId());
        assertEquals("ACTIVE", created.getStatus());
        verify(roomRateRepository, times(1)).save(sampleRate);
    }

    @Test
    @DisplayName("5. Update existing room rate plan")
    void testUpdateRate() {
        when(roomRateRepository.findById(1L)).thenReturn(Optional.of(sampleRate));
        when(roomRateRepository.save(any(RoomRate.class))).thenAnswer(i -> i.getArgument(0));

        RoomRate updateInfo = new RoomRate();
        updateInfo.setRatePlanName("Updated Plan");
        updateInfo.setRatePlanCode("UPD-01");
        updateInfo.setBaseNightlyRate(45000.0);
        updateInfo.setWeekendNightlyRate(48000.0);
        updateInfo.setMealPlan("HALF_BOARD");
        updateInfo.setCancellationPolicy("NON_REFUNDABLE");
        updateInfo.setDepositRequired(true);
        updateInfo.setDepositPercentage(20.0);
        updateInfo.setStatus("ACTIVE");

        RoomRate updated = rateService.updateRate(1L, updateInfo);

        assertEquals("Updated Plan", updated.getRatePlanName());
        assertEquals(45000.0, updated.getBaseNightlyRate());
        assertEquals("HALF_BOARD", updated.getMealPlan());
        assertTrue(updated.getDepositRequired());
    }

    @Test
    @DisplayName("6. Delete rate by ID")
    void testDeleteRate() {
        doNothing().when(roomRateRepository).deleteById(1L);

        rateService.deleteRate(1L);

        verify(roomRateRepository, times(1)).deleteById(1L);
    }
}
