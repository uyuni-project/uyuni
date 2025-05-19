insert into rhnActionType 
select 527, 'virt.refresh_list', 'Refresh virtual instance information', 'N', 'N', 'N'
where not exists (select 1 from rhnActionType where id = 527);
