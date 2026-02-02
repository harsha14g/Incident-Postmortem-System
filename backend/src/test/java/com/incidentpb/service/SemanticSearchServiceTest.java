package com.incidentpb.service;

import com.incidentpb.domain.Incident;
import com.incidentpb.domain.Severity;
import com.incidentpb.repository.IncidentRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.Instant;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test semantic search functionality
 */
@SpringBootTest
class SemanticSearchServiceTest {

    @Autowired
    private SemanticSearchService searchService;

    @Autowired
    private IncidentService incidentService;

    @Autowired
    private IncidentRepository incidentRepository;

    @AfterEach
    void cleanup() {
        incidentRepository.deleteAll();
    }

    @Test
    void shouldFindSimilarIncidents() {
        createTestIncident("Redis connection timeout", "Redis became unresponsive");
        createTestIncident("Cache service slow", "Memory cache had high latency");
        createTestIncident("Database query timeout", "PostgreSQL queries were slow");
        try { Thread.sleep(2000); } catch (InterruptedException e) {}
        List<SemanticSearchService.IncidentSearchResult> results = 
            searchService.searchSimilarIncidents("Redis performance problem", 5);
        assertFalse(results.isEmpty());
        assertTrue(results.size() >= 2);
        assertTrue(results.get(0).getSimilarity() > 0.5);
    }

    private void createTestIncident(String title, String content) {
        Incident incident = Incident.builder()
                .title(title)
                .rawContent(content)
                .severity(Severity.HIGH)
                .services(Set.of("test-service"))
                .occurredAt(Instant.now())
                .source("Test")
                .build();
        incidentRepository.save(incident);
    }
}