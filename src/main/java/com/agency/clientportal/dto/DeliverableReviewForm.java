package com.agency.clientportal.dto;

import com.agency.clientportal.entity.DeliverableStatus;
import jakarta.validation.constraints.NotNull;

public class DeliverableReviewForm {

    @NotNull(message = "Review status is required")
    private DeliverableStatus status;

    private String clientFeedback;

    public DeliverableStatus getStatus() {
        return status;
    }

    public void setStatus(DeliverableStatus status) {
        this.status = status;
    }

    public String getClientFeedback() {
        return clientFeedback;
    }

    public void setClientFeedback(String clientFeedback) {
        this.clientFeedback = clientFeedback;
    }
}
