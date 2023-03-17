
drop trigger rhn_conffiletype_mod_trig on rhnConfigFile;

create trigger
rhn_conffiletype_mod_trig
before insert or update on rhnConfigFileType
for each row
execute procedure rhn_conffiletype_mod_trig_fun();
