import { useEffect, useMemo, useState } from 'react';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { LogIn, Play } from 'lucide-react';
import {
  getGame,
  getLatestGame,
  login,
  passTrump,
  playCard,
  selectTrump,
  setSessionToken,
  startNewGame,
} from './api/client';
import { GameTable } from './components/GameTable';
import type { GameState, LoginResponse, Suit } from './types/game';

const TOKEN_KEY = 'euchre.sessionToken';
const USERNAME_KEY = 'euchre.username';
const GAME_ID_KEY = 'euchre.gameId';

export default function App() {
  const queryClient = useQueryClient();
  const [session, setSession] = useState<LoginResponse | null>(() => {
    const sessionToken = localStorage.getItem(TOKEN_KEY);
    const username = localStorage.getItem(USERNAME_KEY);
    return sessionToken && username ? { sessionToken, username, playerId: 0 } : null;
  });
  const [username, setUsername] = useState(() => localStorage.getItem(USERNAME_KEY) ?? '');
  const [gameId, setGameId] = useState<number | null>(() => {
    const stored = localStorage.getItem(GAME_ID_KEY);
    return stored ? Number(stored) : null;
  });
  const [alone, setAlone] = useState(false);
  const [bannerError, setBannerError] = useState<string | null>(null);

  useEffect(() => {
    setSessionToken(session?.sessionToken ?? null);
  }, [session]);

  const gameQuery = useQuery({
    queryKey: ['game', gameId],
    queryFn: () => getGame(gameId!),
    enabled: Boolean(session && gameId),
    refetchInterval: (query) => {
      const game = query.state.data as GameState | undefined;
      return game?.gameStatus === 'ACTIVE' ? 1000 : 3000;
    },
  });

  const currentGame = gameQuery.data;

  const setGame = (game: GameState) => {
    setGameId(game.id);
    localStorage.setItem(GAME_ID_KEY, String(game.id));
    queryClient.setQueryData(['game', game.id], game);
  };

  const loginMutation = useMutation({
    mutationFn: login,
    onSuccess: async (response) => {
      setSession(response);
      localStorage.setItem(TOKEN_KEY, response.sessionToken);
      localStorage.setItem(USERNAME_KEY, response.username);
      setSessionToken(response.sessionToken);
      try {
        const latest = await getLatestGame();
        setGame(latest);
      } catch {
        setGameId(null);
        localStorage.removeItem(GAME_ID_KEY);
      }
    },
    onError: () => setBannerError('Login failed. Check the backend connection.'),
  });

  const newGameMutation = useMutation({
    mutationFn: startNewGame,
    onSuccess: setGame,
    onError: () => setBannerError('Unable to start a new game.'),
  });

  const playMutation = useMutation({
    mutationFn: ({ id, card }: { id: number; card: string }) => playCard(id, card),
    onSuccess: setGame,
    onError: () => setBannerError('That move was rejected by the server.'),
  });

  const trumpMutation = useMutation({
    mutationFn: ({ id, suit, aloneChoice }: { id: number; suit: Suit; aloneChoice: boolean }) =>
      selectTrump(id, suit, aloneChoice),
    onSuccess: (game) => {
      setAlone(false);
      setGame(game);
    },
    onError: () => setBannerError('Trump selection was rejected by the server.'),
  });

  const passMutation = useMutation({
    mutationFn: passTrump,
    onSuccess: setGame,
    onError: () => setBannerError('Pass was rejected by the server.'),
  });

  const busy = useMemo(
    () => loginMutation.isPending || newGameMutation.isPending || playMutation.isPending || trumpMutation.isPending || passMutation.isPending,
    [loginMutation.isPending, newGameMutation.isPending, playMutation.isPending, trumpMutation.isPending, passMutation.isPending],
  );

  if (!session) {
    return (
      <main className="loginScreen">
        <form
          onSubmit={(event) => {
            event.preventDefault();
            setBannerError(null);
            loginMutation.mutate(username);
          }}
        >
          <h1>Euchre</h1>
          <label>
            Username
            <input
              autoComplete="username"
              maxLength={20}
              minLength={3}
              onChange={(event) => setUsername(event.target.value)}
              pattern="[A-Za-z0-9_]+"
              required
              value={username}
            />
          </label>
          <button disabled={busy} type="submit">
            <LogIn size={16} />
            Login
          </button>
          {bannerError && <p className="formError">{bannerError}</p>}
        </form>
      </main>
    );
  }

  if (!currentGame) {
    return (
      <main className="loginScreen">
        <section className="startPanel">
          <h1>Euchre</h1>
          <p>{gameQuery.isFetching ? 'Looking for an unfinished game...' : 'No unfinished game is loaded.'}</p>
          <button disabled={busy} onClick={() => newGameMutation.mutate()} type="button">
            <Play size={16} />
            New Game
          </button>
          {bannerError && <p className="formError">{bannerError}</p>}
        </section>
      </main>
    );
  }

  return (
    <GameTable
      alone={alone}
      busy={busy}
      error={bannerError}
      game={currentGame}
      isFetching={gameQuery.isFetching}
      onNewGame={() => newGameMutation.mutate()}
      onPass={() => passMutation.mutate(currentGame.id)}
      onPlay={(card) => playMutation.mutate({ id: currentGame.id, card })}
      onTrump={(suit) => trumpMutation.mutate({ id: currentGame.id, suit, aloneChoice: alone })}
      setAlone={setAlone}
    />
  );
}
