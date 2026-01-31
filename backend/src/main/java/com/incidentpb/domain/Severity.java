package com.incidentpb.domain;

/**
 * Incident severity levels
 */
public enum Severity {
    CRITICAL,  // System down, major customer impact
    HIGH,      // Significant degradation
    MEDIUM,    // Minor issues, workaround available
    LOW        // Informational, no immediate impact
}