-- oracle equivalent source sha1 874b297c5c4a19955e360cfd5dfbd3f7eaa07e3d

create or replace function
insert_checksum(checksum_type_in in varchar, checksum_in in varchar)
returns numeric
as
$$
declare
    checksum_id     numeric;
begin
    checksum_id := nextval('rhnchecksum_seq');

    insert into rhnChecksum (id, checksum_type_id, checksum)
      values (
          checksum_id,
          (select id from rhnChecksumType where label = checksum_type_in),
          checksum_in
      )
      on conflict do nothing;

    select c.id
        into strict checksum_id
        from rhnChecksumView c
        where c.checksum = checksum_in and
            c.checksum_type = checksum_type_in;

    return checksum_id;
end;
$$ language plpgsql;
