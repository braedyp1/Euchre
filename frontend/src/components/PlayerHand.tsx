import { CardView } from './CardView';

interface PlayerHandProps {
  cards: string[];
  playableCards: string[];
  busy: boolean;
  onPlay: (card: string) => void;
}

export function PlayerHand({ cards, playableCards, busy, onPlay }: PlayerHandProps) {
  return (
    <div className="playerHand" aria-label="Your hand">
      {cards.map((card) => {
        const playable = playableCards.includes(card);
        return (
          <CardView
            key={card}
            card={card}
            disabled={busy || !playable}
            onClick={() => onPlay(card)}
          />
        );
      })}
    </div>
  );
}
