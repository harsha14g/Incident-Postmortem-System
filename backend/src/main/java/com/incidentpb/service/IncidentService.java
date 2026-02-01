package com.incidentpb.service;

import com.incidentpb.domain.Incident;
import com.incidentpb.domain.IncidentMetadata;
import com.incidentpb.domain.IncidentSummary;
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

@Service
@RequiredArgsConstructor
@Slf4j
public class IncidentService {

    private final IncidentRepository incidentRepository;
    private final GeminiAIService geminiAIService;

    @Transactional
    public Incident createIncident(CreateIncidentRequest request) {
        log.info("Creating new incident: {}", request.getTitle());
        log.info("About to call Gemini AI service...");
        IncidentSummary summary = geminiAIService.generateSummary(
            request.getTitle(), 
            request.getRawContent()
        );
        log.info("AI summary generated: {}", summary != null ? "SUCCESS" : "FAILED");
        String embeddingText = request.getTitle() + "\n" + request.getRawContent();
        log.info("About to generate embedding...");
        List<Float> embedding = geminiAIService.generateEmbedding(embeddingText);
        log.info("Embedding generated with {} dimensions", embedding.size());
        Incident incident = Incident.builder()
                .title(request.getTitle())
                .rawContent(request.getRawContent())
                .summary(summary)
                .embedding(embedding)
                .severity(request.getSeverity())
                .services(request.getServices())
                .occurredAt(request.getOccurredAt() != null ? request.getOccurredAt() : Instant.now())
                .source(request.getSource() != null ? request.getSource() : "Manual Upload")
                .metadata(buildMetadata(request))
                .build();

        Incident saved = incidentRepository.save(incident);
        log.info("Incident saved with ID: {}, has summary: {}, embedding size: {}", 
            saved.getId(), 
            saved.getSummary() != null,
            saved.getEmbedding() != null ? saved.getEmbedding().size() : 0);
        
        return saved;
    }

    public Optional<Incident> getIncidentById(String id) {
        return incidentRepository.findById(id);
    }

    public List<Incident> getAllIncidents() {
        return incidentRepository.findAll();
    }

    public List<Incident> getIncidentsBySeverity(Severity severity) {
        return incidentRepository.findBySeverity(severity);
    }

    public List<Incident> getIncidentsByService(String service) {
        return incidentRepository.findByServicesContaining(service);
    }

    @Transactional
    public void deleteIncident(String id) {
        log.info("Deleting incident: {}", id);
        incidentRepository.deleteById(id);
    }

    public long getIncidentCount() {
        return incidentRepository.count();
    }

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