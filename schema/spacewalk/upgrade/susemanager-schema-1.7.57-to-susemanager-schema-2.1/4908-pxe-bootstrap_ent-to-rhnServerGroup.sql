
select .clear_log_id() from dual;

insert into rhnServerGroup ( id, name, description, max_members, group_type, org_id )
  select nextval('rhn_server_group_id_seq'), sgt.name, sgt.name, 200000, sgt.id, X.org_id
  from rhnServerGroupType sgt,
     (select distinct msg.org_id
        from rhnServerGroup msg
       where msg.org_id not in (select org_id
                                  from rhnServerGroup sg
                                  join rhnServerGroupType sgt ON sgt.id = sg.group_type
                                 where sgt.label = 'bootstrap_entitled')
     ) X
  where sgt.label = 'bootstrap_entitled';
