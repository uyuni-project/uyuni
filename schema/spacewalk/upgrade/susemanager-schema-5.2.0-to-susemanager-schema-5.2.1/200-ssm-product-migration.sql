DO $$
  BEGIN
    IF NOT EXISTS (SELECT 1 FROM information_schema."columns" WHERE table_name='rhnactiondup' AND column_name='server_id') THEN
      -- Add the column as nullable
      ALTER TABLE rhnActionDup ADD COLUMN IF NOT EXISTS server_id NUMERIC NULL;

      -- Copy the correct server id from the single server action
      UPDATE rhnActionDup
         SET server_id = rhnserveraction.server_id
        FROM rhnserveraction
       WHERE rhnActionDup.action_id = rhnserveraction.action_id AND rhnActionDup.server_id IS NULL;

      -- Remove any line for which we cannot figure out the server (orphan actions executed on deleted servers)
      DELETE FROM rhnActionDup WHERE server_id IS NULL;

      -- Alter the table to add the non null and the foreign key
      ALTER TABLE rhnActionDup
          ALTER COLUMN server_id SET NOT NULL;

      ALTER TABLE rhnActionDup
          ADD CONSTRAINT rhn_actiondup_sid_fk FOREIGN KEY (server_id) REFERENCES rhnServer (id);

      -- Recreate the index on action id, since it's no longer unique
      DROP INDEX IF EXISTS rhn_actiondup_aid_uq;
      CREATE INDEX IF NOT EXISTS rhn_actiondup_aid_idx ON rhnActionDup (action_id);

      -- Create the index
      CREATE INDEX IF NOT EXISTS rhn_actiondup_sid_idx ON rhnActionDup (server_id);
    ELSE
      RAISE NOTICE 'rhnActionDup already contains the column server_id';
    END IF;
  END;
$$;
