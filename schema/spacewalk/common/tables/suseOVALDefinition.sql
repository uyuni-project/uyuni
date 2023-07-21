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

CREATE TABLE suseOVALDefinition
(
    id              VARCHAR NOT NULL
                       CONSTRAINT suse_oval_definition_id_pk PRIMARY KEY,
    class           VARCHAR NOT NULL,
    title           VARCHAR,
    description     VARCHAR(10000),
    cve_id          NUMERIC
                       REFERENCES rhnCve(id),
    os_family       VARCHAR,
    os_version      VARCHAR,
    criteria_tree   JSON
);