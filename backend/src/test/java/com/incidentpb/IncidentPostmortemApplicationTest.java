package com.incidentpb;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@TestPropertySource(properties = {
    "spring.data.mongodb.uri=mongodb://localhost:27017/test-db"
})
class IncidentKnowledgeApplicationTests {

    @Test
    void contextLoads() {
        // Verifies that Spring context loads successfully
    }
}