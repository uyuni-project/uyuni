
CREATE TABLE IF NOT EXISTS suseSaltEvent (
  id SERIAL PRIMARY KEY,
  minion_id CHARACTER VARYING(256),
  data TEXT NOT NULL
);

CREATE INDEX IF NOT EXISTS suse_salt_event_minion_id_idx
  ON suseSaltEvent (minion_id NULLS LAST, id);
