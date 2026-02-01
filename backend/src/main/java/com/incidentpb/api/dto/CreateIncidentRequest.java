package com.incidentpb.api.dto;

import java.time.Instant;
import java.util.Set;

import com.incidentpb.domain.Severity;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * Request payload for creating an incident
 */
@Data
public class CreateIncidentRequest {

    @NotBlank(message = "Title is required")
    private String title;

    @NotBlank(message = "Content is required")
    private String rawContent;

    @NotNull(message = "Severity is required")
    private Severity severity;

    private Set<String> services;

    private Instant occurredAt;

    private String source;

    private String team;

    private String environment;
}