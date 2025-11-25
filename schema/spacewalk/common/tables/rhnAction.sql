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
-- SPDX-License-Identifier: GPL-2.0-only
--
-- Red Hat trademarks are not licensed under GPLv2. No permission is
-- granted to use or replicate Red Hat trademarks that are incorporated
-- in this software or its documentation.
--


CREATE TABLE rhnAction
(
    id               NUMERIC NOT NULL
                         CONSTRAINT rhn_action_pk PRIMARY KEY
                         ,
    org_id           NUMERIC NOT NULL
                         CONSTRAINT rhn_action_oid_fk
                             REFERENCES web_customer (id)
                             ON DELETE CASCADE,
    action_type      NUMERIC NOT NULL
                         CONSTRAINT rhn_action_at_fk
                             REFERENCES rhnActionType (id),
    name             VARCHAR(128),
    scheduler        NUMERIC
                         CONSTRAINT rhn_action_scheduler_fk
                             REFERENCES web_contact (id)
                             ON DELETE SET NULL,
    earliest_action  TIMESTAMPTZ NOT NULL,
    version          NUMERIC
                         DEFAULT (0) NOT NULL,
    archived         NUMERIC
                         DEFAULT (0) NOT NULL
                         CONSTRAINT rhn_action_archived_ck
                             CHECK (archived in (0, 1)),
    prerequisite     NUMERIC
                         CONSTRAINT rhn_action_prereq_fk
                             REFERENCES rhnAction (id)
                             ON DELETE CASCADE,
    created          TIMESTAMPTZ
                         DEFAULT (current_timestamp) NOT NULL,
    modified         TIMESTAMPTZ
                         DEFAULT (current_timestamp) NOT NULL
)

;

CREATE INDEX rhn_action_oid_idx
    ON rhnAction(org_id);

CREATE INDEX rhn_action_scheduler_idx
    ON rhnAction(scheduler);

CREATE INDEX rhn_action_prereq_id_idx
    ON rhnAction(prerequisite, id);

CREATE INDEX rhn_action_created_idx
    ON rhnAction(created);

CREATE SEQUENCE rhn_event_id_seq;

