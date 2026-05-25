interface CardViewProps {
  card: string;
  disabled?: boolean;
  selected?: boolean;
  onClick?: () => void;
}

const suitSymbols: Record<string, string> = {
  C: '♣',
  D: '♦',
  H: '♥',
  S: '♠',
};

export function CardView({ card, disabled = false, selected = false, onClick }: CardViewProps) {
  const rank = card.slice(0, -1);
  const suit = card.slice(-1);
  const red = suit === 'H' || suit === 'D';

  return (
    <button
      className={`card ${red ? 'cardRed' : 'cardBlack'} ${selected ? 'cardSelected' : ''}`}
      disabled={disabled}
      onClick={onClick}
      type="button"
      aria-label={`Play ${card}`}
    >
      <span>{rank}</span>
      <strong>{suitSymbols[suit] ?? suit}</strong>
    </button>
  );
}
