CREATE TABLE IF NOT EXISTS requests
(
    id           BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    event_id     BIGINT                      NOT NULL,
    created      TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    requester_id BIGINT                      NOT NULL,
    status       VARCHAR(10)                 NOT NULL,
    CONSTRAINT status_values CHECK (status IN ('PENDING', 'CONFIRMED', 'REJECTED', 'CANCELED'))
);