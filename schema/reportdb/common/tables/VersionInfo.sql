--
-- Copyright (c) 2022 SUSE LLC
--
-- This software is licensed to you under the GNU General Public License,
-- version 2 (GPLv2). There is NO WARRANTY for this software, express or
-- implied, including the implied warranties of MERCHANTABILITY or FITNESS
-- FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
-- along with this software; if not, see
-- http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
--

CREATE TABLE VersionInfo
(
    name      VARCHAR(256) NOT NULL,
    label     VARCHAR(64)  NOT NULL,
    version   VARCHAR(512) NOT NULL,
    release   VARCHAR(512) NOT NULL,
    created   TIMESTAMPTZ DEFAULT (current_timestamp) NOT NULL,
    modified  TIMESTAMPTZ DEFAULT (current_timestamp) NOT NULL
);

CREATE UNIQUE INDEX versioninfo_name_label_uq
    ON VersionInfo (name, label);
