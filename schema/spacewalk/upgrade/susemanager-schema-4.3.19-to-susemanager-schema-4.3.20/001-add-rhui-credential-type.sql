DO $$
  BEGIN
    IF EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name = 'susecredentialstype') THEN
      insert into suseCredentialsType (id, label, name)
        select sequence_nextval('suse_credtype_id_seq'), 'rhui', 'Red Hat Update Infrastructure'
        where not exists(select 1 from suseCredentialsType where label = 'rhui');
    ELSE
      RAISE NOTICE 'suseCredentialsType does not exists';
    END IF;
  END;
$$;
