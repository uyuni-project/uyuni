-- oracle equivalent source sha1 05a7b2cc7d3436b1c99671ec1c98492eff52252f
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
lookup_package_group(name_in in varchar)
returns numeric
as
$$
declare
    package_id   numeric;
begin
    select id
      into package_id
      from rhnPackageGroup
     where name = name_in;

    if not found then
        package_id := nextval('rhn_package_group_id_seq');

        insert into rhnPackageGroup(id, name)
            values (package_id, name_in)
            on conflict do nothing;

        select id
            into strict package_id
            from rhnPackageGroup
            where name = name_in;
    end if;

    return package_id;
end;
$$ language plpgsql;
