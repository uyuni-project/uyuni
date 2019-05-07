-- oracle equivalent source sha1 716dbf40cfe2e527bb1fbf98a7a514d256cc5a8f
--
-- Copyright (c) 2012 Red Hat, Inc.
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
lookup_xccdf_ident(system_in in varchar, identifier_in in varchar)
returns numeric
as
$$
declare
    xccdf_ident_id numeric;
    ident_sys_id numeric;
begin
    select id
      into ident_sys_id
      from rhnXccdfIdentSystem
     where system = system_in;

    if not found then
        -- HACK: insert is isolated in own function in order to be able to declare this function immutable
        -- Postgres optimizes immutable functions calls but those are compatible with the contract of lookup_\*
        -- see https://www.postgresql.org/docs/9.6/xfunc-volatility.html
        ident_sys_id := insert_xccdf_ident_system(system_in);
    end if;

    select id
      into xccdf_ident_id
      from rhnXccdfIdent
     where identsystem_id = ident_sys_id and identifier = identifier_in;

    if not found then
        return insert_xccdf_ident(ident_sys_id, identifier_in);
    end if;

    return xccdf_ident_id;
end;
$$ language plpgsql immutable;
