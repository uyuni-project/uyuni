-- oracle equivalent source none

CREATE TABLE suseSaltEvent (
  id SERIAL PRIMARY KEY,
  minion_id CHARACTER VARYING(256),
  data TEXT NOT NULL,
  queue NUMERIC NOT NULL
);

CREATE INDEX suse_salt_event_minion_id_idx
  ON suseSaltEvent (queue, minion_id NULLS FIRST, id);
