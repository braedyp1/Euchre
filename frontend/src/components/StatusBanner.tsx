import { WifiOff } from 'lucide-react';
import type { GameState } from '../types/game';

interface StatusBannerProps {
  game: GameState;
  error?: string | null;
  isFetching: boolean;
}

const seatNames = ['You', 'CPU Left', 'Partner', 'CPU Right'];

export function StatusBanner({ game, error, isFetching }: StatusBannerProps) {
  const turnText = game.phase === 'GAME_COMPLETE'
    ? `${game.winner} wins`
    : `${seatNames[game.currentTurn]} to act`;

  return (
    <div className="statusBanner" role="status">
      <div>
        <strong>{turnText}</strong>
        <span>{game.phase.replaceAll('_', ' ')}</span>
      </div>
      {error ? (
        <div className="networkError">
          <WifiOff size={16} />
          {error}
        </div>
      ) : (
        <span className="syncState">{isFetching ? 'Syncing' : 'Synced'}</span>
      )}
    </div>
  );
}
