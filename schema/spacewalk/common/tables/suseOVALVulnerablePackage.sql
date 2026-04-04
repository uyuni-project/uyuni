--
-- Copyright (c) 2023 SUSE LLC
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

CREATE TABLE suseOVALVulnerablePackage
(
    id             BIGINT
                      GENERATED ALWAYS AS IDENTITY
                      CONSTRAINT suse_oval_vulnerable_pkg_id_pk PRIMARY KEY,
    plat_vuln_id   BIGINT NOT NULL
                      REFERENCES suseOVALPlatformVulnerable(id)
                      ON DELETE CASCADE,
    name           VARCHAR NOT NULL,
    fix_version    evr_t,
                   CONSTRAINT plat_vuln_id_name_uq UNIQUE (plat_vuln_id, name)
);

CREATE INDEX suse_oval_vulnerable_pkg_plat_vuln_id_idx ON suseOVALVulnerablePackage(plat_vuln_id);
CREATE INDEX suse_oval_vulnerable_pkg_plat_vuln_id_name_idx ON suseOVALVulnerablePackage(plat_vuln_id, name);
