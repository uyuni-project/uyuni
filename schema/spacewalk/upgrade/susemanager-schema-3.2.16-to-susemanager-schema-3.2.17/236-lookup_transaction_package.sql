-- oracle equivalent source sha1 14a86a37406ecfb22852f3d7e268f247a55d6cf7
--
-- Copyright (c) 2008--2012 Red Hat, Inc.
--
-- This software is licensed to you under the GNU General Public License,
-- version 2 (GPLv2). There is NO WARRANTY for this software, express or
-- implied, including the implied warranties of MERCHANTABILITY or FITNESS
-- FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
-- along with this software; if not, see
-- http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
-- 
-- Red Hat trademarks are not licensed under GPLv2. No permission is
-- granted to use or replicate Red Hat trademarks that are incorporated
-- in this software or its documentation. 

create or replace function
lookup_transaction_package(
    o_in in varchar,
    n_in in varchar,
    e_in in varchar,
    v_in in varchar,
    r_in in varchar,
    a_in in varchar)
returns numeric
as
$$
declare
    o_id        numeric;
    n_id        numeric;
    e_id        numeric;
    p_arch_id   numeric;
    tp_id       numeric;
begin
    select id
      into o_id
      from rhnTransactionOperation
     where label = o_in;

    if not found then
        perform rhn_exception.raise_exception('invalid_transaction_operation');
    end if;

    n_id := lookup_package_name(n_in);
    e_id := lookup_evr(e_in, v_in, r_in);
    p_arch_id := null;

    if a_in is not null then
        p_arch_id := lookup_package_arch(a_in);
    end if;

    select id
      into tp_id
      from rhnTransactionPackage
     where operation = o_id and
           name_id = n_id and
           evr_id = e_id and
           (package_arch_id = p_arch_id or (p_arch_id is null and package_arch_id is null));

    if not found then
        -- HACK: insert is isolated in own function in order to be able to declare this function immutable
        -- Postgres optimizes immutable functions calls but those are compatible with the contract of lookup_\*
        -- see https://www.postgresql.org/docs/9.6/xfunc-volatility.html
        return insert_transaction_package(o_id, n_id, e_id, p_arch_id);
    end if;
    return tp_id;
end;
$$ language plpgsql immutable;
