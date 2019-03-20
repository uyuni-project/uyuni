-- oracle equivalent source sha1 4202a766e12fa3137a822d20683ccabe897631a1
-- This file is intentionally left empty.

create or replace function
insert_xccdf_benchmark(identifier_in in varchar, version_in in varchar)
returns numeric
as
$$
declare
    benchmark_id numeric;
begin
    benchmark_id := nextval('rhn_xccdf_benchmark_id_seq');

    insert into rhnXccdfBenchmark (id, identifier, version)
        values (benchmark_id, identifier_in, version_in)
        on conflict do nothing;

    select id
        into strict benchmark_id
        from rhnXccdfBenchmark
        where identifier = identifier_in and version = version_in;

    return benchmark_id;
end;
$$ language plpgsql;
