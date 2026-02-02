import { useState } from 'react';
import { QueryClient, QueryClientProvider, useQuery } from '@tanstack/react-query';
import { SearchBar } from './components/SearchBar';
import { SearchResults } from './components/SearchResults';
import { searchIncidents } from './services/api';
import { SearchResponse } from './types/incident';
import './App.css';

const queryClient = new QueryClient({
  defaultOptions: {
    queries: {
      retry: 1,
      refetchOnWindowFocus: false,
    },
  },
});

function SearchPage() {
  const [searchQuery, setSearchQuery] = useState<string>('');
  const [activeQuery, setActiveQuery] = useState<string>('');

  const { data, isLoading, error } = useQuery<SearchResponse>({
    queryKey: ['search', activeQuery],
    queryFn: () => searchIncidents(activeQuery),
    enabled: activeQuery.length > 0,
  });

  const handleSearch = (query: string) => {
    setSearchQuery(query);
    setActiveQuery(query);
  };

  return (
    <div className="app">

      <header className="header">
        <div className="header-content">
          <h1 className="logo">🔍 Incident Knowledge System</h1>
        </div>
      </header>

      <div className="hero">
        <div className="hero-content">
          <h2 className="hero-title">Find Similar Incidents</h2>
          <p className="hero-subtitle">
            Zero-Friction Incident Analysis: Semantic Search for Production Postmortems
          </p>
          <SearchBar onSearch={handleSearch} isLoading={isLoading} />
        </div>
      </div>

      <main className="main-content">
        {error && (
          <div className="error-message">
            <strong>Error:</strong> {error instanceof Error ? error.message : 'Failed to search'}
          </div>
        )}

        {isLoading && (
          <div className="loading">
            <div className="spinner"></div>
            <p>Searching incidents...</p>
          </div>
        )}

        {!isLoading && <SearchResults results={data || null} />}
      </main>

      <footer className="footer">
        <p>Incident Knowledge System - Developed by Harsha Gupta</p>
      </footer>
    </div>
  );
}

function App() {
  return (
    <QueryClientProvider client={queryClient}>
      <SearchPage />
    </QueryClientProvider>
  );
}

export default App;