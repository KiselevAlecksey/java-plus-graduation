-- user table --

CREATE TABLE IF NOT EXISTS users
(
    id    BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    name  VARCHAR(250) NOT NULL,
    email VARCHAR(254) NOT NULL,
    CONSTRAINT users_email_ux UNIQUE (email)
);

ALTER TABLE users DROP CONSTRAINT IF EXISTS users_name_check;
ALTER TABLE users DROP CONSTRAINT IF EXISTS users_email_check;

ALTER TABLE users ADD CONSTRAINT users_name_check CHECK (TRIM(name) <> '' AND LENGTH(TRIM(name)) >= 2);
ALTER TABLE users ADD CONSTRAINT users_email_check CHECK (TRIM(email) <> '' AND LENGTH(TRIM(email)) >= 6 AND TRIM(email) LIKE '%_@_%._%');

COMMENT ON TABLE users IS 'Содержит информацию о пользователях';
COMMENT ON COLUMN users.id IS 'Уникальный идентификатор пользователя';
COMMENT ON COLUMN users.name IS 'Имя пользователя';
COMMENT ON COLUMN users.email IS 'Адрес электронной почты пользователя';