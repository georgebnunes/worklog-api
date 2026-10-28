CREATE SEQUENCE logs_id_seq;

CREATE TABLE logs (
    id          INTEGER      PRIMARY KEY DEFAULT nextval('logs_id_seq'),
    "userId"    INTEGER      NOT NULL REFERENCES accounts (id),
    content     VARCHAR(140) NOT NULL,
    "createdAt" TIMESTAMPTZ  NOT NULL DEFAULT now(),
    "updatedAt" TIMESTAMPTZ  NOT NULL DEFAULT now(),
    active      BOOLEAN      NOT NULL DEFAULT TRUE
);

ALTER SEQUENCE logs_id_seq OWNED BY logs.id;
