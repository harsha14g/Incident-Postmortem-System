package com.incidentpb.domain;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.Instant;
import java.util.List;
import java.util.Set;

/**
 * Core domain model representing an incident.
 * 
 * An incident is any production issue, outage, or notable event
 * that has been documented (postmortem, ticket, log summary).
 */

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "incidents")
public class Incident {

    @Id
    private String id;

    /**
     * Original title/summary from the incident report
     */
    @Indexed
    private String title;

    /**
     * Raw content: logs, postmortem text, ticket description
     */
    private String rawContent;

    /**
     * AI-generated summary
     */
    private IncidentSummary summary;

    /**
     * Services affected (e.g., "auth-service", "payment-api")
     */
    @Indexed
    private Set<String> services;

    /**
     * Severity: CRITICAL, HIGH, MEDIUM, LOW
     */
    @Indexed
    private Severity severity;

    /**
     * When the incident occurred
     */
    @Indexed
    private Instant occurredAt;

    /**
     * When this record was created in our system
     */
    @CreatedDate
    private Instant createdAt;

    /**
     * When this record was last updated
     */
    @LastModifiedDate
    private Instant updatedAt;

    /**
     * Source of the incident (e.g., "PagerDuty", "JIRA", "Manual Upload")
     */
    private String source;

    /**
     * Vector embedding for semantic search
     * This is a list of floats representing the meaning of the incident
     */
    private List<Float> embedding;

    /**
     * Additional metadata (cloud resources, team, etc.)
     */
    private IncidentMetadata metadata;

}