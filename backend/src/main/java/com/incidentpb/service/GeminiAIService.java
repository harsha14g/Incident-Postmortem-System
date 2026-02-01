package com.incidentpb.service;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.incidentpb.domain.IncidentSummary;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * AI Service using Google Gemini (FREE)
 */
@Service
@Slf4j
public class GeminiAIService {

    @Value("${gemini.api-key}")
    private String apiKey;

    @Value("${gemini.model:gemini-1.5-flash}")
    private String model;

    private final OkHttpClient httpClient;
    private final Gson gson;

    private static final String GEMINI_API_URL = "https://generativelanguage.googleapis.com/v1beta/models/";
    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");

    public GeminiAIService() {
        this.httpClient = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .build();
        this.gson = new Gson();
    }

    /**
     * Generate structured summary from incident content
     */
    public IncidentSummary generateSummary(String title, String content) {
        log.info("Generating AI summary with Gemini for incident: {}", title);
        try {
            String prompt = buildSummarizationPrompt(title, content);
            String response = callGemini(prompt);
            return parseSummaryResponse(response);
        } catch (Exception e) {
            log.error("Error generating summary with Gemini", e);
            return createFallbackSummary();
        }
    }

    /**
     * Generate vector embedding for semantic search
     */
    public List<Float> generateEmbedding(String text) {
        log.debug("Generating embedding with Gemini for text of length: {}", text.length());
        try {
            return callGeminiEmbedding(text);
        } catch (Exception e) {
            log.error("Error generating embedding", e);
            return new ArrayList<>();
        }
    }

    /**
     * Build the prompt for incident summarization
     */
    private String buildSummarizationPrompt(String title, String content) {
        return String.format("""
            You are an expert SRE analyzing a production incident. Extract structured information.
            
            Incident Title: %s
            
            Incident Details:
            %s
            
            Provide a structured summary in the following format (use ## markers):
            
            ## SHORT_SUMMARY
            One sentence summary of what happened.
            
            ## ROOT_CAUSE
            What caused the incident? Be specific and technical.
            
            ## IMPACT
            What was the business and technical impact? Who was affected?
            
            ## RESOLUTION
            How was the incident resolved? What actions were taken?
            
            ## PREVENTION
            How can we prevent this from happening again? Specific actionable steps.
            
            Keep each section concise but informative. Use technical language.
            """, title, content);
    }

    /**
     * Call Gemini API for text generation
     */
    private String callGemini(String prompt) throws IOException {
        String url = GEMINI_API_URL + model + ":generateContent?key=" + apiKey;

        JsonObject requestBody = new JsonObject();
        JsonArray contents = new JsonArray();
        JsonObject content = new JsonObject();
        JsonArray parts = new JsonArray();
        JsonObject part = new JsonObject();

        part.addProperty("text", prompt);
        parts.add(part);
        content.add("parts", parts);
        contents.add(content);
        requestBody.add("contents", contents);

        JsonObject generationConfig = new JsonObject();
        generationConfig.addProperty("temperature", 0.3);
        generationConfig.addProperty("maxOutputTokens", 1000);
        requestBody.add("generationConfig", generationConfig);

        RequestBody body = RequestBody.create(gson.toJson(requestBody), JSON);
        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .build();

        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Gemini API error: " + response.code() + " - " + response.message());
            }
            String responseBody = response.body().string();
            JsonObject jsonResponse = gson.fromJson(responseBody, JsonObject.class);
            return jsonResponse
                .getAsJsonArray("candidates").get(0)
                .getAsJsonObject()
                .getAsJsonObject("content")
                .getAsJsonArray("parts").get(0)
                .getAsJsonObject()
                .get("text").getAsString();
        }
    }

    /**
     * Call Gemini API for embeddings
     */
    private List<Float> callGeminiEmbedding(String text) throws IOException {
        String url = GEMINI_API_URL + "text-embedding-004:embedContent?key=" + apiKey;

        JsonObject requestBody = new JsonObject();
        JsonObject content = new JsonObject();
        JsonArray parts = new JsonArray();
        JsonObject part = new JsonObject();
        
        part.addProperty("text", text.substring(0, Math.min(text.length(), 2048))); // Limit length
        parts.add(part);
        content.add("parts", parts);
        requestBody.add("content", content);

        RequestBody body = RequestBody.create(gson.toJson(requestBody), JSON);
        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .build();

        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                log.warn("Gemini embedding API error: {} - {}", response.code(), response.message());
                return new ArrayList<>();
            }
            String responseBody = response.body().string();
            JsonObject jsonResponse = gson.fromJson(responseBody, JsonObject.class);
            JsonArray values = jsonResponse
                    .getAsJsonObject("embedding")
                    .getAsJsonArray("values");
            List<Float> embedding = new ArrayList<>();
            for (int i = 0; i < values.size(); i++) {
                embedding.add(values.get(i).getAsFloat());
            }
            return embedding;
        }
    }

    /**
     * Parse the structured response from Gemini
     */
    private IncidentSummary parseSummaryResponse(String response) {
        String shortSummary = extractSection(response, "SHORT_SUMMARY");
        String rootCause = extractSection(response, "ROOT_CAUSE");
        String impact = extractSection(response, "IMPACT");
        String resolution = extractSection(response, "RESOLUTION");
        String prevention = extractSection(response, "PREVENTION");

        return IncidentSummary.builder()
            .shortSummary(shortSummary)
            .rootCause(rootCause)
            .impact(impact)
            .resolution(resolution)
            .prevention(prevention)
            .build();
    }

    /**
     * Extract a section from the formatted response
     */
    private String extractSection(String response, String sectionName) {
        String marker = "## " + sectionName;
        int start = response.indexOf(marker);
        if (start == -1) {
            return "Not available";
        }
        start += marker.length();
        int end = response.indexOf("## ", start);
        if (end == -1) {
            end = response.length();
        }
        return response.substring(start, end).trim();
    }

    /**
     * Fallback summary (if AI fails)
     */
    private IncidentSummary createFallbackSummary() {
        return IncidentSummary.builder()
            .shortSummary("AI summarization unavailable")
            .rootCause("Unable to determine - AI service error")
            .impact("Unable to determine - AI service error")
            .resolution("Unable to determine - AI service error")
            .prevention("Unable to determine - AI service error")
            .build();
    }
}