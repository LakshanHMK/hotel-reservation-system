package com.lankastay.backend.dto.rate;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public class RoomRateUpdateRequest {

    @Size(max = 150, message = "Rate Plan Name must not exceed 150 characters")
    private String ratePlanName;

    @Size(max = 50, message = "Rate Plan Code must not exceed 50 characters")
    private String ratePlanCode;

    @Positive(message = "Base Nightly Rate must be greater than 0")
    private Double baseNightlyRate;

    @DecimalMin(value = "0.0", message = "Weekend Nightly Rate must be positive")
    private Double weekendNightlyRate;

    @Size(max = 50, message = "Meal Plan must not exceed 50 characters")
    private String mealPlan;

    @Size(max = 50, message = "Cancellation Policy must not exceed 50 characters")
    private String cancellationPolicy;

    private Boolean depositRequired;

    @DecimalMin(value = "0.0", message = "Deposit Percentage must be >= 0")
    private Double depositPercentage;

    private String status;

    public RoomRateUpdateRequest() {}

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
