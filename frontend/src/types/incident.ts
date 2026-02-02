export enum Severity {
    CRITICAL = 'CRITICAL',
    HIGH = 'HIGH',
    MEDIUM = 'MEDIUM',
    LOW = 'LOW',
  }
  
  export interface IncidentSummary {
    shortSummary: string;
    rootCause: string;
    impact: string;
    resolution: string;
    prevention: string;
  }
  
  export interface Incident {
    id: string;
    title: string;
    rawContent: string;
    summary: IncidentSummary | null;
    services: string[] | null;
    severity: Severity;
    occurredAt: string;
    createdAt: string;
    source: string;
    team: string | null;
    environment: string | null;
  }
  
  export interface SearchResult {
    incident: Incident;
    similarityScore: number;
    matchReason: string;
  }
  
  export interface SearchResponse {
    query: string;
    totalResults: number;
    results: SearchResult[];
  }