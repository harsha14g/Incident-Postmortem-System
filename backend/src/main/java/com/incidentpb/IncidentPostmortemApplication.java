package com.incidentpb;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.mongodb.config.EnableMongoAuditing;

/**
 * Main entry point for the Incident Knowledge System
 */
@SpringBootApplication
@EnableMongoAuditing
public class IncidentPostmortemApplication {

    public static void main(String[] args) {
        SpringApplication.run(IncidentPostmortemApplication.class, args);
    }
}