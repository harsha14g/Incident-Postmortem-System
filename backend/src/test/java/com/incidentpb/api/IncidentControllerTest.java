package com.incidentpb.api;

import java.util.Set;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.incidentpb.api.dto.CreateIncidentRequest;
import com.incidentpb.domain.Severity;

/**
 * Integration tests for Incident API
 */
@SpringBootTest
@AutoConfigureMockMvc
class IncidentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void shouldCreateIncident() throws Exception {
        CreateIncidentRequest request = new CreateIncidentRequest();
        request.setTitle("Test Incident");
        request.setRawContent("This is a test incident");
        request.setSeverity(Severity.MEDIUM);
        request.setServices(Set.of("test-service"));
        mockMvc.perform(post("/api/incidents")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").exists())
            .andExpect(jsonPath("$.title").value("Test Incident"))
            .andExpect(jsonPath("$.severity").value("MEDIUM"));
    }

    @Test
    void shouldRejectInvalidIncident() throws Exception {
        CreateIncidentRequest request = new CreateIncidentRequest();
        mockMvc.perform(post("/api/incidents")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error").value("Validation Failed"));
    }

    @Test
    void shouldGetAllIncidents() throws Exception {
        mockMvc.perform(get("/api/incidents"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").isArray());
    }

    @Test
    void shouldGetStats() throws Exception {
        mockMvc.perform(get("/api/incidents/stats"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.totalIncidents").exists());
    }
}