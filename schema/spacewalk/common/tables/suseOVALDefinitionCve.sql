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

DROP TABLE IF EXISTS suseOVALDefinitionCve;
CREATE TABLE suseOVALDefinitionCve
(
    definition_id    VARCHAR NOT NULL
                         REFERENCES suseOVALDefinition(id),
    cve_id           NUMERIC NOT NULL
                        REFERENCES rhncve(id)
);


CREATE UNIQUE INDEX suse_oval_def_cve_uq
    ON suseOVALDefinitionCve (definition_id, cve_id)
;