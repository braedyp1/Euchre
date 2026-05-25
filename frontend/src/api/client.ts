import axios from 'axios';
import type { GameState, LoginResponse, Suit } from '../types/game';

const api = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL ?? 'http://localhost:8080',
});

export function setSessionToken(token: string | null) {
  if (token) {
    api.defaults.headers.common['X-Session-Token'] = token;
  } else {
    delete api.defaults.headers.common['X-Session-Token'];
  }
}

export async function login(username: string): Promise<LoginResponse> {
  const response = await api.post<LoginResponse>('/api/player/login', { username });
  return response.data;
}

export async function startNewGame(): Promise<GameState> {
  const response = await api.post<GameState>('/api/game/new');
  return response.data;
}

export async function getLatestGame(): Promise<GameState> {
  const response = await api.get<GameState>('/api/game/latest');
  return response.data;
}

export async function getGame(gameId: number): Promise<GameState> {
  const response = await api.get<GameState>(`/api/game/${gameId}`);
  return response.data;
}

export async function playCard(gameId: number, card: string): Promise<GameState> {
  const response = await api.post<GameState>(`/api/game/${gameId}/play`, { card });
  return response.data;
}

export async function selectTrump(gameId: number, suit: Suit, alone: boolean): Promise<GameState> {
  const response = await api.post<GameState>(`/api/game/${gameId}/trump`, { suit, alone });
  return response.data;
}

export async function passTrump(gameId: number): Promise<GameState> {
  const response = await api.post<GameState>(`/api/game/${gameId}/pass`);
  return response.data;
}

export async function abandonGame(gameId: number): Promise<void> {
  await api.delete(`/api/game/${gameId}`);
}
