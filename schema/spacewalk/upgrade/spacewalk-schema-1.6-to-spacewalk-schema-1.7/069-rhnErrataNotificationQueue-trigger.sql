
drop trigger rhn_enqueue_mod_trig on rhnErrataKeywordTmp;

create trigger
rhn_enqueue_mod_trig
before insert or update on rhnErrataNotificationQueue
for each row
execute procedure rhn_enqueue_mod_trig_fun();
