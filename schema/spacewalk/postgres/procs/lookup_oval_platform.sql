--
-- Copyright (c) 2024 SUSE LLC
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
lookup_oval_platform(platform_cpe_in in varchar)
returns numeric
as $$
declare
    platform_cpe_in_val numeric;
begin
    if not exists(select c from suseOVALPlatform c where cpe = platform_cpe_in) then
        insert into suseovalplatform(id, cpe)
        values (nextval('suse_oval_platform_id_seq'), platform_cpe_in);
    end if;

    select id into platform_cpe_in_val FROM suseOVALPlatform WHERE cpe = platform_cpe_in;

    return platform_cpe_in_val;
end;
$$ language plpgsql;