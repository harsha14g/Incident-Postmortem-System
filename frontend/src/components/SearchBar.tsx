import React, { useState } from 'react';
import './SearchBar.css';

interface SearchBarProps {
  onSearch: (query: string) => void;
  isLoading: boolean;
}

export const SearchBar: React.FC<SearchBarProps> = ({ onSearch, isLoading }) => {
  const [query, setQuery] = useState('');

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    if (query.trim()) {
      onSearch(query);
    }
  };

  return (
    <form onSubmit={handleSubmit} className="search-bar">
      <input
        type="text"
        value={query}
        onChange={(e) => setQuery(e.target.value)}
        placeholder='Search incidents: "Redis timeout", "database slow", etc.'
        className="search-input"
        disabled={isLoading}
      />
      <button type="submit" className="search-button" disabled={isLoading || !query.trim()}>
        {isLoading ? '⏳ Searching...' : 'Search'}
      </button>
    </form>
  );
};