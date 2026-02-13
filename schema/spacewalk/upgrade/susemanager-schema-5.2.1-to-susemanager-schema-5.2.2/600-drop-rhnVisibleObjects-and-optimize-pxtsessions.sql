BEGIN;

-- Needs to be dropped before marking PXTSessions as unlogged
DROP TABLE IF EXISTS rhnVisibleObjects;

-- To make migration robust, let's drop existing table and create new one
-- This will efectivelly logout everyone
DROP TABLE IF EXISTS PXTSessions;
DROP SEQUENCE IF EXISTS pxt_id_seq;

CREATE UNLOGGED TABLE PXTSessions
(
    id           BIGINT PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
    web_user_id  NUMERIC
                     CONSTRAINT pxtsessions_user
                         REFERENCES web_contact (id)
                         ON DELETE CASCADE,
    expires      NUMERIC DEFAULT (0) NOT NULL
);

CREATE INDEX PXTSessions_user
    ON PXTSessions (web_user_id);

CREATE INDEX PXTSessions_expires
    ON PXTSessions (expires);

DROP FUNCTION IF EXISTS create_pxt_session;

COMMIT;
