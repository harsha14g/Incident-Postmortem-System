import React from 'react';
import { SearchResponse } from '../types/incident';
import { formatDistanceToNow } from 'date-fns';
import './SearchResults.css';

interface SearchResultsProps {
  results: SearchResponse | null;
}

const getSeverityColor = (severity: string): string => {
  const colors: Record<string, string> = {
    CRITICAL: '#dc2626',
    HIGH: '#ea580c',
    MEDIUM: '#ca8a04',
    LOW: '#16a34a',
  };
  return colors[severity] || '#6b7280';
};

export const SearchResults: React.FC<SearchResultsProps> = ({ results }) => {
  if (!results) {
    return (
      <div className="empty-state">
        <div className="empty-icon">🔎</div>
        <h3>Search for incidents</h3>
        <p>Try: "database timeout", "Redis latency", "API errors"</p>
      </div>
    );
  }

  if (results.totalResults === 0) {
    return (
      <div className="empty-state">
        <div className="empty-icon">📭</div>
        <h3>No incidents found</h3>
        <p>Try a different search query</p>
      </div>
    );
  }

  return (
    <div className="search-results">
      <div className="results-header">
        <h2>Found {results.totalResults} similar incidents</h2>
        <p className="results-query">Search: "{results.query}"</p>
      </div>

      <div className="results-list">
        {results.results.map((result) => (
          <div key={result.incident.id} className="result-card">
            <div className="result-header">
              <h3 className="result-title">{result.incident.title}</h3>
              <span
                className="severity-badge"
                style={{ backgroundColor: getSeverityColor(result.incident.severity) }}
              >
                {result.incident.severity}
              </span>
            </div>

            <div className="result-meta">
              <span className="similarity-score">
                {Math.round(result.similarityScore * 100)}% match
              </span>
              <span className="match-reason">{result.matchReason}</span>
              <span className="result-time">
                {formatDistanceToNow(new Date(result.incident.occurredAt), { addSuffix: true })}
              </span>
            </div>

            {result.incident.summary?.shortSummary && (
              <p className="result-summary">{result.incident.summary.shortSummary}</p>
            )}

            {result.incident.services && result.incident.services.length > 0 && (
              <div className="result-tags">
                {result.incident.services.map((service) => (
                  <span key={service} className="tag">
                    {service}
                  </span>
                ))}
              </div>
            )}

            {result.incident.summary && (
              <details className="result-details">
                <summary>View Full Details</summary>
                <div className="details-content">
                  <div className="detail-section">
                    <strong>Root Cause:</strong>
                    <p>{result.incident.summary.rootCause}</p>
                  </div>
                  <div className="detail-section">
                    <strong>Impact:</strong>
                    <p>{result.incident.summary.impact}</p>
                  </div>
                  <div className="detail-section">
                    <strong>Resolution:</strong>
                    <p>{result.incident.summary.resolution}</p>
                  </div>
                  <div className="detail-section">
                    <strong>Prevention:</strong>
                    <p>{result.incident.summary.prevention}</p>
                  </div>
                </div>
              </details>
            )}
          </div>
        ))}
      </div>
    </div>
  );
};