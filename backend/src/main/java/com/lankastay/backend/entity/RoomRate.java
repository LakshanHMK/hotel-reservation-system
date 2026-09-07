package com.lankastay.backend.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "room_rates")
public class RoomRate {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "hotel_id", nullable = false)
    private Long hotelId;

    @Column(name = "room_id", nullable = false)
    private Long roomId;

    @Column(name = "rate_plan_name", nullable = false, length = 150)
    private String ratePlanName;

    @Column(name = "rate_plan_code", nullable = false, length = 50)
    private String ratePlanCode;

    @Column(name = "base_nightly_rate", nullable = false)
    private Double baseNightlyRate;

    @Column(name = "weekend_nightly_rate")
    private Double weekendNightlyRate;

    @Column(name = "meal_plan", nullable = false, length = 50)
    private String mealPlan = "ROOM_ONLY";

    @Column(name = "cancellation_policy", nullable = false, length = 50)
    private String cancellationPolicy = "FLEXIBLE_24H";

    @Column(name = "deposit_required", nullable = false)
    private Boolean depositRequired = false;

    @Column(name = "deposit_percentage")
    private Double depositPercentage = 0.0;

    @Column(nullable = false, length = 20)
    private String status = "ACTIVE";

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public RoomRate() {}

    public RoomRate(Long hotelId, Long roomId, String ratePlanName, String ratePlanCode, Double baseNightlyRate, Double weekendNightlyRate, String mealPlan, String cancellationPolicy, Boolean depositRequired, Double depositPercentage, String status) {
        this.hotelId = hotelId;
        this.roomId = roomId;
        this.ratePlanName = ratePlanName;
        this.ratePlanCode = ratePlanCode;
        this.baseNightlyRate = baseNightlyRate;
        this.weekendNightlyRate = weekendNightlyRate;
        this.mealPlan = mealPlan;
        this.cancellationPolicy = cancellationPolicy;
        this.depositRequired = depositRequired;
        this.depositPercentage = depositPercentage;
        this.status = status;
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

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

    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}
