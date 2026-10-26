CREATE SEQUENCE refresh_tokens_id_seq;

CREATE TABLE refresh_tokens (
    id          INTEGER      PRIMARY KEY DEFAULT nextval('refresh_tokens_id_seq'),
    "accountId" INTEGER      NOT NULL REFERENCES accounts (id),
    "tokenHash" VARCHAR(255) NOT NULL UNIQUE,
    "expiresAt" TIMESTAMPTZ  NOT NULL,
    "createdAt" TIMESTAMPTZ  NOT NULL DEFAULT now(),
    "updatedAt" TIMESTAMPTZ  NOT NULL DEFAULT now(),
    active      BOOLEAN      NOT NULL DEFAULT TRUE
);

ALTER SEQUENCE refresh_tokens_id_seq OWNED BY refresh_tokens.id;
