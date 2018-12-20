create or replace procedure drop_if_exists(type_in varchar2, name_in varchar2)
is
  cnt number := 0;
begin
  select count(*) into cnt from user_objects where object_type = upper(type_in) and object_name = upper(name_in);
  if cnt > 0 then
    if upper(type_in) = 'TABLE' then
      execute immediate 'drop ' || type_in || ' ' || name_in || ' purge';
    else
      execute immediate 'drop ' || type_in || ' ' || name_in;
    end if;
  end if;
end;
/
show errors

