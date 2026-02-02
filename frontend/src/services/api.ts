import axios from 'axios';
import { SearchResponse } from '../types/incident';

const api = axios.create({
  baseURL: '/api',
  headers: {
    'Content-Type': 'application/json',
  },
});

export const searchIncidents = async (query: string): Promise<SearchResponse> => {
  const response = await api.get<SearchResponse>('/incidents/search', {
    params: { q: query, limit: 10 },
  });
  return response.data;
};