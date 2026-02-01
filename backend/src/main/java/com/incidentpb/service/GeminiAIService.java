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
 * Updated to use Gemini 2.0 Flash and Header-based Auth
 */
@Service
@Slf4j
public class GeminiAIService {

    @Value("${gemini.api-key}")
    private String apiKey;

    @Value("${gemini.model:gemini-2.0-flash}")
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

    public IncidentSummary generateSummary(String title, String content) {
        log.info("Generating AI summary with Gemini ({}) for: {}", model, title);
        try {
            String prompt = buildSummarizationPrompt(title, content);
            String response = callGemini(prompt);
            return parseSummaryResponse(response);
        } catch (Exception e) {
            log.error("Error generating summary with Gemini", e);
            return createFallbackSummary();
        }
    }

    public List<Float> generateEmbedding(String text) {
        try {
            return callGeminiEmbedding(text);
        } catch (Exception e) {
            log.error("Error generating embedding", e);
            return new ArrayList<>();
        }
    }

    private String buildSummarizationPrompt(String title, String content) {
        return String.format("""
            You are an expert SRE. Extract structured information from this incident.
            
            Title: %s
            Details: %s
            
            Format response with these exact headers:
            ## SHORT_SUMMARY
            ## ROOT_CAUSE
            ## IMPACT
            ## RESOLUTION
            ## PREVENTION
            """, title, content);
    }

    private String callGemini(String prompt) throws IOException {
        String url = GEMINI_API_URL + model + ":generateContent";

        JsonObject requestBody = new JsonObject();
        JsonArray contents = new JsonArray();
        JsonObject contentObj = new JsonObject();
        JsonArray parts = new JsonArray();
        JsonObject part = new JsonObject();

        part.addProperty("text", prompt);
        parts.add(part);
        contentObj.add("parts", parts);
        contents.add(contentObj);
        requestBody.add("contents", contents);

        RequestBody body = RequestBody.create(gson.toJson(requestBody), JSON);
        Request request = new Request.Builder()
                .url(url)
                .addHeader("x-goog-api-key", apiKey)
                .post(body)
                .build();

        try (Response response = httpClient.newCall(request).execute()) {
            String responseBody = response.body() != null ? response.body().string() : "";
            
            if (!response.isSuccessful()) {
                log.error("Gemini API Error: {} - Response: {}", response.code(), responseBody);
                throw new IOException("Gemini API error: " + response.code());
            }

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

    private List<Float> callGeminiEmbedding(String text) throws IOException {
        String url = GEMINI_API_URL + "text-embedding-004:embedContent";

        JsonObject requestBody = new JsonObject();
        JsonObject contentObj = new JsonObject();
        JsonArray parts = new JsonArray();
        JsonObject part = new JsonObject();
        
        part.addProperty("text", text.substring(0, Math.min(text.length(), 2048)));
        parts.add(part);
        contentObj.add("parts", parts);
        requestBody.add("content", contentObj);

        RequestBody body = RequestBody.create(gson.toJson(requestBody), JSON);
        Request request = new Request.Builder()
                .url(url)
                .addHeader("x-goog-api-key", apiKey)
                .post(body)
                .build();

        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
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

    private IncidentSummary parseSummaryResponse(String response) {
        return IncidentSummary.builder()
            .shortSummary(extractSection(response, "SHORT_SUMMARY"))
            .rootCause(extractSection(response, "ROOT_CAUSE"))
            .impact(extractSection(response, "IMPACT"))
            .resolution(extractSection(response, "RESOLUTION"))
            .prevention(extractSection(response, "PREVENTION"))
            .build();
    }

    private String extractSection(String response, String sectionName) {
        String marker = "## " + sectionName;
        int start = response.indexOf(marker);
        if (start == -1) return "Not available";
        
        start += marker.length();
        int end = response.indexOf("## ", start);
        if (end == -1) end = response.length();
        
        return response.substring(start, end).trim();
    }

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