insert into suseCredentialsType (id, label, name) (
  select sequence_nextval('suse_credtype_id_seq'), 'registrycreds', 'Registry'
    from dual
   where not exists (
           select 1
             from suseCredentialsType
            where label = 'registrycreds'
  )
);
