CREATE TABLE suseCredentials
(
    id       NUMBER NOT NULL
                 CONSTRAINT suse_credentials_pk PRIMARY KEY,
    org_id   NUMBER NOT NULL UNIQUE
                 CONSTRAINT suse_credentials_oid_fk
                 REFERENCES web_customer (id)
                 ON DELETE CASCADE,
    type     VARCHAR2(32) NOT NULL,
    hostname VARCHAR2(256),
    username VARCHAR2(64) NOT NULL,
    password VARCHAR2(64) NOT NULL,
    created  DATE DEFAULT (sysdate) NOT NULL,
    modified DATE DEFAULT (sysdate) NOT NULL
)
ENABLE ROW MOVEMENT
;

CREATE SEQUENCE suse_credentials_id_seq START WITH 100;

