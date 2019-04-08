-- oracle equivalent source none

CREATE TABLE suseSaltEvent (
  id SERIAL PRIMARY KEY,
  minion_id CHARACTER VARYING(256),
  data TEXT NOT NULL
);

CREATE INDEX suse_salt_event_minion_id_idx
  ON suseSaltEvent (minion_id NULLS FIRST, id);
