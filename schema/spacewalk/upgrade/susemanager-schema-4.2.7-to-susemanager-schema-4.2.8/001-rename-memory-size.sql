DO $$
BEGIN
  IF EXISTS(SELECT *
    FROM information_schema.columns
    WHERE table_name='rhnvirtualinstanceinfo' and column_name='memory_size_k')
  THEN
      ALTER TABLE rhnVirtualInstanceInfo RENAME COLUMN memory_size_k TO memory_size;
  END IF;
END $$;
