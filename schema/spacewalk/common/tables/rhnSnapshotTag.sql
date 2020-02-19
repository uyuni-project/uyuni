--
-- Copyright (c) 2008--2012 Red Hat, Inc.
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


CREATE TABLE rhnSnapshotTag
(
    snapshot_id  NUMERIC NOT NULL
                     CONSTRAINT rhn_st_ssid_fk
                         REFERENCES rhnSnapshot (id)
                         ON DELETE CASCADE,
    tag_id       NUMERIC NOT NULL
                     CONSTRAINT rhn_st_tid_fk
                         REFERENCES rhnTag (id),
    server_id    NUMERIC
                     CONSTRAINT rhn_st_sid_fk
                         REFERENCES rhnServer (id),
    created      TIMESTAMPTZ
                     DEFAULT (current_timestamp) NOT NULL,
    modified     TIMESTAMPTZ
                     DEFAULT (current_timestamp) NOT NULL
)

;

CREATE UNIQUE INDEX rhn_ss_tag_ssid_tid_uq
    ON rhnSnapshotTag (snapshot_id, tag_id);

CREATE UNIQUE INDEX rhn_ss_tag_sid_tid_uq
    ON rhnSnapshotTag (server_id, tag_id);

CREATE INDEX rhn_ss_tag_tid_idx
    ON rhnSnapshotTag (tag_id);

