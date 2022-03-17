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

CREATE TABLE suseMgrServerInfo
(
    server_id          NUMERIC NOT NULL
                           CONSTRAINT suse_mgr_info_sid_fk
                               REFERENCES rhnServer (id),
    mgr_evr_id         NUMERIC
                           CONSTRAINT suse_mgr_info_peid_fk
                               REFERENCES rhnPackageEVR (id),
    report_db_cred_id  NUMERIC
                           CONSTRAINT suse_mgr_info_creds_fk
                               REFERENCES suseCredentials (id)
                               ON DELETE SET NULL,
    report_db_name     VARCHAR(128),
    report_db_host     VARCHAR(256),
    report_db_port     NUMERIC DEFAULT (5432) NOT NULL,
    report_db_last_synced TIMESTAMPTZ,
    created            TIMESTAMPTZ
                           DEFAULT (current_timestamp) NOT NULL,
    modified           TIMESTAMPTZ
                           DEFAULT (current_timestamp) NOT NULL,
    CONSTRAINT suse_mgr_info_sid_uq UNIQUE (server_id)
)
;
