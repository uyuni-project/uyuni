update suseSCCRegCache
   set server_id = NULL
 where server_id in (select s.id
                       from rhnserver s
                       join rhnservergroupmembers sgm on s.id = sgm.server_id
                       join rhnservergroup sg on sgm.server_group_id = sg.id
                       join rhnservergrouptype sgt on sg.group_type = sgt.id
                      where sgt.label = 'foreign_entitled');
