-- oracle equivalent source sha1 0a62bbccb566b9483e2059f68e8252a9b0730d17
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
lookup_package_name(name_in in varchar, ignore_null in numeric default 0)
returns numeric
as
$$
declare
    name_id     numeric;
begin
    if ignore_null = 1 and name_in is null then
        return null;
    end if;

    select id
      into name_id
      from rhnPackageName
     where name = name_in;

    if not found then
        -- HACK: insert is isolated in own function in order to be able to declare this function immutable
        -- Postgres optimizes immutable functions calls but those are compatible with the contract of lookup_\*
        -- see https://www.postgresql.org/docs/9.6/xfunc-volatility.html
        return insert_package_name(name_in);
    end if;

    return name_id;
end;
$$ language plpgsql immutable;
