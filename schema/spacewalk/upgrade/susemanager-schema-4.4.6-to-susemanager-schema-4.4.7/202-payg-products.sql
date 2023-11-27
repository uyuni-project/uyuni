CREATE TABLE IF NOT EXISTS susePaygProduct
(
    id                          NUMERIC NOT NULL
                                CONSTRAINT susePaygProduct_pk PRIMARY KEY,
    credentials_id              NUMERIC NOT NULL
                                CONSTRAINT susePaygProduct_credentials_id_fk
                                REFERENCES suseCredentials (id)
                                ON DELETE CASCADE,
    name                        VARCHAR(256),
    version                     VARCHAR(256),
    arch                        VARCHAR(256),
    created                     TIMESTAMPTZ DEFAULT (current_timestamp) NOT NULL,
    modified                    TIMESTAMPTZ DEFAULT (current_timestamp) NOT NULL
);

CREATE SEQUENCE IF NOT EXISTS susePaygProduct_id_seq;

CREATE UNIQUE INDEX IF NOT EXISTS susePaygProduct_credentials_product_uq
    ON susePaygProduct (credentials_id, name, version, arch);
