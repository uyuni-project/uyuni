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

DROP TABLE IF EXISTS suseOVALPackageState;
CREATE TABLE suseOVALPackageState
(
    id                  NUMERIC NOT NULL
                           CONSTRAINT suse_oval_pkg_state_id_pk PRIMARY KEY,
    operator            VARCHAR,
    comment             VARCHAR,
    arch_state_id       NUMERIC
                           REFERENCES suseOVALPackageArchState(id),
    version_state_id    NUMERIC
                            REFERENCES suseOVALPackageVersionState(id),
    evr_state_id        NUMERIC
                            REFERENCES suseOVALPackageEvrState(id),
    isRpm               BOOLEAN
);