import { BadgeCheck, Trophy } from 'lucide-react';
import type { GameState } from '../types/game';

interface ScoreboardProps {
  game: GameState;
}

export function Scoreboard({ game }: ScoreboardProps) {
  return (
    <aside className="scoreboard">
      <div className="scoreRow">
        <span>Your Team</span>
        <strong>{game.scoreTeamA}</strong>
      </div>
      <div className="scoreRow">
        <span>CPU Team</span>
        <strong>{game.scoreTeamB}</strong>
      </div>
      <div className="scoreMeta">
        <span><Trophy size={16} /> Tricks {game.tricksTeamA}-{game.tricksTeamB}</span>
        <span><BadgeCheck size={16} /> Dealer Seat {game.dealerPosition}</span>
      </div>
      <div className="trumpBadge">
        <span>Trump</span>
        <strong>{game.trumpSuit ?? game.upcard ?? 'Pending'}</strong>
      </div>
    </aside>
  );
}
