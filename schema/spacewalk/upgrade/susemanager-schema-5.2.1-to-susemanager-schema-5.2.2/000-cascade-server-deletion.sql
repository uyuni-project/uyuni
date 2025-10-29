DO $$
  BEGIN
    IF NOT EXISTS (SELECT 1 FROM information_schema.referential_constraints WHERE constraint_name='rhn_actiondup_sid_fk' AND delete_rule='CASCADE') THEN
        ALTER TABLE rhnactiondup
            DROP CONSTRAINT IF EXISTS rhn_actiondup_sid_fk;

        ALTER TABLE rhnactiondup
            ADD CONSTRAINT rhn_actiondup_sid_fk
                FOREIGN KEY (server_id)
                REFERENCES rhnServer (id)
                ON DELETE CASCADE;
    ELSE
      RAISE NOTICE 'Constraint rhn_actiondup_sid_fk has already DELETE CASCADE';
    END IF;
  END;
$$;