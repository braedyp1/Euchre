create table players (
    id bigserial primary key,
    username varchar(20) not null unique,
    total_wins integer not null default 0,
    total_losses integer not null default 0,
    total_games_played integer not null default 0,
    created_at timestamptz not null,
    updated_at timestamptz not null
);

create table session_tokens (
    id bigserial primary key,
    player_id bigint not null references players(id),
    token varchar(36) not null unique,
    created_at timestamptz not null,
    last_used_at timestamptz not null
);

create table game_sessions (
    id bigserial primary key,
    version bigint not null default 0,
    player_id bigint not null references players(id),
    game_status varchar(20) not null,
    phase varchar(30) not null,
    dealer_position integer not null,
    current_turn integer not null,
    trump_suit varchar(12),
    turned_down_suit varchar(12),
    maker_team varchar(12),
    maker_seat integer,
    lone_hand boolean not null default false,
    score_team_a integer not null default 0,
    score_team_b integer not null default 0,
    tricks_team_a integer not null default 0,
    tricks_team_b integer not null default 0,
    current_trick_number integer not null default 1,
    passes_this_round integer not null default 0,
    upcard varchar(8),
    winner varchar(20),
    created_at timestamptz not null,
    updated_at timestamptz not null,
    last_action_timestamp timestamptz not null
);

create table hand_states (
    id bigserial primary key,
    game_session_id bigint not null references game_sessions(id) on delete cascade,
    seat_position integer not null,
    serialized_cards text not null,
    unique (game_session_id, seat_position)
);

create table trick_states (
    id bigserial primary key,
    game_session_id bigint not null references game_sessions(id) on delete cascade,
    lead_suit varchar(12),
    winning_seat integer,
    played_cards text not null,
    trick_number integer not null,
    unique (game_session_id, trick_number)
);

create table action_logs (
    id bigserial primary key,
    game_session_id bigint not null references game_sessions(id) on delete cascade,
    actor varchar(40) not null,
    action_type varchar(30) not null,
    payload text not null,
    timestamp timestamptz not null
);

create index idx_players_username on players(username);
create index idx_session_tokens_token on session_tokens(token);
create index idx_game_sessions_player on game_sessions(player_id);
create index idx_game_sessions_status on game_sessions(game_status);
create index idx_game_sessions_updated on game_sessions(updated_at);
create index idx_action_logs_game on action_logs(game_session_id);
