CREATE TABLE battles (
    battle_id UUID PRIMARY KEY NOT NULL,
    player_id UUID NOT NULL,
    level_id UUID NOT NULL,
    score INTEGER NOT NULL,
    started_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    completed_at TIMESTAMP WITH TIME ZONE
);

CREATE INDEX idx_battles_player_id_started_at ON battles (player_id, started_at);
CREATE INDEX idx_battles_player_id_completed_at ON battles (player_id, completed_at);
