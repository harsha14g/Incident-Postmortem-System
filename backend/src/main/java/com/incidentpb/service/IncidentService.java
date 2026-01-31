package com.incidentpb.service;

import com.incidentpb.domain.Incident;
import com.incidentpb.domain.Severity;
import com.incidentpb.repository.IncidentRepository;
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
     * Save a new incident
     */
    @Transactional
    public Incident createIncident(Incident incident) {
        log.info("Creating new incident: {}", incident.getTitle());
        
        // Set timestamps (will be auto-set by @CreatedDate, but being explicit)
        Instant now = Instant.now();
        incident.setCreatedAt(now);
        incident.setUpdatedAt(now);
        
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
}