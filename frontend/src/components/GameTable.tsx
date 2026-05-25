import { RotateCcw } from 'lucide-react';
import type { GameState, Suit } from '../types/game';
import { PlayerHand } from './PlayerHand';
import { Scoreboard } from './Scoreboard';
import { TrickArea } from './TrickArea';
import { TrumpSelectionModal } from './TrumpSelectionModal';
import { StatusBanner } from './StatusBanner';

interface GameTableProps {
  game: GameState;
  busy: boolean;
  error?: string | null;
  isFetching: boolean;
  alone: boolean;
  setAlone: (alone: boolean) => void;
  onPlay: (card: string) => void;
  onTrump: (suit: Suit) => void;
  onPass: () => void;
  onNewGame: () => void;
}

const seatNames = ['You', 'CPU Left', 'Partner', 'CPU Right'];

export function GameTable({
  game,
  busy,
  error,
  isFetching,
  alone,
  setAlone,
  onPlay,
  onTrump,
  onPass,
  onNewGame,
}: GameTableProps) {
  if (game.phase === 'GAME_COMPLETE') {
    return (
      <main className="completeScreen">
        <section>
          <strong>{game.winner} wins</strong>
          <span>Final score {game.scoreTeamA}-{game.scoreTeamB}</span>
          <button onClick={onNewGame} type="button">
            <RotateCcw size={16} />
            New Game
          </button>
        </section>
      </main>
    );
  }

  return (
    <main className="gameShell">
      <StatusBanner game={game} error={error} isFetching={isFetching} />
      <div className="tableLayout">
        <Scoreboard game={game} />
        <section className="tableSurface">
          {[2, 1, 3].map((seat) => (
            <div className={`seat seat${seat} ${game.currentTurn === seat ? 'activeSeat' : ''}`} key={seat}>
              <strong>{seatNames[seat]}</strong>
              <span>{game.handSizes[seat] ?? 0} cards</span>
            </div>
          ))}
          <TrickArea game={game} />
          <div className={`humanSeat ${game.currentTurn === 0 ? 'activeSeat' : ''}`}>
            <strong>You</strong>
            <PlayerHand
              busy={busy}
              cards={game.playerHand}
              playableCards={game.legalActions.playableCards}
              onPlay={onPlay}
            />
          </div>
        </section>
      </div>
      <TrumpSelectionModal
        alone={alone}
        busy={busy}
        game={game}
        onPass={onPass}
        onSelect={onTrump}
        setAlone={setAlone}
      />
    </main>
  );
}
