-- oracle equivalent source sha1 d866228c3de07511d70f723c031efb8c4e25f9a5
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
lookup_tag(org_id_in in numeric, name_in in varchar)
returns numeric
as $$
declare
    tag_id  numeric;
    tag_name_id numeric;
begin
    tag_name_id := lookup_tag_name(name_in);

    select id
      into tag_id
      from rhnTag
     where org_id = org_id_in and
           name_id = tag_name_id;

    if not found then
        -- HACK: insert is isolated in own function in order to be able to declare this function immutable
        -- Postgres optimizes immutable functions calls but those are compatible with the contract of lookup_\*
        -- see https://www.postgresql.org/docs/9.6/xfunc-volatility.html
        return insert_tag(org_id_in, tag_name_id);
    end if;

    return tag_id;
end;
$$ language plpgsql immutable;
