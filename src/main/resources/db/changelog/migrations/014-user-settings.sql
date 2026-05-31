--liquibase formatted sql

--changeset g.remniov@gmail.com:014-user-settings
-- clock_timestamp() (not now()) so created_at reflects true insert order even for multiple
-- inserts within one transaction — list endpoints order client settings oldest-first.
CREATE TABLE user_preferences
(
    user_id    UUID PRIMARY KEY REFERENCES users (id) ON DELETE CASCADE,
    language   VARCHAR(7),
    currency   VARCHAR(3),
    timezone   VARCHAR(64),
    created_at TIMESTAMPTZ NOT NULL DEFAULT clock_timestamp(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT clock_timestamp()
);

CREATE TABLE user_client_settings
(
    user_id    UUID        NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    client_id  VARCHAR(32) NOT NULL,
    settings   JSONB       NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT clock_timestamp(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT clock_timestamp(),
    PRIMARY KEY (user_id, client_id)
);

--rollback DROP TABLE user_client_settings;
--rollback DROP TABLE user_preferences;
