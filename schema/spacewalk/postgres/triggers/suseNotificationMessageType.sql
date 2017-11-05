-- oracle equivalent source sha1 2078ae7aee30852c64dd3df6d2fe038ef80e204e

create or replace function suse_notifmesstype_mod_trig_fun() returns trigger as
$$
begin
        new.modified := current_timestamp;
        return new;
end;
$$ language plpgsql;

create trigger
suse_notifmesstype_mod_trig
before insert or update on suseNotificationMessageType
for each row
execute procedure suse_notifmesstype_mod_trig_fun();

