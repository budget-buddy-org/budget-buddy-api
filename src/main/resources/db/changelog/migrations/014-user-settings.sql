--liquibase formatted sql

--changeset g.remniov@gmail.com:014-user-settings
CREATE TABLE user_preferences
(
    user_id    UUID PRIMARY KEY REFERENCES users (id) ON DELETE CASCADE,
    language   VARCHAR(7)  NOT NULL,
    currency   VARCHAR(3)  NOT NULL,
    timezone   VARCHAR(64) NOT NULL,
    version    INTEGER     NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL
);

CREATE TABLE user_client_settings
(
    id         UUID PRIMARY KEY,
    owner_id   UUID        NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    client_id  VARCHAR(32) NOT NULL,
    settings   JSONB       NOT NULL,
    version    INTEGER     NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    UNIQUE (owner_id, client_id)
);

--rollback DROP TABLE user_client_settings;
--rollback DROP TABLE user_preferences;
