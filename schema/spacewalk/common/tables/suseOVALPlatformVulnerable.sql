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


CREATE TABLE suseOVALPlatformVulnerable
(
    id                   BIGINT
                            GENERATED ALWAYS AS IDENTITY
                            CONSTRAINT suse_oval_plat_vuln_id_pk PRIMARY KEY,
    platform_id          NUMERIC NOT NULL
                            REFERENCES suseOVALPlatform (id)
                            ON DELETE CASCADE,
    cve_id               NUMERIC NOT NULL
                            REFERENCES rhnCve (id),
                         CONSTRAINT platform_cve_id_uq UNIQUE (platform_id, cve_id)
);

CREATE INDEX suse_oval_plat_vuln_plat_id_idx ON suseOVALPlatformVulnerable(platform_id);
CREATE INDEX suse_oval_plat_vuln_cve_id_idx ON suseOVALPlatformVulnerable(cve_id);
CREATE INDEX suse_oval_plat_vuln_plat_cve_id_idx ON suseOVALPlatformVulnerable(platform_id, cve_id);
