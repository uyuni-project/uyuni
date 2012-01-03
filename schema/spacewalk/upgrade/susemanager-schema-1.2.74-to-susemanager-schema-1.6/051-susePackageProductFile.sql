--
-- Copyright (c) 2011 Novell
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
susePackageProductFile
(
    package_id numeric        not null
                             CONSTRAINT sppf_pid_fk
                             REFERENCES rhnPackage (id),
    prodfile_id numeric        not null
                             CONSTRAINT sppf_pfid_fk
                             REFERENCES suseProductFile (id),
    created     date default(sysdate) not null,
    modified    date default(sysdate) not null
)
ENABLE ROW MOVEMENT
;

CREATE UNIQUE INDEX sppf_pi_pi_uq
    ON susePackageProductFile (package_id, prodfile_id)
    TABLESPACE [[64k_tbs]];

CREATE INDEX sppf_pid_idx
    ON susePackageProductFile (package_id)
    TABLESPACE [[64k_tbs]]
    NOLOGGING;

create or replace trigger
suse_pack_prod_file_mod_trig
before insert or update on susePackageProductFile
for each row
begin
	:new.modified := sysdate;
end suse_pack_prod_file_mod_trig;
/
show errors

