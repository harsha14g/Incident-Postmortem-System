package com.incidentpb.api;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.incidentpb.api.dto.CreateIncidentRequest;
import com.incidentpb.api.dto.IncidentResponse;
import com.incidentpb.domain.Incident;
import com.incidentpb.domain.Severity;
import com.incidentpb.service.IncidentService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * REST API for incident management
 */
@RestController
@RequestMapping("/api/incidents")
@RequiredArgsConstructor
@Slf4j
public class IncidentController {

    private final IncidentService incidentService;

    /**
     * Create a new incident
     */
    @PostMapping
    public ResponseEntity<IncidentResponse> createIncident(
            @Valid @RequestBody CreateIncidentRequest request) {
        log.info("Received request to create incident: {}", request.getTitle());
        Incident incident = incidentService.createIncident(request);
        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(IncidentResponse.from(incident));
    }

    /**
     * Get all incidents
     */
    @GetMapping
    public ResponseEntity<List<IncidentResponse>> getAllIncidents(
            @RequestParam(required = false) Severity severity,
            @RequestParam(required = false) String service) {
        List<Incident> incidents;
        if (severity != null) {
            log.info("Fetching incidents with severity: {}", severity);
            incidents = incidentService.getIncidentsBySeverity(severity);
        } else if (service != null) {
            log.info("Fetching incidents for service: {}", service);
            incidents = incidentService.getIncidentsByService(service);
        } else {
            log.info("Fetching all incidents");
            incidents = incidentService.getAllIncidents();
        }
        List<IncidentResponse> response = incidents.stream()
            .map(IncidentResponse::from)
            .collect(Collectors.toList());
        
        return ResponseEntity.ok(response);
    }

    /**
     * Get incident by ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getIncidentById(@PathVariable String id) {
        log.info("Fetching incident with ID: {}", id);
        return incidentService.getIncidentById(id)
            .map(incident -> ResponseEntity.ok(IncidentResponse.from(incident)))
            .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Delete incident
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteIncident(@PathVariable String id) {
        log.info("Deleting incident with ID: {}", id);
        if (incidentService.getIncidentById(id).isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        incidentService.deleteIncident(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Get statistics
     */
    @GetMapping("/stats")
    public ResponseEntity<?> getStats() {
        return ResponseEntity.ok(new Object() {
            public final long totalIncidents = incidentService.getIncidentCount();
        });
    }
}