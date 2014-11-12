--
-- Copyright (c) 2010-2012 Novell
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
suseProducts
(
    id            number        not null PRIMARY KEY,
    name          varchar2(256) not null,
    version       varchar2(256),
    friendly_name varchar2(256),
    arch_type_id  NUMBER
                  CONSTRAINT suse_products_aid_fk
                  REFERENCES rhnPackageArch (id),
    release       varchar2(256),
    product_id    NUMBER NOT NULL,
    created   timestamp with local time zone
                  DEFAULT (current_timestamp) NOT NULL,
    modified  timestamp with local time zone
                  DEFAULT (current_timestamp) NOT NULL
);

CREATE SEQUENCE suse_products_id_seq START WITH 100;

CREATE INDEX suseprod_pdid_idx
ON suseProducts (product_id)
TABLESPACE [[64k_tbs]];

