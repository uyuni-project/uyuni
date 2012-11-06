-- oracle equivalent source sha1 776c0f457428023314bff3357354e89576cff559
--
-- Copyright (c) 2012 Novell
--
-- This software is licensed to you under the GNU General Public License,
-- version 2 (GPLv2). There is NO WARRANTY for this software, express or
-- implied, including the implied warranties of MERCHANTABILITY or FITNESS
-- FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
-- along with this software; if not, see
-- http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
--
--


create table
suseUpgradePath
(
    from_pdid     numeric not null
                  CONSTRAINT suse_upgpath_fromid_fk
                  REFERENCES suseProducts (id)
                  ON DELETE CASCADE,
    to_pdid       numeric not null
                  CONSTRAINT suse_upgpath_toid_fk
                  REFERENCES suseProducts (id)
                  ON DELETE CASCADE,
    created       timestamptz default (current_timestamp) not null,
    modified      timestamptz default (current_timestamp) not null
);

CREATE INDEX suseupgpath_fromid_idx
ON suseUpgradePath (from_pdid);

create or replace function suse_upgpath_mod_trig_fun() returns trigger
as
$$
begin
        new.modified := current_timestamp;

        return new;
end;
$$
language plpgsql;


create trigger
suseupgpath_mod_trig
before insert or update on suseUpgradePath
for each row
execute procedure suse_upgpath_mod_trig_fun();

