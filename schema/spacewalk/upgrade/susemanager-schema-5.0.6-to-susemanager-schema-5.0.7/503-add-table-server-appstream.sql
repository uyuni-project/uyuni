CREATE TABLE IF NOT EXISTS suseServerAppstream(
    id  NUMERIC NOT NULL
            CONSTRAINT suse_as_servermodule_id_pk PRIMARY KEY,
    server_id NUMERIC NOT NULL
            REFERENCES rhnServer(id)
            ON DELETE CASCADE,
    name    VARCHAR(128) NOT NULL,
    stream  VARCHAR(128) NOT NULL,
    version VARCHAR(128) NOT NULL,
    context VARCHAR(16) NOT NULL,
    arch    VARCHAR(16) NOT NULL
);

CREATE SEQUENCE IF NOT EXISTS suse_as_servermodule_seq;
