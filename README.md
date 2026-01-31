# Incident Knowledge System

> AI-powered incident knowledge base for engineering teams

## Problem Statement

Engineering teams repeatedly encounter similar incidents but lack an efficient way to search and learn from past resolutions. Traditional wiki-based approaches fail because:

- Manual documentation is inconsistent
- Search relies on exact keyword matching
- Context and relationships between incidents are lost

## Solution

An intelligent system that:

1. **Ingests** incident data (logs, postmortems, tickets)
2. **Summarizes** using AI (root cause, impact, resolution)
3. **Enables semantic search** to find relevant past incidents
4. **Surfaces patterns** through tagging and categorization


## Tech Stack

- **Backend**: Spring Boot 3.x, Java 17
- **Database**: MongoDB (documents + vector embeddings)
- **AI**: OpenAI API (text-embedding-ada-002, GPT-4)
- **Frontend**: React 18, TypeScript
- **Deployment**: Docker, AWS/Azure


## Getting Started

### Prerequisites

- Java 17+
- Node 18+
- MongoDB 6+
- Docker (optional)

### Quick Start

```bash
# Backend
cd backend
./mvnw spring-boot:run

# Frontend
cd frontend
npm install
npm start
```

### Contributing
### This is a portfolio/learning project. Issues and suggestions welcome!