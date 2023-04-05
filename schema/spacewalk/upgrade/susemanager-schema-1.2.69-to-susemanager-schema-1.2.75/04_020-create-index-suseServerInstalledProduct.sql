--
-- Copyright (c) 2012 SUSE Linux Products GmbH, Nuremberg, Germany
--
-- This software is licensed to you under the GNU General Public License,
-- version 2 (GPLv2). There is NO WARRANTY for this software, express or
-- implied, including the implied warranties of MERCHANTABILITY or FITNESS
-- FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
-- along with this software; if not, see
-- http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
--
--

CREATE UNIQUE INDEX suse_srv_inprod_uq
    ON suseServerInstalledProduct (rhn_server_id, suse_installed_product_id)
    TABLESPACE [[64k_tbs]];

CREATE INDEX suse_srv_inprod_pkg_idx
    ON suseServerInstalledProduct (rhn_server_id)
    TABLESPACE [[64k_tbs]]
    NOLOGGING;

commit;
