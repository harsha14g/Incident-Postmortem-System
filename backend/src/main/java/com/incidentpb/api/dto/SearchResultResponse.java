package com.incidentpb.api.dto;

import com.incidentpb.service.SemanticSearchService;
import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.stream.Collectors;

@Data
@Builder
public class SearchResultResponse {

    private String query;
    private int totalResults;
    private List<SearchResult> results;

    @Data
    @Builder
    public static class SearchResult {
        private IncidentResponse incident;
        private double similarityScore;
        private String matchReason;
    }

    public static SearchResultResponse from(String query, List<SemanticSearchService.IncidentSearchResult> results) {
        List<SearchResult> searchResults = results.stream()
            .map(result -> SearchResult.builder()
                .incident(IncidentResponse.from(result.getIncident()))
                .similarityScore(Math.round(result.getSimilarity() * 100.0) / 100.0) // Round to 2 decimals
                .matchReason(generateMatchReason(result.getSimilarity()))
                .build())
            .collect(Collectors.toList());

        return SearchResultResponse.builder()
            .query(query)
            .totalResults(searchResults.size())
            .results(searchResults)
            .build();
    }

    private static String generateMatchReason(double similarity) {
        if (similarity > 0.9) {
            return "Highly similar incident";
        } else if (similarity > 0.7) {
            return "Very similar incident";
        } else if (similarity > 0.5) {
            return "Somewhat similar incident";
        } else {
            return "Related incident";
        }
    }
}