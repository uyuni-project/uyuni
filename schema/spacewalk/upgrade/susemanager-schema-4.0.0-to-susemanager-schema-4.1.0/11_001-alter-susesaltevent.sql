
ALTER TABLE suseSaltEvent DROP COLUMN IF EXISTS queue;

ALTER TABLE suseSaltEvent ADD queue NUMERIC;
UPDATE suseSaltEvent SET queue = 0;
ALTER TABLE suseSaltEvent ALTER COLUMN queue SET NOT NULL;

DROP INDEX IF EXISTS suse_salt_event_minion_id_idx;

CREATE INDEX IF NOT EXISTS suse_salt_event_minion_id_idx
  ON suseSaltEvent (queue, minion_id NULLS FIRST, id);
