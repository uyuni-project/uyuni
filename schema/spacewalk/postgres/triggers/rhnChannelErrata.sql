
create or replace function rhn_channel_errata_mod_trig_fun() returns trigger as
$$
begin
        new.modified := current_timestamp;

        return new;
end;
$$ language plpgsql;

create trigger
rhn_channel_errata_mod_trig
before insert or update on rhnChannelErrata
for each row
execute procedure rhn_channel_errata_mod_trig_fun();
