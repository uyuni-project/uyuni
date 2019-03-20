-- oracle equivalent source sha1 fea9bf6e4df0ab1c2654501af40bdb91b39457b3
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
lookup_evr(e_in in varchar, v_in in varchar, r_in in varchar)
returns numeric
as
$$
declare
    evr_id  numeric;
begin
    select id
      into evr_id
      from rhnPackageEVR
     where ((epoch is null and e_in is null) or (epoch = e_in)) and
           version = v_in and
           release = r_in;

    if not found then
        -- HACK: insert is isolated in own function in order to be able to declare this function immutable
        -- Postgres optimizes immutable functions calls but those are compatible with the contract of lookup_\*
        -- see https://www.postgresql.org/docs/9.6/xfunc-volatility.html
        return insert_evr(e_in, v_in, r_in);
    end if;

    return evr_id;
end;
$$ language plpgsql immutable;
