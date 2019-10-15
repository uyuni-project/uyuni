-- oracle equivalent source sha1 cc7718d6336ad3ecce4391540b38e3c2b2630dc0
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
--

create or replace function
lookup_xccdf_profile(identifier_in in varchar, title_in in varchar)
returns numeric
as
$$
declare
    profile_id numeric;
begin
    select id
      into profile_id
      from rhnXccdfProfile
     where identifier = identifier_in and title = title_in;

    if not found then
        -- HACK: insert is isolated in own function in order to be able to declare this function immutable
        -- Postgres optimizes immutable functions calls but those are compatible with the contract of lookup_\*
        -- see https://www.postgresql.org/docs/9.6/xfunc-volatility.html
        return insert_xccdf_profile(identifier_in, title_in);
    end if;

    return profile_id;
end;
$$ language plpgsql immutable;
