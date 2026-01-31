package com.incidentpb.domain;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * AI-generated structured summary of an incident
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IncidentSummary {

    /**
     * What caused the incident
     */
    private String rootCause;

    /**
     * Business/technical impact
     */
    private String impact;

    /**
     * How it was resolved
     */
    private String resolution;

    /**
     * Steps to prevent recurrence
     */
    private String prevention;

    /**
     * Short one-liner summary
     */
    private String shortSummary;
}