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

CREATE TABLE Repository
(
    mgm_id                    NUMERIC NOT NULL,
    repository_id             NUMERIC NOT NULL,
    label                     VARCHAR(128),
    url                       VARCHAR(2048),
    type                      VARCHAR(32),
    metadata_signed           BOOLEAN,
    organization              VARCHAR(128),
    synced_date               TIMESTAMPTZ DEFAULT (current_timestamp)
);

ALTER TABLE Repository
  ADD CONSTRAINT Repository_pk PRIMARY KEY (mgm_id, repository_id);
