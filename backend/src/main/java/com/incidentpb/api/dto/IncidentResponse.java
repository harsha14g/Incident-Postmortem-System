package com.incidentpb.api.dto;

import com.incidentpb.domain.Incident;
import com.incidentpb.domain.IncidentSummary;
import com.incidentpb.domain.Severity;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.Set;

/**
 * Response payload for incident data
 */
@Data
@Builder
public class IncidentResponse {

    private String id;
    private String title;
    private String rawContent;
    private IncidentSummary summary;
    private Set<String> services;
    private Severity severity;
    private Instant occurredAt;
    private Instant createdAt;
    private String source;
    private String team;
    private String environment;

    public static IncidentResponse from(Incident incident) {
        return IncidentResponse.builder()
                .id(incident.getId())
                .title(incident.getTitle())
                .rawContent(incident.getRawContent())
                .summary(incident.getSummary())
                .services(incident.getServices())
                .severity(incident.getSeverity())
                .occurredAt(incident.getOccurredAt())
                .createdAt(incident.getCreatedAt())
                .source(incident.getSource())
                .team(incident.getMetadata() != null ? incident.getMetadata().getTeam() : null)
                .environment(incident.getMetadata() != null ? incident.getMetadata().getEnvironment() : null)
                .build();
    }
}