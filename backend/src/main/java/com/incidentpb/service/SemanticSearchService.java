package com.incidentpb.service;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.incidentpb.domain.Incident;
import com.incidentpb.repository.IncidentRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Service for semantic search using vector embeddings
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SemanticSearchService {

    private final IncidentRepository incidentRepository;
    private final GeminiAIService geminiAIService;

    /**
     * Search for incidents similar to the query text
     * 
     * @param query The search query
     * @param limit Maximum number of results to return
     * @return List of incidents sorted by similarity
     */
    public List<IncidentSearchResult> searchSimilarIncidents(String query, int limit) {
        log.info("Performing semantic search for: {}", query);
        List<Float> queryEmbedding = geminiAIService.generateEmbedding(query);
        if (queryEmbedding.isEmpty()) {
            log.warn("Failed to generate embedding for query, falling back to text search");
            return fallbackTextSearch(query, limit);
        }
        List<Incident> incidents = incidentRepository.findAllWithEmbeddings();
        log.debug("Found {} incidents with embeddings", incidents.size());
        List<IncidentSearchResult> results = incidents.stream()
                .map(incident -> {
                    double similarity = cosineSimilarity(queryEmbedding, incident.getEmbedding());
                    return new IncidentSearchResult(incident, similarity);
                })
                .filter(result -> result.getSimilarity() > 0.5) // Only return reasonably similar results
                .sorted(Comparator.comparingDouble(IncidentSearchResult::getSimilarity).reversed())
                .limit(limit)
                .collect(Collectors.toList());
        log.info("Found {} similar incidents", results.size());
        return results;
    }

    /**
     * Calculate cosine similarity between two vectors
     * Formula: cos(θ) = (A · B) / (||A|| * ||B||)
     */
    private double cosineSimilarity(List<Float> vectorA, List<Float> vectorB) {
        if (vectorA.size() != vectorB.size()) {
            log.warn("Vector size mismatch: {} vs {}", vectorA.size(), vectorB.size());
            return 0.0;
        }
        double dotProduct = 0.0;
        double normA = 0.0;
        double normB = 0.0;
        for (int i = 0; i < vectorA.size(); i++) {
            dotProduct += vectorA.get(i) * vectorB.get(i);
            normA += vectorA.get(i) * vectorA.get(i);
            normB += vectorB.get(i) * vectorB.get(i);
        }
        if (normA == 0.0 || normB == 0.0) {
            return 0.0;
        }
        return dotProduct / (Math.sqrt(normA) * Math.sqrt(normB));
    }

    /**
     * Fallback to simple text search if embeddings fail
     */
    private List<IncidentSearchResult> fallbackTextSearch(String query, int limit) {
        List<Incident> incidents = incidentRepository.searchByTitle(query);
        return incidents.stream()
            .limit(limit)
            .map(incident -> new IncidentSearchResult(incident, 0.0))
            .collect(Collectors.toList());
    }

    /**
     * Search result with similarity score
     */
    public static class IncidentSearchResult {
        private final Incident incident;
        private final double similarity;

        public IncidentSearchResult(Incident incident, double similarity) {
            this.incident = incident;
            this.similarity = similarity;
        }
        public Incident getIncident() {
            return incident;
        }
        public double getSimilarity() {
            return similarity;
        }
    }
}