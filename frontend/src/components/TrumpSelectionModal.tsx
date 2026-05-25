import { Check, X } from 'lucide-react';
import type { GameState, Suit } from '../types/game';

interface TrumpSelectionModalProps {
  game: GameState;
  busy: boolean;
  alone: boolean;
  setAlone: (alone: boolean) => void;
  onSelect: (suit: Suit) => void;
  onPass: () => void;
}

export function TrumpSelectionModal({ game, busy, alone, setAlone, onSelect, onPass }: TrumpSelectionModalProps) {
  if (game.currentTurn !== 0 || game.legalActions.trumpSuits.length === 0) {
    return null;
  }

  return (
    <div className="modalBackdrop">
      <section className="trumpModal" aria-label="Trump selection">
        <header>
          <strong>Choose Trump</strong>
          <span>{game.phase === 'FIRST_TRUMP_ROUND' ? `Upcard ${game.upcard}` : 'Second round'}</span>
        </header>
        <div className="suitGrid">
          {game.legalActions.trumpSuits.map((suit) => (
            <button disabled={busy} key={suit} onClick={() => onSelect(suit)} type="button">
              <Check size={16} />
              {suit}
            </button>
          ))}
        </div>
        <label className="aloneToggle">
          <input
            checked={alone}
            disabled={!game.legalActions.canGoAlone || busy}
            onChange={(event) => setAlone(event.target.checked)}
            type="checkbox"
          />
          Go alone
        </label>
        <button className="passButton" disabled={!game.legalActions.canPass || busy} onClick={onPass} type="button">
          <X size={16} />
          Pass
        </button>
      </section>
    </div>
  );
}
