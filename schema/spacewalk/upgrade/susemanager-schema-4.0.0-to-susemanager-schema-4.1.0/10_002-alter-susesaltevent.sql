
DROP INDEX IF EXISTS suse_salt_event_minion_id_idx;

CREATE INDEX IF NOT EXISTS suse_salt_event_minion_id_idx
  ON suseSaltEvent (minion_id NULLS FIRST, id);
