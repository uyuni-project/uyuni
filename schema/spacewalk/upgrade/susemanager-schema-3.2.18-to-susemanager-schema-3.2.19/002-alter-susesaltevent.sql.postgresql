-- oracle equivalent source sha1 81eaaa6f639e70eb12dc131d659536c6ec576644

DROP INDEX IF EXISTS suse_salt_event_minion_id_idx;

CREATE INDEX IF NOT EXISTS suse_salt_event_minion_id_idx
  ON suseSaltEvent (minion_id NULLS FIRST, id);
