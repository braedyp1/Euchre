import type { GameState } from '../types/game';
import { CardView } from './CardView';

interface TrickAreaProps {
  game: GameState;
}

const seatLabels = ['You', 'Left', 'Partner', 'Right'];

export function TrickArea({ game }: TrickAreaProps) {
  return (
    <div className="trickArea">
      {game.currentTrick.length === 0 ? (
        <div className="emptyTrick">Trick {game.currentTrickNumber}</div>
      ) : (
        game.currentTrick.map((played) => (
          <div className="playedCard" key={`${played.seatPosition}-${played.card}`}>
            <span>{seatLabels[played.seatPosition]}</span>
            <CardView card={played.card} disabled />
          </div>
        ))
      )}
      {game.currentWinningSeat !== null && (
        <div className="winningSeat">Winning: {seatLabels[game.currentWinningSeat]}</div>
      )}
    </div>
  );
}
