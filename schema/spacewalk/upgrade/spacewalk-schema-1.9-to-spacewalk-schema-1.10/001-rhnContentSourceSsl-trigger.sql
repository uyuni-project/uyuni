
create or replace function rhn_csssl_ins_trig_fun() returns trigger as
$$
begin
    if new.id is null then
        new.id := sequence_nextval('rhn_contentsourcessl_seq');
    end if;
    return new;
end;
$$ language plpgsql;

create trigger
rhn_csssl_ins_trig
before insert on rhnContentSourceSsl
for each row
execute procedure rhn_csssl_ins_trig_fun();
