package com.agency.clientportal.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class MessageForm {

    @NotNull(message = "Project ID is required")
    private Long projectId;

    @NotBlank(message = "Message text cannot be blank")
    private String content;

    public Long getProjectId() {
        return projectId;
    }

    public void setProjectId(Long projectId) {
        this.projectId = projectId;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
