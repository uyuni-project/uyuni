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
    id            NUMERIC        not null PRIMARY KEY,
    name          VARCHAR(256) not null,
    version       VARCHAR(256),
    friendly_name VARCHAR(256),
    description   VARCHAR(4000),
    arch_type_id  NUMERIC
                  CONSTRAINT suse_products_aid_fk
                  REFERENCES rhnPackageArch (id),
    release       VARCHAR(256),
    product_id    NUMERIC NOT NULL,
    channel_family_id NUMERIC
                        CONSTRAINT suse_products_cfid_fk
                        REFERENCES rhnChannelFamily (id)
                        ON DELETE SET NULL,
    base          CHAR(1) DEFAULT ('N') NOT NULL,
    free          CHAR(1) DEFAULT ('N') NOT NULL,
    release_stage VARCHAR(10) DEFAULT ('released') NOT NULL,
    created   TIMESTAMPTZ
                  DEFAULT (current_timestamp) NOT NULL,
    modified  TIMESTAMPTZ
                  DEFAULT (current_timestamp) NOT NULL
);

CREATE SEQUENCE suse_products_id_seq START WITH 100;

CREATE UNIQUE INDEX suseprod_pdid_uq
ON suseProducts (product_id)
;
