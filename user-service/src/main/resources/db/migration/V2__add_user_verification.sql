ALTER TABLE users
    ADD COLUMN phone                   VARCHAR(30),
    ADD COLUMN email_verified boolean  NOT NULL DEFAULT FALSE,
    ADD COLUMN phone_verified boolean  NOT NULL DEFAULT FALSE;