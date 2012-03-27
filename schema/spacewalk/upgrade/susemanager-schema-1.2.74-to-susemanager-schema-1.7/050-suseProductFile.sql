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
create table suseProductFile
(
  id              NUMBER NOT NULL PRIMARY KEY,
  name            VARCHAR2(256) NOT NULL,
  evr_id          NUMBER NOT NULL
                         CONSTRAINT suse_prod_file_eid_fk
                         REFERENCES rhnpackageevr (id),
  package_arch_id NUMBER NOT NULL
                         CONSTRAINT suse_prod_file_paid_fk
                         REFERENCES rhnpackagearch (id),
  vendor          VARCHAR2(256),
  summary         VARCHAR2(4000),
  description     VARCHAR2(4000),
  created         date default(sysdate) not null,
  modified        date default(sysdate) not null
);

CREATE SEQUENCE suse_prod_file_id_seq START WITH 100;

create or replace trigger
suse_product_file_mod_trig
before insert or update on suseProductFile
for each row
begin
	:new.modified := sysdate;
end suse_product_file_mod_trig;
/
show errors

