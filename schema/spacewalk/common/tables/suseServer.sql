create table
suseServer
(
    rhn_server_id     number
                      CONSTRAINT suseserver_rhns_id_fk
                      REFERENCES rhnserver (id)
                      ON DELETE CASCADE
                      PRIMARY KEY,
    guid              varchar2(256)
                      CONSTRAINT suseserver_guid_uq UNIQUE,
    secret            varchar2(256),
    ostarget          varchar2(256),
    ncc_sync_required CHAR(1 BYTE) DEFAULT ('N') NOT NULL ENABLE,

    created     date default(sysdate) not null,
    modified    date default(sysdate) not null
);

