export type Suit = 'CLUBS' | 'DIAMONDS' | 'HEARTS' | 'SPADES';

export type GamePhase =
  | 'WAITING_FOR_DEAL'
  | 'FIRST_TRUMP_ROUND'
  | 'SECOND_TRUMP_ROUND'
  | 'PLAYING_TRICKS'
  | 'SCORING'
  | 'GAME_COMPLETE';

export type GameStatus = 'ACTIVE' | 'COMPLETE' | 'ABANDONED' | 'CORRUPTED';

export type Team = 'TEAM_A' | 'TEAM_B';

export interface PlayedCard {
  seatPosition: number;
  card: string;
}

export interface LegalActions {
  playableCards: string[];
  trumpSuits: Suit[];
  canPass: boolean;
  canGoAlone: boolean;
}

export interface GameState {
  id: number;
  gameStatus: GameStatus;
  phase: GamePhase;
  dealerPosition: number;
  currentTurn: number;
  trumpSuit: Suit | null;
  turnedDownSuit: Suit | null;
  upcard: string | null;
  scoreTeamA: number;
  scoreTeamB: number;
  tricksTeamA: number;
  tricksTeamB: number;
  currentTrickNumber: number;
  makerTeam: Team | null;
  makerSeat: number | null;
  loneHand: boolean;
  winner: string | null;
  playerHand: string[];
  currentTrick: PlayedCard[];
  leadSuit: Suit | null;
  currentWinningSeat: number | null;
  handSizes: Record<number, number>;
  legalActions: LegalActions;
}

export interface LoginResponse {
  playerId: number;
  username: string;
  sessionToken: string;
}
