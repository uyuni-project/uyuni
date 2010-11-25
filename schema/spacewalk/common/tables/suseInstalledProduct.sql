--
-- Copyright (c) 2010 Novell
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
suseInstalledProduct
(
    id            number        not null PRIMARY KEY,
    name          varchar2(256) not null,
    version       varchar2(256),
    arch_type_id  NUMBER
                  CONSTRAINT suse_installed_product_aid_fk
                  REFERENCES rhnArchType (id),
    release       varchar2(256),
    is_baseproduct CHAR(1 BYTE) DEFAULT ('N') NOT NULL ENABLE,

    created     date default(sysdate) not null,
    modified    date default(sysdate) not null
);

CREATE SEQUENCE suse_inst_pr_id_seq START WITH 100;
