package com.lankastay.backend.dto.destination;

import com.lankastay.backend.entity.DestinationStatus;
import jakarta.validation.constraints.NotNull;

public class DestinationStatusUpdateRequest {

    @NotNull(message = "Status is required")
    private DestinationStatus status;

    public DestinationStatusUpdateRequest() {}

    public DestinationStatusUpdateRequest(DestinationStatus status) {
        this.status = status;
    }

    public DestinationStatus getStatus() {
        return status;
    }

    public void setStatus(DestinationStatus status) {
        this.status = status;
    }
}
