package com.incidentpb.repository;

import com.incidentpb.domain.Incident;
import com.incidentpb.domain.Severity;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;

import java.time.Instant;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@DataMongoTest
class IncidentRepositoryTest {

    @Autowired
    private IncidentRepository incidentRepository;

    @AfterEach
    void cleanup() {
        incidentRepository.deleteAll();
    }

    @Test
    void shouldSaveAndRetrieveIncident() {
        // Given
        Incident incident = Incident.builder()
                .title("Database connection timeout")
                .rawContent("Production database became unresponsive at 14:30 UTC")
                .severity(Severity.CRITICAL)
                .services(Set.of("auth-service", "user-service"))
                .occurredAt(Instant.now())
                .source("PagerDuty")
                .build();

        // When
        Incident saved = incidentRepository.save(incident);

        // Then
        assertNotNull(saved.getId());
        assertEquals("Database connection timeout", saved.getTitle());
        assertEquals(Severity.CRITICAL, saved.getSeverity());
        assertTrue(saved.getServices().contains("auth-service"));
    }

    @Test
    void shouldFindIncidentsBySeverity() {
        // Given
        createTestIncident("Critical Issue 1", Severity.CRITICAL);
        createTestIncident("Critical Issue 2", Severity.CRITICAL);
        createTestIncident("Medium Issue", Severity.MEDIUM);

        // When
        List<Incident> criticalIncidents = incidentRepository.findBySeverity(Severity.CRITICAL);

        // Then
        assertEquals(2, criticalIncidents.size());
        assertTrue(criticalIncidents.stream()
                .allMatch(i -> i.getSeverity() == Severity.CRITICAL));
    }

    @Test
    void shouldFindIncidentsByService() {
        // Given
        Incident incident1 = createTestIncident("Redis issue", Severity.HIGH);
        incident1.setServices(Set.of("cache-service", "redis"));
        incidentRepository.save(incident1);

        Incident incident2 = createTestIncident("API slowdown", Severity.MEDIUM);
        incident2.setServices(Set.of("api-gateway", "redis"));
        incidentRepository.save(incident2);

        Incident incident3 = createTestIncident("Database issue", Severity.LOW);
        incident3.setServices(Set.of("postgres"));
        incidentRepository.save(incident3);

        // When
        List<Incident> redisIncidents = incidentRepository.findByServicesContaining("redis");

        // Then
        assertEquals(2, redisIncidents.size());
    }

    @Test
    void shouldSearchByTitle() {
        // Given
        createTestIncident("Redis latency spike", Severity.HIGH);
        createTestIncident("Database connection pool exhausted", Severity.CRITICAL);
        createTestIncident("Redis connection timeout", Severity.MEDIUM);

        // When
        List<Incident> results = incidentRepository.searchByTitle("redis");

        // Then
        assertEquals(2, results.size());
    }

    // Helper method
    private Incident createTestIncident(String title, Severity severity) {
        Incident incident = Incident.builder()
                .title(title)
                .rawContent("Test content for " + title)
                .severity(severity)
                .occurredAt(Instant.now())
                .source("Test")
                .build();
        return incidentRepository.save(incident);
    }
}