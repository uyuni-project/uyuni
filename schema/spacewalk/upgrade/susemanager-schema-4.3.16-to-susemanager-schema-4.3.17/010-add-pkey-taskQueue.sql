ALTER TABLE rhnTaskQueue ADD COLUMN IF NOT EXISTS id NUMERIC;
CREATE SEQUENCE IF NOT EXISTS rhn_task_queue_id_seq START WITH 1;
UPDATE rhnTaskQueue SET id = nextval('rhn_task_queue_id_seq') WHERE id IS NULL;
ALTER TABLE rhnTaskQueue ALTER COLUMN id SET NOT NULL;

ALTER TABLE rhnTaskQueue DROP CONSTRAINT IF EXISTS rhn_task_queue_id_pk;
ALTER TABLE rhnTaskQueue ADD CONSTRAINT rhn_task_queue_id_pk PRIMARY KEY (id);
