-- oracle equivalent source sha1 c6072b2270204eee9312f4f43bfc3c0c7eb4dc4e

create or replace function
insert_transaction_package(
    o_id_in      in numeric,
    n_id_in      in numeric,
    e_id_in      in numeric,
    p_arch_id_in in numeric
)
returns numeric
as
$$
declare
    tp_id       numeric;
begin
    tp_id := nextval('rhn_transpack_id_seq');

    insert into rhnTransactionPackage (id, operation, name_id, evr_id, package_arch_id)
        values (tp_id, o_id_in, n_id_in, e_id_in, p_arch_id_in)
        on conflict do nothing;

    select id
        into strict tp_id
        from rhnTransactionPackage
     where operation = o_id_in and name_id = n_id_in and evr_id = e_id_in and
        (package_arch_id = p_arch_id_in or (p_arch_id_in is null and package_arch_id is null));

    return tp_id;
end;
$$ language plpgsql;
