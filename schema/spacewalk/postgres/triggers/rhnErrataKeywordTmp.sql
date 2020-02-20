
create or replace function rhn_errata_keywordtmp_mod_trig_fun() returns trigger as
$$
begin
	new.modified := current_timestamp;
	return new;
end;
$$ language plpgsql;

create trigger
rhn_errata_keywordtmp_mod_trig
before insert or update on rhnErrataKeywordTmp
for each row
execute procedure rhn_errata_keywordtmp_mod_trig_fun();
