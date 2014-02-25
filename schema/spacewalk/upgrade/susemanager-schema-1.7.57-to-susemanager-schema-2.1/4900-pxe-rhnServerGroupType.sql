--  bootstrap_entitled type ----------------------------------------------------

insert into rhnServerGroupType ( id, label, name, permanent, is_base) (
    select sequence_nextval('rhn_servergroup_type_seq'),
                            'bootstrap_entitled',
                            'Bootstrap Entitled Servers',
                            'N', 'Y'
      from dual
     where not exists (
           select 1
             from rhnServerGroupType
            where label = 'bootstrap_entitled'
     )
);

