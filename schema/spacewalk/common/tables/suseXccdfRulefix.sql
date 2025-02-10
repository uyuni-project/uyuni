--
-- Copyright (c) 2025 SUSE
--
-- This software is licensed to you under the GNU General Public License,
-- version 2 (GPLv2). There is NO WARRANTY for this software, express or
-- implied, including the implied warranties of MERCHANTABILITY or FITNESS
-- FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
-- along with this software; if not, see
-- http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
--

CREATE TABLE suseXccdfRuleFix
(
    id                   INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    identifier           TEXT NOT NULL,
    remediation          TEXT NOT NULL ,
    benchMarkId          TEXT NOT NULL
);

CREATE UNIQUE INDEX idx_identifier_benchMarkId
    ON suseXccdfRuleFix (identifier, benchMarkId); -- Ensures unique identified names within a benchmark


