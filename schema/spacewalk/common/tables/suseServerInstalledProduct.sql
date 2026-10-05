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
-- SPDX-License-Identifier: GPL-2.0-only
--
--

create table
suseServerInstalledProduct
(
    rhn_server_id     NUMERIC
                      CONSTRAINT suseserver_ip_rhns_id_fk
                      REFERENCES rhnServer (id)
                      ON DELETE CASCADE
                      not null,
    suse_installed_product_id   NUMERIC
                                CONSTRAINT ssip_sip_id_fk
                                REFERENCES suseInstalledProduct (id)
                                not null,
    created   TIMESTAMPTZ
                  DEFAULT (current_timestamp) NOT NULL,
    modified  TIMESTAMPTZ
                  DEFAULT (current_timestamp) NOT NULL
);

CREATE UNIQUE INDEX suse_srv_inprod_uq
    ON suseServerInstalledProduct (rhn_server_id, suse_installed_product_id)
    ;

CREATE INDEX suse_srv_inprod_pkg_idx
    ON suseServerInstalledProduct (rhn_server_id)
    
    ;

