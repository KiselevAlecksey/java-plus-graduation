
CREATE TABLE IF NOT EXISTS subscribers
(
    id    BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    user_id BIGINT,
    subscriber BIGINT,
    CONSTRAINT subscribers_user_id FOREIGN KEY (user_id) REFERENCES users (id)
);

CREATE TABLE IF NOT EXISTS black_list
(
    id    BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    user_id BIGINT NOT NULL,
    block_user BIGINT,
    CONSTRAINT black_list_user_id FOREIGN KEY (user_id) REFERENCES users (id)
);