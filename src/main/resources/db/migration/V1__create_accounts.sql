CREATE SEQUENCE accounts_id_seq;

CREATE TABLE accounts (
    id               INTEGER      PRIMARY KEY DEFAULT nextval('accounts_id_seq'),
    username         VARCHAR(30)  NOT NULL,
    password         VARCHAR(255) NOT NULL,
    email            VARCHAR(255) NOT NULL,
    "emailConfirmed" BOOLEAN      NOT NULL DEFAULT FALSE,
    "createdAt"      TIMESTAMPTZ  NOT NULL DEFAULT now(),
    "updatedAt"      TIMESTAMPTZ  NOT NULL DEFAULT now(),
    active           BOOLEAN      NOT NULL DEFAULT TRUE
);

ALTER SEQUENCE accounts_id_seq OWNED BY accounts.id;

CREATE UNIQUE INDEX accounts_username_lower_uk ON accounts (lower(username));
CREATE UNIQUE INDEX accounts_email_lower_uk ON accounts (lower(email));
