--
-- Copyright (c) 2026 SUSE LLC
--
-- This software is licensed to you under the GNU General Public License,
-- version 2 (GPLv2). There is NO WARRANTY for this software, express or
-- implied, including the implied warranties of MERCHANTABILITY or FITNESS
-- FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
-- along with this software; if not, see
-- http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
--

CREATE TABLE suseMinionTransactionalInfo
(
    minion_server_id NUMERIC NOT NULL
                         CONSTRAINT suse_minion_transactional_info_sid_fk
                             REFERENCES suseMinionInfo (server_id)
                             ON DELETE CASCADE,

    active_snapshot  NUMERIC,
    default_snapshot NUMERIC,
    snapshots        VARCHAR,
    snapshot_details TEXT,

    CONSTRAINT suse_minion_transactional_info_pk PRIMARY KEY (minion_server_id)
);
