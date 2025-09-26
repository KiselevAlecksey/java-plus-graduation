CREATE TABLE IF NOT EXISTS interactions (
    user_id BIGINT NOT NULL,
    event_id BIGINT NOT NULL,
    rating FLOAT NOT NULL,
    ts TIMESTAMP WITH TIME ZONE NOT NULL,
    PRIMARY KEY (user_id, event_id),
    UNIQUE (user_id, event_id)
);

CREATE TABLE IF NOT EXISTS similarities (
    eventA BIGINT NOT NULL,
    eventB BIGINT NOT NULL,
    similarity FLOAT NOT NULL,
    ts TIMESTAMP WITH TIME ZONE NOT NULL,
    PRIMARY KEY (eventA, eventB),
    UNIQUE (eventA, eventB)
);