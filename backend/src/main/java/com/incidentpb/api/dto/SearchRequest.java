package com.incidentpb.api.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * Request for semantic search
 */
@Data
public class SearchRequest {

    @NotBlank(message = "Query is required")
    private String query;

    @Min(value = 1, message = "Limit must be at least 1")
    @Max(value = 50, message = "Limit cannot exceed 50")
    private int limit = 10;
}