-- oracle equivalent source sha1 d55d28857e2a08b53ff47a87285909cb392522d5

 SELECT logging.clear_log_id();

 insert into rhnServerGroup ( id, name, description, group_type, org_id )
  select nextval('rhn_server_group_id_seq'), sgt.name, sgt.name, sgt.id, X.org_id
  from rhnServerGroupType sgt,
     (select distinct msg.org_id
        from rhnServerGroup msg
       where msg.org_id not in (select org_id
                                  from rhnServerGroup sg
                                  join rhnServerGroupType sgt ON sgt.id = sg.group_type
                                 where sgt.label = 'foreign_entitled')
     ) X
  where sgt.label = 'foreign_entitled';
