package com.incidentpb.domain;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.Map;

/**
 * Additional metadata about an incident
 * Flexible structure for cloud resources, teams, etc.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IncidentMetadata {

    /**
     * Team responsible (e.g., "Platform Team")
     */
    private String team;

    /**
     * Cloud resources involved (e.g., "ec2-i-12345", "rds-prod-db")
     */
    private String[] cloudResources;

    /**
     * Environment (e.g., "production", "staging")
     */
    private String environment;

    /**
     * Custom fields for extensibility
     */
    private Map<String, String> customFields;
}