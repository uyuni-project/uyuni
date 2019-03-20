-- oracle equivalent source sha1 d3b5c7a74a87caed2bb73e40ad371c19fe8be006
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
lookup_package_capability(name_in in varchar, version_in in varchar default null)
returns numeric
as
$$
declare
    name_id numeric;
begin
    if version_in is null then
        select id
          into name_id
          from rhnpackagecapability
         where name = name_in and
               version is null;
    else
        select id
          into name_id
          from rhnpackagecapability
         where name = name_in and
               version = version_in;
    end if;

    if not found then
        -- HACK: insert is isolated in own function in order to be able to declare this function immutable
        -- Postgres optimizes immutable functions calls but those are compatible with the contract of lookup_\*
        -- see https://www.postgresql.org/docs/9.6/xfunc-volatility.html
        return insert_package_capability(name_in, version_in);
    end if;

    return name_id;
end;
$$ language plpgsql immutable;

-- Note: intentionally not thread-safe! You must aquire a write lock on the
-- rhnPackageCapability tabel if you are going to use this proc!
create or replace function lookup_package_capability_fast(name_in in varchar, version_in in varchar default null)
returns numeric
as
$$
declare
    name_id numeric;
begin
    if version_in is null then
        select id
          into name_id
          from rhnpackagecapability
         where name = name_in and
               version is null;
    else
        select id
          into name_id
          from rhnpackagecapability
         where name = name_in and
               version = version_in;
    end if;

    if not found then
        name_id := nextval('rhn_pkg_capability_id_seq');
        insert into rhnPackageCapability(id, name, version) values (
               name_id, name_in, version_in);
    end if;

    return name_id;
end;
$$ language plpgsql;
