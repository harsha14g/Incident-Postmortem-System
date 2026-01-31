package com.incidentpb.repository;

import com.incidentpb.domain.Incident;
import com.incidentpb.domain.Severity;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

/**
 * Repository for incident data access.
 * 
 * Spring Data MongoDB automatically implements basic CRUD operations.
 * We only define custom queries here.
 */
@Repository
public interface IncidentRepository extends MongoRepository<Incident, String> {

    /**
     * Find incidents by severity
     */
    List<Incident> findBySeverity(Severity severity);

    /**
     * Find incidents affecting a specific service
     */
    List<Incident> findByServicesContaining(String service);

    /**
     * Find incidents by severity and service
     */
    List<Incident> findBySeverityAndServicesContaining(Severity severity, String service);

    /**
     * Find recent incidents within a time range
     */
    List<Incident> findByOccurredAtBetween(Instant start, Instant end);

    /**
     * Find incidents by title containing text (case-insensitive)
     */
    @Query("{ 'title': { $regex: ?0, $options: 'i' } }")
    List<Incident> searchByTitle(String keyword);
}