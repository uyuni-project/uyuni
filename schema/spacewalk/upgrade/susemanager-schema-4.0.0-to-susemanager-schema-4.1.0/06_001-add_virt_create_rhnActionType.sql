insert into rhnActionType (id, label, name, trigger_snapshot, unlocked_only) (
    select 508, 'virt.create', 'Creates a virtual domain.', 'N', 'N'
    from dual
    where not exists (select 1 from rhnActionType where id = 508)
);
