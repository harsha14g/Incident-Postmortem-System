package com.incidentpb.service;

import com.incidentpb.domain.Incident;
import com.incidentpb.domain.IncidentMetadata;
import com.incidentpb.domain.Severity;
import com.incidentpb.repository.IncidentRepository;
import com.incidentpb.api.dto.CreateIncidentRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

/**
 * Business logic for incident management
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class IncidentService {

    private final IncidentRepository incidentRepository;

    /**
     * Create incident from request
     */
    @Transactional
    public Incident createIncident(CreateIncidentRequest request) {
        log.info("Creating new incident: {}", request.getTitle());
        Incident incident = Incident.builder()
            .title(request.getTitle())
            .rawContent(request.getRawContent())
            .severity(request.getSeverity())
            .services(request.getServices())
            .occurredAt(request.getOccurredAt() != null ? request.getOccurredAt() : Instant.now())
            .source(request.getSource() != null ? request.getSource() : "Manual Upload")
            .metadata(buildMetadata(request))
            .build();
        return incidentRepository.save(incident);
    }

    /**
     * Get incident by ID
     */
    public Optional<Incident> getIncidentById(String id) {
        return incidentRepository.findById(id);
    }

    /**
     * Get all incidents
     */
    public List<Incident> getAllIncidents() {
        return incidentRepository.findAll();
    }

    /**
     * Find incidents by severity
     */
    public List<Incident> getIncidentsBySeverity(Severity severity) {
        return incidentRepository.findBySeverity(severity);
    }

    /**
     * Find incidents affecting a service
     */
    public List<Incident> getIncidentsByService(String service) {
        return incidentRepository.findByServicesContaining(service);
    }

    /**
     * Delete incident by ID
     */
    @Transactional
    public void deleteIncident(String id) {
        log.info("Deleting incident: {}", id);
        incidentRepository.deleteById(id);
    }

    /**
     * Get incident count
     */
    public long getIncidentCount() {
        return incidentRepository.count();
    }

    /**
     * Build metadata from request
     */
    private IncidentMetadata buildMetadata(CreateIncidentRequest request) {
        if (request.getTeam() == null && request.getEnvironment() == null) {
            return null;
        }
        return IncidentMetadata.builder()
            .team(request.getTeam())
            .environment(request.getEnvironment())
            .build();
    }
}