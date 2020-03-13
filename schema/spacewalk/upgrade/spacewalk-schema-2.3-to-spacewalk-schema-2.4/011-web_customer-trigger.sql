
create or replace function web_customer_insert_trig_fun() returns trigger
as
$$
begin
	insert into rhnOrgConfiguration (org_id) values (new.id);
	insert into rhnOrgAdminManagement (org_id) values (new.id);

        return new;
end;
$$
language plpgsql;
