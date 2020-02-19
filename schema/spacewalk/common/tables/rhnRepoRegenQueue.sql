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


CREATE TABLE rhnRepoRegenQueue
(
    id              NUMERIC
                        CONSTRAINT rhn_reporegenq_id_pk PRIMARY KEY,
    channel_label   VARCHAR(128) NOT NULL,
    client          VARCHAR(128),
    reason          VARCHAR(128),
    force           CHAR(1),
    bypass_filters  CHAR(1),
    next_action     TIMESTAMPTZ
                        DEFAULT (current_timestamp),
    created         TIMESTAMPTZ
                        DEFAULT (current_timestamp) NOT NULL,
    modified        TIMESTAMPTZ
                        DEFAULT (current_timestamp) NOT NULL
)
;

CREATE SEQUENCE rhn_repo_regen_queue_id_seq START WITH 101;

