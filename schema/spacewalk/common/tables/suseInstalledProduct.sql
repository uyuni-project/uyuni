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
    id            NUMERIC        not null PRIMARY KEY,
    name          VARCHAR(256) not null,
    version       VARCHAR(256),
    arch_type_id  NUMERIC
                  CONSTRAINT suse_installed_product_aid_fk
                  REFERENCES rhnPackageArch (id),
    release       VARCHAR(256),
    is_baseproduct CHAR(1) DEFAULT ('N') NOT NULL,
    created   TIMESTAMPTZ
                  DEFAULT (current_timestamp) NOT NULL,
    modified  TIMESTAMPTZ
                  DEFAULT (current_timestamp) NOT NULL
);

CREATE SEQUENCE suse_inst_pr_id_seq START WITH 100;
