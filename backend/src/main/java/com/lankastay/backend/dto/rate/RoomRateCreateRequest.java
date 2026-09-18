package com.lankastay.backend.dto.rate;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public class RoomRateCreateRequest {

    @NotNull(message = "Hotel ID is required")
    private Long hotelId;

    @NotNull(message = "Room ID is required")
    private Long roomId;

    @NotBlank(message = "Rate Plan Name is required")
    @Size(max = 150, message = "Rate Plan Name must not exceed 150 characters")
    private String ratePlanName;

    @NotBlank(message = "Rate Plan Code is required")
    @Size(max = 50, message = "Rate Plan Code must not exceed 50 characters")
    private String ratePlanCode;

    @NotNull(message = "Base Nightly Rate is required")
    @Positive(message = "Base Nightly Rate must be greater than 0")
    private Double baseNightlyRate;

    @DecimalMin(value = "0.0", message = "Weekend Nightly Rate must be positive")
    private Double weekendNightlyRate;

    @Size(max = 50, message = "Meal Plan must not exceed 50 characters")
    private String mealPlan = "ROOM_ONLY";

    @Size(max = 50, message = "Cancellation Policy must not exceed 50 characters")
    private String cancellationPolicy = "FLEXIBLE_24H";

    private Boolean depositRequired = false;

    @DecimalMin(value = "0.0", message = "Deposit Percentage must be >= 0")
    private Double depositPercentage = 0.0;

    private String status = "ACTIVE";

    public RoomRateCreateRequest() {}

    public Long getHotelId() { return hotelId; }
    public void setHotelId(Long hotelId) { this.hotelId = hotelId; }

    public Long getRoomId() { return roomId; }
    public void setRoomId(Long roomId) { this.roomId = roomId; }

    public String getRatePlanName() { return ratePlanName; }
    public void setRatePlanName(String ratePlanName) { this.ratePlanName = ratePlanName; }

    public String getRatePlanCode() { return ratePlanCode; }
    public void setRatePlanCode(String ratePlanCode) { this.ratePlanCode = ratePlanCode; }

    public Double getBaseNightlyRate() { return baseNightlyRate; }
    public void setBaseNightlyRate(Double baseNightlyRate) { this.baseNightlyRate = baseNightlyRate; }

    public Double getWeekendNightlyRate() { return weekendNightlyRate; }
    public void setWeekendNightlyRate(Double weekendNightlyRate) { this.weekendNightlyRate = weekendNightlyRate; }

    public String getMealPlan() { return mealPlan; }
    public void setMealPlan(String mealPlan) { this.mealPlan = mealPlan; }

    public String getCancellationPolicy() { return cancellationPolicy; }
    public void setCancellationPolicy(String cancellationPolicy) { this.cancellationPolicy = cancellationPolicy; }

    public Boolean getDepositRequired() { return depositRequired; }
    public void setDepositRequired(Boolean depositRequired) { this.depositRequired = depositRequired; }

    public Double getDepositPercentage() { return depositPercentage; }
    public void setDepositPercentage(Double depositPercentage) { this.depositPercentage = depositPercentage; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
