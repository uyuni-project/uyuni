DO $$
  BEGIN
    IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'rhnactivationkey' AND column_name = 'bootstrap') THEN

      DELETE FROM rhnActivationKey where bootstrap = 'Y';

      ALTER TABLE rhnActivationKey DROP COLUMN IF EXISTS bootstrap;

    END IF;
  END;
$$;
